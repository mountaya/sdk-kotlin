#!/usr/bin/env bash
# USAGE-START
# Releases sdk-kotlin: the annotated tag v<X.Y.Z> on the commit CI passed, pushed to the repository
# on Entire, its five modules published to Maven Central together through the Central Portal, then
# the wait for the public mirror on GitHub, where mirror.yml makes its GitHub Release.
#
#   mise run release <X.Y.Z> [--yes]
#   mise run release <X.Y.Z> --check
#   mise run release <X.Y.Z> --credentials
#
# <X.Y.Z> is the version gradle/libs.versions.toml and the README's install lines give every
# module, with a dated CHANGELOG.md section (`mise run version <X.Y.Z>` writes all three), past the
# newest `v*` tag. The release runs from `main`, clean, at the commit Entire's `main` is at, on which
# CI finished green. It asks before it does anything; --yes answers yes. --check runs every check,
# the credentials' included, says what a release would do, and changes nothing. --credentials takes
# and checks the credentials alone, as a release does, and nothing else: platform's release asks
# for every credential with it before it waits on CI and runs the device suites.
#
# Run again after a stop, it resumes from what the remotes and the Portal say: `mise run ci:local`
# runs once on a commit, a tag made here or on Entire is kept, and only what Maven Central lacks is
# published.
#
# The credentials are MOUNTAYA_SIGNING_KEY, the ASCII-armoured secret key every publication is
# signed with, and MOUNTAYA_SIGNING_PASSWORD, its passphrase, and MOUNTAYA_CENTRAL_USERNAME and
# MOUNTAYA_CENTRAL_PASSWORD, the two halves of a Central Portal user token: each from the
# environment, else the macOS Keychain (service mountaya-release), else asked for, and checked
# online before anything is built. MOUNTAYA_SIGNING_KEY_ID, from the environment, names the signing
# subkey where the primary key does not sign. The repository on Entire is the remote `origin`;
# RELEASE_ENTIRE_REMOTE names another, RELEASE_MIRROR_URL another mirror and RELEASE_PORTAL_URL
# another Portal than https://central.sonatype.com, for a test; RELEASE_POLL_SECONDS is how long to
# wait between two asks (15), and RELEASE_PORTAL_WAIT_SECONDS how long the Portal is waited on at
# most, to validate a deployment (1800) and to publish it (3600).
# USAGE-END
#
# It refuses, every refusal told together with what to do: what every release refuses
# (release_checks: a version that is not MAJOR.MINOR.PATCH, a dirty tree, off `main` or not at
# Entire's `main`, a tag elsewhere than this commit or not newer than every `v*`, CI not finished
# green on this commit); a version the catalog's `mountaya` or the README's install lines do not
# name; no dated CHANGELOG.md section for it; a sibling that is not clean at the commit
# siblings.lock pins; licence texts that are not sdk-typescript's bytes, or a licence address that
# does not answer; and, until `mise run ci:local` passed on this commit, a host that is not macOS or
# no emulator running. Then it takes the credentials, each checked online (the key and its
# passphrase sign a test payload in a throwaway GnuPG home, the token is answered by the Portal), and
# refuses a version Maven Central holds already, unless the tag is on this commit already, as a
# release that stopped after its tag leaves it.
#
# Then, once asked: `mise run disk:check` and `CI=true mise run ci:local`, unless ci:local passed
# here on this commit; every module published, signed, into build/release/maven and zipped
# (centralBundle), uploaded to the Portal as the USER_MANAGED deployment sdk-kotlin-<version> and
# waited on until the Portal has validated it, which proves the publish before anything is tagged;
# the annotated tag, noted with CHANGELOG.md's section; the tag pushed alone to Entire, whose `main`
# holds the commit already; the deployment published through the Portal API and waited on until
# it is, unless Maven Central holds every module at the version already; and the wait for the
# mirror. Run again after the tag, the deployment's id is gone with the process that uploaded it:
# a deployment of the version the Portal is publishing is waited on, and otherwise the bundle is
# built, uploaded and validated again, and that deployment published. It makes no commit and
# pushes no `main`.
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
# shellcheck source=lib/release.sh
. "$(dirname "$0")/lib/release.sh"
answer_help "$@"
require_cmd awk "awk, which comes with the operating system"
require_cmd base64 "base64, which comes with the operating system"
require_cmd cmp "cmp, which comes with the operating system"
require_cmd curl "curl, which comes with the operating system"
require_cmd git "git, which comes with Xcode's command line tools: xcode-select --install"
require_cmd gpg "brew install gnupg"
require_cmd mise "mise: sh .agents/setup installs it"

root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"

version=""
yes=0
check=0
credentials=0
while [ "$#" -gt 0 ]; do
  case "$1" in
    --yes) yes=1 ;;
    --check) check=1 ;;
    --credentials) credentials=1 ;;
    --dry-run | --skip-gate)
      die "\`$1\` is no option of the release any more: CI's run on the commit is the gate, and --check runs every check and changes nothing." \
        "mise run release <X.Y.Z> --check"
      ;;
    -*) die "\`$1\` is no option of the release." "bash scripts/release.sh --help" ;;
    *)
      [ -z "$version" ] || die "The release takes one version, and was given \`$version\` and \`$1\`." "mise run release <X.Y.Z>"
      version="$1"
      ;;
  esac
  shift
done
[ -n "$version" ] || die "The release takes the version it releases." "mise run release <X.Y.Z>, such as: mise run release 0.2.0"
entire="${RELEASE_ENTIRE_REMOTE:-origin}"
tag="v$version"
portal="${RELEASE_PORTAL_URL:-https://central.sonatype.com}"
portal="${portal%/}"
poll="${RELEASE_POLL_SECONDS:-15}"
namespace="com.mountaya"
deployment_name="sdk-kotlin-$version"
# Every module a release publishes, each after every module it depends on: the core, its admin unit,
# the geo layer, then the elements and navigation over it. One deployment holds them all.
modules="mountaya-client mountaya-admin mountaya-geo mountaya-elements mountaya-navigation"
module_count=5
repository="$root/build/release/maven"
bundle="$root/build/release/central-bundle.zip"

# The credentials handed to each step through its environment, and to no other step.
signing_secrets="-u MOUNTAYA_SIGNING_KEY -u MOUNTAYA_SIGNING_PASSWORD"
central_secrets="-u MOUNTAYA_CENTRAL_USERNAME -u MOUNTAYA_CENTRAL_PASSWORD"

# portal <method> <path and query> <answer file>: one call to the Central Portal's API, its answer
# written into the file and its HTTP status printed, 000 where nothing answered. The token
# ($portal_auth) travels in a header curl reads from its standard input (`--config -`), never on a
# command line and never in a file.
portal_auth=""
portal() {
  printf 'header = "Authorization: Bearer %s"\n' "$portal_auth" \
    | curl -sS --max-time 60 --config - -X "$1" -o "$3" -w '%{http_code}' "$portal$2" 2>/dev/null || true
}

# published_modules: the modules Maven Central holds at the version, one a line, as the Portal's
# `published` says, which no cache stands in front of. It fails, saying why, where the Portal does
# not answer.
published_modules() {
  local module code answer
  answer="$(mktemp)"
  for module in $modules; do
    code="$(portal GET "/api/v1/publisher/published?namespace=$namespace&name=$module&version=$version" "$answer")"
    if [ "$code" != 200 ]; then
      echo "Asked whether Maven Central holds $module $version, the Central Portal answered ${code}: $(cat "$answer" 2>/dev/null)" >&2
      rm -f "$answer"
      return 1
    fi
    if grep -Eq '"published"[[:space:]]*:[[:space:]]*true' "$answer"; then
      echo "$module"
    fi
  done
  rm -f "$answer"
}

# publishing_deployment: the id of the deployment named $deployment_name the Portal is publishing,
# nothing where it publishes none. The Portal matches a name as a substring, so each deployment's
# own fields are read at its own depth of the answer and its name compared whole. It fails, saying
# why, where the Portal does not answer.
publishing_deployment() {
  local code answer
  answer="$(mktemp)"
  code="$(portal GET "/api/v1/publisher/deployments?namespace=$namespace&deploymentName=$deployment_name&deploymentState=PUBLISHING" "$answer")"
  if [ "$code" != 200 ]; then
    echo "Asked for the deployments of $deployment_name it is publishing, the Central Portal answered ${code}: $(cat "$answer" 2>/dev/null)" >&2
    rm -f "$answer"
    return 1
  fi
  awk -v name="$deployment_name" '
    { text = text $0 "\n" }
    END {
      # The list is {"deployments": [{...}, ...]}: each deployment an object at depth 3, its
      # components nested deeper. A string at depth 3 is a key where a colon follows it, and the
      # value of the key before it where a colon came first.
      n = length(text)
      depth = 0; quoted = 0; token = ""; key = ""; pending = ""; id = ""; named = ""; state = ""
      for (i = 1; i <= n; i++) {
        c = substr(text, i, 1)
        if (quoted) {
          if (c == "\\") { token = token substr(text, i + 1, 1); i++; continue }
          if (c == "\"") {
            quoted = 0
            if (depth == 3) {
              if (pending != "") {
                if (pending == "deploymentId") id = token
                else if (pending == "deploymentName") named = token
                else if (pending == "deploymentState") state = token
                pending = ""
              } else {
                key = token
              }
            }
            continue
          }
          token = token c
          continue
        }
        if (c == "\"") { quoted = 1; token = ""; continue }
        if (c == "{" || c == "[") {
          depth++
          if (depth == 3) { id = ""; named = ""; state = ""; key = ""; pending = "" }
          continue
        }
        if (c == "}" || c == "]") {
          if (depth == 3 && named == name && state == "PUBLISHING" && id != "") print id
          depth--
          continue
        }
        if (depth == 3 && c == ":") { pending = key; continue }
        if (depth == 3 && c == ",") { pending = ""; key = ""; continue }
      }
    }
  ' "$answer" | sed -n 1p
  rm -f "$answer"
}

# signing_works <key> <passphrase or nothing>: whether gpg imports the key as a secret key, in a
# throwaway GnuPG home deleted with the agent it started, and, given the passphrase, signs a test
# payload with the key Gradle signs with: MOUNTAYA_SIGNING_KEY_ID, else the primary key. Each value
# reaches gpg on its standard input, never on a command line. It prints why where it fails.
signing_works() {
  local home primary signer works=0
  home="$(mktemp -d)"
  chmod 700 "$home"
  if ! printf '%s\n' "$1" | GNUPGHOME="$home" gpg --batch --quiet --import >/dev/null 2>"$home/import.log"; then
    echo "gpg imports no key from it: $(sed -n 1p "$home/import.log")" >&2
    works=1
  else
    primary="$(GNUPGHOME="$home" gpg --batch --with-colons --list-secret-keys 2>/dev/null \
      | awk -F: '$1 == "sec" { primary = 1; next } primary && $1 == "fpr" { print $10; exit }')"
    signer="${MOUNTAYA_SIGNING_KEY_ID:-$primary}"
    if [ -z "$primary" ]; then
      echo "It holds no secret key, only a public one: export it with gpg --armor --export-secret-keys <id>." >&2
      works=1
    elif [ -n "$2" ]; then
      printf 'sdk-kotlin release check\n' >"$home/payload"
      if ! printf '%s\n' "$2" | GNUPGHOME="$home" gpg --batch --yes --quiet --pinentry-mode loopback --passphrase-fd 0 \
        --local-user "$signer!" --output "$home/payload.sig" --detach-sign "$home/payload" 2>"$home/sign.log"; then
        echo "The key does not sign with it: $(sed -n 1p "$home/sign.log")" >&2
        works=1
      fi
    fi
  fi
  if command -v gpgconf >/dev/null 2>&1; then
    GNUPGHOME="$home" gpgconf --kill gpg-agent >/dev/null 2>&1 || true
  fi
  rm -rf "$home"
  return "$works"
}
signing_key_works() { signing_works "$1" ""; }
signing_passphrase_works() { signing_works "$MOUNTAYA_SIGNING_KEY" "$1"; }

# central_token_works <password>: whether the Portal answers one authenticated read made with the
# token MOUNTAYA_CENTRAL_USERNAME and <password> make. A Portal that cannot be asked stops the
# release there, since the token may well be right.
central_token_works() {
  local answer code why
  portal_auth="$(printf '%s:%s' "$MOUNTAYA_CENTRAL_USERNAME" "$1" | base64 | tr -d '\n')"
  answer="$(mktemp)"
  code="$(portal GET "/api/v1/publisher/published?namespace=$namespace&name=mountaya-client&version=$version" "$answer")"
  case "$code" in
    200)
      rm -f "$answer"
      return 0
      ;;
    401 | 403)
      echo "The Central Portal answers $code to the token MOUNTAYA_CENTRAL_USERNAME and MOUNTAYA_CENTRAL_PASSWORD make: $(cat "$answer"). Generate a user token at $portal/usertoken; its two halves are the two values." >&2
      rm -f "$answer"
      return 1
      ;;
  esac
  why="$(cat "$answer" 2>/dev/null || true)"
  rm -f "$answer"
  die "The Central Portal, $portal, could not be asked whether the token works: it answered ${code:-nothing}${why:+: $why}." \
    "Reach it, then run the release again."
}

# take_credentials: the four credentials, each from the environment, the Keychain or a prompt, and
# each checked online, a failure stopping the release with why; $portal_auth from the token.
take_credentials() {
  release_secret MOUNTAYA_SIGNING_KEY "the ASCII-armoured secret key every publication is signed with" signing_key_works multiline
  release_secret MOUNTAYA_SIGNING_PASSWORD "the signing key's passphrase" signing_passphrase_works
  release_secret MOUNTAYA_CENTRAL_USERNAME "the first half of the Central Portal user token"
  release_secret MOUNTAYA_CENTRAL_PASSWORD "the second half of the Central Portal user token" central_token_works
  portal_auth="$(printf '%s:%s' "$MOUNTAYA_CENTRAL_USERNAME" "$MOUNTAYA_CENTRAL_PASSWORD" | base64 | tr -d '\n')"
}

if [ "$credentials" -eq 1 ]; then
  release_is_version "$version" || die "\`$version\` is no release version: a release is MAJOR.MINOR.PATCH, each a number with no leading zero." \
    "mise run release <X.Y.Z> --credentials, such as: mise run release 0.2.0 --credentials"
  take_credentials
  ok "Every credential of the release works: the signing key signs with its passphrase, and the Central Portal answers the token."
  exit 0
fi

# The checks every release makes, then this repository's own.
release_checks "$entire" "$version"
# A resume: the tag is on this commit here already, made by a release that stopped after it, which
# validated a deployment of this commit before it tagged.
resume=0
[ "$(git rev-parse -q --verify "refs/tags/$tag^{commit}" 2>/dev/null || true)" != "$release_head" ] || resume=1

stated="$(sed -n 's/^mountaya = "\(.*\)"$/\1/p' gradle/libs.versions.toml)"
if [ "$stated" != "$version" ]; then
  release_refuse "The build gives every module ${stated:-no version}, in gradle/libs.versions.toml, and the release
is $version, so the tag and what Maven Central would hold would part ways.

  Fix: mise run version $version, commit it as \"version: Release $tag\", push main, then run the
  release again once CI passed."
else
  for line in $(grep -o 'com\.mountaya:mountaya-[a-z-]*:[^")[:space:]]*' README.md || true); do
    if [ "${line##*:}" != "$version" ]; then
      release_refuse "README.md installs ${line%:*} at ${line##*:}, and the release is $version.

  Fix: mise run version $version, commit it as \"version: Release $tag\", push main, then run the
  release again once CI passed."
      break
    fi
  done
fi

if [ ! -f CHANGELOG.md ] || ! release_dated CHANGELOG.md "$version"; then
  release_refuse "CHANGELOG.md has no dated section for $version, so the release would say nothing of what it carries.

  Fix: mise run version $version, which dates the [Unreleased] notes, commit it, push main, then run
  the release again once CI passed."
fi

# The siblings beside this checkout, each clean at the commit siblings.lock pins, so ci:local runs
# on what CI tested this commit against.
if ! siblings="$(sh .agents/siblings at-pins 2>&1)"; then
  release_refuse "A sibling beside this checkout is not clean at the commit siblings.lock pins, so ci:local would
not run on what CI tested this commit against:
$(printf '%s\n' "$siblings" | sed 's/^/  /')

  Fix: do what the lines above say for each (sh .agents/siblings status shows every pin beside its
  sibling), then run the release again."
fi

typescript="$(cd .. && pwd -P)/sdk-typescript"
for text in LICENSE.txt LICENCE.fr.txt; do
  if [ ! -f "$typescript/$text" ]; then
    release_refuse "$typescript/$text is missing, so nothing holds $text to the licence the TypeScript
packages ship.

  Fix: clone sdk-typescript beside this repository, with: entire repo clone /et/mountaya/sdk-typescript,
  then run the release again."
  elif ! cmp -s "$text" "$typescript/$text"; then
    release_refuse "$text differs from sdk-typescript's, and the modules ship the TypeScript packages'
licence byte for byte.

  Fix: copy sdk-typescript's $text over this one, commit it, then run the release again."
  fi
done

# The licence's address, which every POM names and Maven Central keeps for good: the one the build
# states, and one that answers.
licence_source="build-logic/src/main/kotlin/com/mountaya/build/licence/LicenceCheck.kt"
licence="$(sed -n 's/^ *const val URL = "\(.*\)"$/\1/p' "$licence_source" 2>/dev/null || true)"
if [ -z "$licence" ]; then
  release_refuse "$licence_source states no URL, the
address every POM names the licence by.

  Fix: state it there as \`const val URL = \"<address>\"\`, then run the release again."
else
  answered="$(curl -sS -L --max-time 30 -o /dev/null -w '%{http_code}' "$licence" 2>/dev/null || true)"
  if [ "$answered" != "200" ]; then
    release_refuse "The licence's address, $licence, answers ${answered:-nothing}, and every POM published
names the licence by it, for good.

  Fix: publish the licence there, or name the address it is published at in LicenceCheck.URL,
  then run the release again."
  fi
fi

# What ci:local runs on: macOS for the iOS simulator suites, and an emulator for the device tests
# and Trail's journey. Once ci:local passed on this commit, neither is asked for.
if ! release_ran ci-local "$release_head"; then
  if [ "$(uname -s)" != "Darwin" ]; then
    release_refuse "This host is $(uname -s), and mise run ci:local runs the iOS simulator suites, which macOS alone runs.

  Fix: run the release on a Mac with the Xcode mise.toml's toolchain:check names."
  fi
  if ! has_cmd adb; then
    release_refuse "adb is not on PATH, so nothing can tell whether an emulator is running for the device tests.

  Fix: mise run android:packages, then run the release under mise: mise run release $version"
  elif [ -z "$(adb devices 2>/dev/null | awk 'NR > 1 && $2 == "device"')" ]; then
    release_refuse "No emulator is running, and mise run ci:local runs the device tests and Trail's journey on one.

  Fix: boot the AVD (mise run android:emulator creates it): emulator -avd mountaya-api37 -gpu host,
  then run the release again."
  fi
fi
release_refused "sdk-kotlin $version"

# The credentials, once nothing else stands in the way, so none is asked for a release refused anyway.
take_credentials

if ! held="$(published_modules)"; then
  die "The Central Portal could not say whether Maven Central holds $version: the line above says why." \
    "Reach it, then run the release again."
fi
held_count="$(printf '%s' "$held" | grep -c . || true)"
if [ "$held_count" -gt 0 ] && [ "$resume" -eq 0 ]; then
  release_refuse "Maven Central already holds $(printf '%s' "$held" | tr '\n' ' ' | sed 's/ $//') at $version, and a version is published once.

  Fix: release a version past $version."
fi
release_refused "sdk-kotlin $version"

# step <line> [<line>]: one numbered step of what the release does.
steps=0
step() {
  steps=$((steps + 1))
  echo "  $steps. $1"
  [ -z "${2:-}" ] || echo "     $2"
}

head_short="$(git rev-parse --short=12 HEAD)"
echo ""
echo "${PF_BOLD}The release of sdk-kotlin $version:${PF_RESET}"
if release_ran ci-local "$release_head"; then
  step "mise run ci:local passed on $head_short already: it is not run again."
else
  step "mise run disk:check, then CI=true mise run ci:local: the toolchain check, the device tests and Trail's" \
    "journey on the emulator, and the iOS simulator suites, the part of the gate CI cannot run."
fi
if [ "$resume" -eq 0 ]; then
  step "Every module, signed, published into build/release/maven and zipped (centralBundle), uploaded to" \
    "the Central Portal as the deployment $deployment_name, and waited on until the Portal has validated it."
  step "The annotated tag $tag on $head_short, noted with CHANGELOG.md's [$version] section."
else
  step "$tag is tagged on $head_short already, by a release that validated its deployment before it: it is kept."
fi
step "$tag pushed alone to $entire ($(git remote get-url "$entire")), unless it is there: main is there already."
if [ "$held_count" -eq "$module_count" ]; then
  step "Maven Central holds every module at $version already: nothing is published."
elif [ "$resume" -eq 0 ]; then
  step "The deployment published to Maven Central through the Portal, and waited on until it is."
else
  step "The modules published to Maven Central: a deployment of $version the Portal is publishing is waited on;" \
    "otherwise the bundle is built, uploaded and validated again, and that deployment published."
fi
step "A wait until the mirror, $RELEASE_MIRROR, lists $tag: mirror.yml pushes it and makes its GitHub Release."
echo ""
if [ "$check" -eq 1 ]; then
  ok "Every check passed: nothing was built, tagged, uploaded, pushed or published."
  exit 0
fi
release_confirm "sdk-kotlin $version" "$yes"

release_lock

if release_ran ci-local "$release_head"; then
  ok "mise run ci:local passed on $head_short already."
else
  info "Running the part of the gate CI cannot run: mise run disk:check, then CI=true mise run ci:local."
  # The disk check passes under CI without looking, so it runs on its own first. ci:local is handed
  # none of the credentials.
  # shellcheck disable=SC2086 # Each credential is a word of its own to env.
  env $signing_secrets $central_secrets mise run disk:check \
    || die "The disk lacks room for ci:local, as the lines above say: nothing was tagged or published." \
      "Make room (the lines above name both cleans), then run the release again."
  # shellcheck disable=SC2086 # Each credential is a word of its own to env.
  env $signing_secrets $central_secrets CI=true mise run ci:local \
    || die "mise run ci:local failed on $head_short, as the log above says: nothing was tagged or published." \
      "Fix what it reported in a commit of its own, push it, then run the release again once CI passed."
  release_record ci-local "$release_head"
  ok "mise run ci:local passed on $head_short."
fi

# build_bundle: every module published, signed, into the release repository, which
# checkReleaseRepository holds to what the Portal validates, and zipped into the bundle. Gradle is
# handed the signing key and its passphrase, and not the Portal's token.
build_bundle() {
  rm -rf "$root/build/release"
  info "Publishing every module, signed, into build/release/maven and zipping the bundle (centralBundle)."
  # shellcheck disable=SC2086 # Each credential is a word of its own to env.
  if ! env $central_secrets mise exec -- ./gradlew --console=plain \
    "-Pmountaya.releaseRepository=$repository" "-Pmountaya.releaseBundle=$bundle" centralBundle; then
    die "Publishing the modules into build/release/maven failed, as the log above says: $1" \
      "Fix what it reported in a commit of its own, push it, then run the release again once CI passed."
  fi
  ok "Published $(find "$repository" -type f | wc -l | tr -d ' ') files into build/release/maven, signed, and zipped build/release/central-bundle.zip."
}

# upload_bundle: the bundle uploaded as the USER_MANAGED deployment $deployment_name and waited on
# until the Portal has validated it; its id in $deployment. The upload is handed the Portal's token,
# and not the signing key.
deployment=""
upload_bundle() {
  # shellcheck disable=SC2086 # Each credential is a word of its own to env.
  if ! deployment="$(env $signing_secrets bash scripts/central-upload.sh "$bundle" "$deployment_name")"; then
    die "The Central Portal validated no deployment of $version, as the lines above say: $1" \
      "Fix what the Portal named in a commit of its own, push it, then run the release again once CI passed."
  fi
  ok "The Central Portal validated deployment $deployment."
}

# The dry checks: the publish proven, up to the Portal's validation, before anything is tagged.
if [ "$resume" -eq 0 ]; then
  build_bundle "nothing was tagged or published."
  upload_bundle "nothing was tagged or published."
fi

notes="$(mktemp)"
release_notes CHANGELOG.md "$version" >"$notes"
release_tag "$tag" "sdk-kotlin $version" "$notes"
rm -f "$notes"
release_push_tag "$entire" "$tag"

# wait_published <deployment>: until the Portal says the deployment is on Maven Central, an hour
# at most.
wait_published() {
  local answer code state="" deadline limit="${RELEASE_PORTAL_WAIT_SECONDS:-3600}"
  answer="$(mktemp)"
  deadline=$(($(date +%s) + limit))
  while :; do
    code="$(portal POST "/api/v1/publisher/status?id=$1" "$answer")"
    if [ "$code" = 200 ]; then
      state="$(sed -n 's/.*"deploymentState"[[:space:]]*:[[:space:]]*"\([A-Z_]*\)".*/\1/p' "$answer" | sed -n 1p)"
      case "$state" in
        PUBLISHED)
          rm -f "$answer"
          ok "The Central Portal published deployment $1: Maven Central holds every module at $version."
          return 0
          ;;
        FAILED)
          die "The Central Portal failed to publish deployment $1: $(cat "$answer"). $tag is on $entire." \
            "Read what it names at $portal/publishing/deployments, then run the release again: it publishes what Maven Central lacks."
          ;;
      esac
    fi
    if [ "$(date +%s)" -ge "$deadline" ]; then
      die "Deployment $1 is still ${state:-unanswered} after $limit seconds; $tag is on $entire." \
        "Follow it at $portal/publishing/deployments, then run the release again: it waits on a deployment the Portal is publishing, and goes on to the mirror once Maven Central holds $version."
    fi
    info "Deployment $1 is ${state:-not yet answered}; asking again in $poll seconds."
    sleep "$poll"
  done
}

# The publish of what Maven Central lacks, read from the Portal again, since a release that stopped
# after its tag may have published already.
if ! held="$(published_modules)"; then
  die "The Central Portal could not say whether Maven Central holds $version, as the line above says; $tag is on $entire." \
    "Once it answers, run the release again: it publishes what Maven Central lacks."
fi
if [ "$(printf '%s' "$held" | grep -c . || true)" -eq "$module_count" ]; then
  ok "Maven Central holds every module at $version already: nothing to publish."
else
  if ! publishing="$(publishing_deployment)"; then
    die "The Central Portal could not list its deployments of $version, as the line above says; $tag is on $entire." \
      "Once it answers, run the release again: it publishes what Maven Central lacks."
  fi
  if [ -n "$publishing" ]; then
    info "The Central Portal is publishing deployment $publishing of $version already: waiting on it."
    deployment="$publishing"
  else
    if [ -z "$deployment" ]; then
      info "$tag was tagged by a release that stopped before it published, and its deployment's id went with it: the bundle is built and uploaded again."
      build_bundle "$tag is on $entire, and nothing was published. Where it fails again, this commit cannot be published: release a version past $version."
      upload_bundle "$tag is on $entire, and nothing was published."
    fi
    answer="$(mktemp)"
    code="$(portal POST "/api/v1/publisher/deployment/$deployment" "$answer")"
    if [ "$code" != 204 ]; then
      why="$(cat "$answer" 2>/dev/null || true)"
      rm -f "$answer"
      die "The Central Portal did not publish deployment $deployment, answering ${code}${why:+: $why}; $tag is on $entire, and nothing was published." \
        "Run the release again: it uploads the bundle again and publishes that deployment."
    fi
    rm -f "$answer"
    ok "The Central Portal is publishing deployment $deployment."
  fi
  wait_published "$deployment"
fi

release_wait_mirror "$tag"
ok "sdk-kotlin $version is released: its five modules on Maven Central (https://central.sonatype.com/artifact/$namespace/mountaya-client/$version), and $RELEASE_MIRROR/releases/tag/$tag"
