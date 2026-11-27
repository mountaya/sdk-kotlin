#!/usr/bin/env bash
# USAGE-START
# Tests scripts/version.sh and scripts/release.sh against fakes, with no network, no registry and
# no Gradle, in a scratch directory it deletes after: a copy of the files the two scripts read, in a
# git repository of its own, beside an `sdk-typescript` holding the licence texts; a bare repository
# as Entire, whose post-receive hook logs what it is pushed and feeds a bare repository as the
# mirror, as mirror.yml does; and, first on PATH, fakes of depot (one CI run, of the status a file
# holds), mise (each task it runs logged in order, centralBundle writing a bundle), curl (the
# Central Portal's API, its deployments kept in files, and the licence's address), gpg, adb, uname
# and security, which reads no Keychain.
#
#   bash scripts/test-release.sh
#
# It runs on macOS and on Linux, where CI runs it (`mise run test:release`, in `mise run ci`), and
# touches no git of this repository: it copies the files, and commits and pushes in the copy.
# USAGE-END
# No set -e: a failing case is reported and counted, and the exit status says whether any did.
set -uo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"
require_cmd git "git, which comes with Xcode's command line tools: xcode-select --install"
require_cmd awk "awk, which comes with the operating system"
require_cmd base64 "base64, which comes with the operating system"
require_cmd cksum "cksum, which comes with the operating system"
require_cmd sed "sed, which comes with the operating system"

root="$(cd "$(dirname "$0")/.." && pwd -P)"
WS="$(cd "$(mktemp -d)" && pwd -P)"
trap 'rm -rf "$WS"' EXIT
BIN="$WS/bin"
STATE="$WS/state"
EVENTS="$WS/events.log"
REPO="$WS/sdk-kotlin"
mkdir -p "$BIN" "$STATE/portal/deployments" "$STATE/portal/published"
: >"$EVENTS"
: >"$STATE/argv.log"
: >"$STATE/gpg-homes"

# git as a test's, whatever this machine's or CI's own configuration says: an identity for the
# commits and the tags, no signing, no hooks but the fixture's.
export GIT_CONFIG_NOSYSTEM=1 GIT_CONFIG_GLOBAL="$WS/gitconfig" GIT_TERMINAL_PROMPT=0
unset GIT_DIR GIT_WORK_TREE GIT_INDEX_FILE GIT_COMMON_DIR GIT_OBJECT_DIRECTORY GIT_ALTERNATE_OBJECT_DIRECTORIES
printf '[user]\n\tname = test\n\temail = test@example.com\n[init]\n\tdefaultBranch = main\n[commit]\n\tgpgSign = false\n[tag]\n\tgpgSign = false\n' >"$GIT_CONFIG_GLOBAL"
# No credential of this shell reaches the scripts under test, and none of the Keychain does; CI,
# which CI's own run sets, is left to the release to set, so a test sees where it does; and an
# agent's shell (CLAUDECODE), where release_confirm and release_lock refuse, is not the fixture's.
unset MOUNTAYA_SIGNING_KEY MOUNTAYA_SIGNING_PASSWORD MOUNTAYA_SIGNING_KEY_ID MOUNTAYA_CENTRAL_USERNAME MOUNTAYA_CENTRAL_PASSWORD CI CLAUDECODE

# The credentials the fakes take: the key gpg imports, the passphrase it signs with, and the token
# the Portal answers.
KEY="$(printf -- '-----BEGIN PGP PRIVATE KEY BLOCK-----\n\nlQdGBGVzZXJpYWwtdGVzdC1rZXk\n-----END PGP PRIVATE KEY BLOCK-----')"
PUBLIC_KEY="$(printf -- '-----BEGIN PGP PUBLIC KEY BLOCK-----\n\nmQINBGVzZXJpYWwtdGVzdC1rZXk\n-----END PGP PUBLIC KEY BLOCK-----')"
export RS_STATE="$STATE" RS_EVENTS="$EVENTS" RS_PASSPHRASE="passphrase-7f3a-never-on-argv"
export RS_CENTRAL_USERNAME="portal-user-2c9e" RS_CENTRAL_PASSWORD="portal-secret-5b1d-never-on-argv"
TOKEN="$(printf '%s:%s' "$RS_CENTRAL_USERNAME" "$RS_CENTRAL_PASSWORD" | base64 | tr -d '\n')"
signing_key="$KEY"
signing_password="$RS_PASSPHRASE"
central_username="$RS_CENTRAL_USERNAME"
central_password="$RS_CENTRAL_PASSWORD"

passed=0
failed=0
# report <case> <yes or no> [<what was seen>]: one case, and what was seen where it failed.
report() {
  if [ "$2" = yes ]; then
    passed=$((passed + 1))
    ok "$1"
  else
    failed=$((failed + 1))
    echo "${PF_RED}✗${PF_RESET} $1"
    [ -z "${3:-}" ] || printf '%s\n' "$3" | sed 's/^/    /'
  fi
}
# holds <text> <string>: whether the text holds the string, as it is written.
holds() { printf '%s\n' "$1" | grep -qF -- "$2"; }
# in_order <text> <line>...: whether a line of the text starts with each line given, each after the
# one before.
in_order() {
  local text="$1" at=0 line found
  shift
  for line in "$@"; do
    found="$(printf '%s\n' "$text" | awk -v want="$line" -v after="$at" 'NR > after && index($0, want) == 1 { print NR; exit }')"
    [ -n "$found" ] || return 1
    at="$found"
  done
}
# mark, then since <mark>: the events logged after the mark.
mark() { wc -l <"$EVENTS" | tr -d ' '; }
since() { tail -n +"$(($1 + 1))" "$EVENTS"; }

# depot, as release_ci asks it: the run a push of main started, of the status $STATE/ci holds
# (finished where it holds none), after a newer, failed one a tag's push started, which a release
# passes over, whichever commit is asked; `none` lists no run.
cat >"$BIN/depot" <<'FAKE'
#!/bin/sh
printf '%s\n' "$*" >>"$RS_STATE/depot.log"
status="$(cat "$RS_STATE/ci" 2>/dev/null || echo finished)"
[ "$status" = none ] && { echo '[]'; exit 0; }
printf '[{"run_id": "run_tag", "repo": "et/mountaya/sdk-kotlin", "status": "failed", "trigger": "push", "ref": "refs/tags/v0.0.1"},\n'
printf ' {"run_id": "run_1", "repo": "et/mountaya/sdk-kotlin", "status": "%s", "trigger": "push", "ref": "refs/heads/main"}]\n' "$status"
FAKE

# mise: `run <task>` logged, with CI's value and the credentials its environment holds, failing
# for ci:local where $STATE/ci-local-fails exists; `exec -- ./gradlew ... centralBundle` logged
# the same way, writing the release repository and the bundle the properties name.
cat >"$BIN/mise" <<'FAKE'
#!/bin/sh
seen=""
for name in MOUNTAYA_SIGNING_KEY MOUNTAYA_SIGNING_PASSWORD MOUNTAYA_CENTRAL_USERNAME MOUNTAYA_CENTRAL_PASSWORD; do
  [ -z "$(printenv "$name")" ] || seen="$seen $name"
done
case "${1:-}" in
  run)
    printf 'mise run %s%s%s\n' "$2" "${CI:+ under CI=$CI}" "${seen:+ with$seen}" >>"$RS_EVENTS"
    if [ "$2" = ci:local ] && [ -e "$RS_STATE/ci-local-fails" ]; then
      echo "ci:local: a device test failed" >&2
      exit 1
    fi
    exit 0
    ;;
  exec)
    shift
    [ "${1:-}" = -- ] && shift
    printf '%s\n' "$*" >>"$RS_STATE/argv.log"
    task=""
    repository=""
    bundle=""
    for argument in "$@"; do
      case "$argument" in
        -Pmountaya.releaseRepository=*) repository="${argument#*=}" ;;
        -Pmountaya.releaseBundle=*) bundle="${argument#*=}" ;;
        -* | ./gradlew) ;;
        *) task="$argument" ;;
      esac
    done
    printf 'gradle %s%s\n' "$task" "${seen:+ with$seen}" >>"$RS_EVENTS"
    [ ! -e "$RS_STATE/gradle-fails" ] || exit 1
    mkdir -p "$repository/com/mountaya/mountaya-client" "$(dirname "$bundle")"
    echo "a POM" >"$repository/com/mountaya/mountaya-client/mountaya-client.pom"
    echo "a bundle" >"$bundle"
    exit 0
    ;;
esac
echo "fake mise: no $*" >&2
exit 2
FAKE

# curl: the licence's address answers 200 (404 where $STATE/licence-down exists); the Central
# Portal's API answers a call made with the test's token, read from a `--config -` on standard
# input, and 401 to any other. A deployment is a file under $STATE/portal/deployments holding its
# name and its state, a published module a file under $STATE/portal/published. Its arguments are
# logged, so a test shows no credential stands on a command line.
cat >"$BIN/curl" <<'FAKE'
#!/bin/sh
printf '%s\n' "$*" >>"$RS_STATE/argv.log"
out=""
method=GET
url=""
config=0
write=0
while [ "$#" -gt 0 ]; do
  case "$1" in
    -o) out="$2"; shift ;;
    -w) write=1; shift ;;
    -X) method="$2"; shift ;;
    -F) method=POST; shift ;;
    --config) [ "$2" = - ] && config=1; shift ;;
    --max-time | -H) shift ;;
    -*) ;;
    *) url="$1" ;;
  esac
  shift
done
token=""
[ "$config" -eq 0 ] || token="$(sed -n 's/^header = "Authorization: Bearer \(.*\)"$/\1/p')"
answer() {
  if [ -n "$out" ]; then printf '%s' "$2" >"$out"; else printf '%s' "$2"; fi
  [ "$write" -eq 0 ] || printf '%s' "$1"
  exit 0
}
query() { printf '%s\n' "$url" | sed -n "s/.*[?&]$1=\([^&]*\).*/\1/p"; }
case "$url" in
  */api/v1/publisher/*) ;;
  *)
    [ ! -e "$RS_STATE/licence-down" ] || answer 404 "Not found"
    answer 200 "The Mountaya SDK License 1.0"
    ;;
esac
[ ! -e "$RS_STATE/portal-down" ] || answer 503 "Service unavailable"
good="$(printf '%s:%s' "$RS_CENTRAL_USERNAME" "$RS_CENTRAL_PASSWORD" | base64 | tr -d '\n')"
[ "$token" = "$good" ] || answer 401 '{"httpStatus":401,"errorCode":10401,"message":"Authentication required"}'
path="${url#*/api/v1/publisher/}"
path="${path%%\?*}"
deployments="$RS_STATE/portal/deployments"
case "$method $path" in
  "GET published")
    [ ! -e "$RS_STATE/portal/published/$(query name)-$(query version)" ] || answer 200 '{"published":true}'
    answer 200 '{"published":false}'
    ;;
  "POST upload")
    name="$(query name)"
    printf 'portal upload %s as %s%s\n' "$name" "$(query publishingType)" "${MOUNTAYA_SIGNING_KEY:+ with the signing key}" >>"$RS_EVENTS"
    count=$(($(cat "$RS_STATE/portal/count" 2>/dev/null || echo 0) + 1))
    echo "$count" >"$RS_STATE/portal/count"
    state=VALIDATING
    [ ! -e "$RS_STATE/validation-fails" ] || state=FAILED
    printf '%s %s\n' "$name" "$state" >"$deployments/dep-$count"
    answer 201 "dep-$count"
    ;;
  "POST status")
    id="$(query id)"
    [ -f "$deployments/$id" ] || answer 404 '{"httpStatus":404,"message":"Deployment with provided id not found"}'
    read -r name state <"$deployments/$id"
    # A deployment is validating at its first ask and validated at the next; publishing at its
    # first ask after the publish, and published, every module with it, at the next, unless
    # $STATE/publishing-stuck holds it there.
    case "$state" in
      VALIDATING) printf '%s VALIDATED\n' "$name" >"$deployments/$id" ;;
      PUBLISHING)
        [ ! -e "$RS_STATE/publishing-stuck" ] || answer 200 "{\"deploymentId\":\"$id\",\"deploymentName\":\"$name\",\"deploymentState\":\"PUBLISHING\",\"purls\":[]}"
        printf '%s PUBLISHED\n' "$name" >"$deployments/$id"
        for module in mountaya-client mountaya-admin mountaya-geo mountaya-elements mountaya-navigation; do
          : >"$RS_STATE/portal/published/$module-${name#sdk-kotlin-}"
        done
        ;;
    esac
    errors='{}'
    [ "$state" != FAILED ] || errors='{"pkg:maven/com.mountaya/mountaya-client":["Invalid signature"]}'
    answer 200 "{\"deploymentId\":\"$id\",\"deploymentName\":\"$name\",\"deploymentState\":\"$state\",\"purls\":[],\"errors\":$errors}"
    ;;
  "POST deployment/"*)
    id="${path#deployment/}"
    printf 'portal publish %s\n' "$id" >>"$RS_EVENTS"
    if [ -e "$RS_STATE/publish-fails" ]; then
      rm -f "$RS_STATE/publish-fails"
      answer 500 '{"httpStatus":500,"errorCode":10500,"message":"Internal server error"}'
    fi
    [ -f "$deployments/$id" ] || answer 404 '{"httpStatus":404,"message":"Deployment not found"}'
    read -r name state <"$deployments/$id"
    [ "$state" = VALIDATED ] || answer 400 '{"httpStatus":400,"message":"Bad request"}'
    printf '%s PUBLISHING\n' "$name" >"$deployments/$id"
    answer 204 ""
    ;;
  "GET deployments")
    wanted="$(query deploymentName)"
    wanted_state="$(query deploymentState)"
    list=""
    for file in "$deployments"/*; do
      [ -f "$file" ] || continue
      read -r name state <"$file"
      case "$name" in *"$wanted"*) ;; *) continue ;; esac
      [ -z "$wanted_state" ] || [ "$state" = "$wanted_state" ] || continue
      # Its id after its name and its components, which name an id of their own: no client may
      # count on the order of an object's keys.
      list="$list${list:+,}{\"deploymentName\":\"$name\",\"namespace\":\"com.mountaya\",\"deploymentState\":\"$state\",\"deploymentComponents\":[{\"name\":\"$name.jar\",\"deploymentId\":\"not-this-one\",\"errors\":[]}],\"deploymentId\":\"${file##*/}\"}"
    done
    answer 200 "{\"deployments\":[$list],\"page\":0,\"pageSize\":20,\"pageCount\":1}"
    ;;
esac
answer 404 '{"httpStatus":404,"message":"Not found"}'
FAKE

# gpg: imports a private key block into the GnuPG home it is given, and no other; lists its
# secret key; signs with the passphrase it reads on standard input where it is the test's, with
# the primary key. Each home and each argument list is logged.
cat >"$BIN/gpg" <<'FAKE'
#!/bin/sh
printf '%s\n' "$*" >>"$RS_STATE/argv.log"
[ -n "${GNUPGHOME:-}" ] && [ -d "$GNUPGHOME" ] || { echo "gpg: no GnuPG home of its own" >&2; exit 2; }
echo "$GNUPGHOME" >>"$RS_STATE/gpg-homes"
case " $* " in
  *" --import "*)
    case "$(cat)" in
      *"BEGIN PGP PRIVATE KEY BLOCK"*) : >"$GNUPGHOME/secret"; exit 0 ;;
      *"BEGIN PGP PUBLIC KEY BLOCK"*) exit 0 ;;
    esac
    echo "gpg: no valid OpenPGP data found." >&2
    exit 2
    ;;
  *" --list-secret-keys "*)
    [ ! -e "$GNUPGHOME/secret" ] || printf 'sec:u:255:22:0123456789ABCDEF:1700000000:::u:::scESC:::+:::ed25519:::0:\nfpr:::::::::0123456789ABCDEF0123456789ABCDEF01234567:\n'
    exit 0
    ;;
  *" --detach-sign "*)
    case " $* " in
      *" --local-user 0123456789ABCDEF0123456789ABCDEF01234567! "*) ;;
      *) echo "gpg: signing failed: not the primary key" >&2; exit 2 ;;
    esac
    [ "$(cat)" = "$RS_PASSPHRASE" ] || { echo "gpg: signing failed: Bad passphrase" >&2; exit 2; }
    exit 0
    ;;
esac
exit 0
FAKE
printf '#!/bin/sh\nexit 0\n' >"$BIN/gpgconf"

# adb lists an emulator where $STATE/emulator exists; uname -s says Darwin, or what $STATE/host
# holds; security finds nothing, so no test reads this machine's Keychain.
cat >"$BIN/adb" <<'FAKE'
#!/bin/sh
echo "List of devices attached"
[ ! -e "$RS_STATE/emulator" ] || printf 'emulator-5554\tdevice\n'
exit 0
FAKE
cat >"$BIN/uname" <<'FAKE'
#!/bin/sh
if [ "${1:-}" = -s ]; then
  cat "$RS_STATE/host" 2>/dev/null || echo Darwin
  exit 0
fi
for real in /usr/bin/uname /bin/uname; do
  [ ! -x "$real" ] || exec "$real" "$@"
done
exit 1
FAKE
printf '#!/bin/sh\nexit 44\n' >"$BIN/security"
chmod +x "$BIN"/*

# Entire, a bare repository whose post-receive logs each ref it is pushed and feeds the mirror, as
# mirror.yml does, unless the mirror is held.
git init -q --bare -b main "$WS/entire.git"
git init -q --bare -b main "$WS/mirror.git"
cat >"$WS/entire.git/hooks/post-receive" <<HOOK
#!/bin/sh
while read -r old new ref; do printf 'entire %s\n' "\$ref" >>"$EVENTS"; done
[ -e "$STATE/mirror-held" ] && exit 0
git push -q "$WS/mirror.git" 'refs/tags/*:refs/tags/*' 'refs/heads/main:refs/heads/main' 2>/dev/null
exit 0
HOOK
chmod +x "$WS/entire.git/hooks/post-receive"

# The copy: the files the two scripts read, at this checkout's content; the licence texts again
# as sdk-typescript's beside it; and a stand-in for the rendered siblings script, whose at-pins
# answers as $STATE/siblings says.
mkdir -p "$REPO/scripts/lib" "$REPO/gradle" "$REPO/.agents" "$REPO/build-logic/src/main/kotlin/com/mountaya/build/licence" "$WS/sdk-typescript"
cp "$root/scripts/release.sh" "$root/scripts/version.sh" "$root/scripts/central-upload.sh" "$REPO/scripts/"
cp "$root/scripts/lib/common.sh" "$root/scripts/lib/release.sh" "$REPO/scripts/lib/"
cp "$root/gradle/libs.versions.toml" "$REPO/gradle/"
cp "$root/README.md" "$root/CHANGELOG.md" "$root/LICENSE.txt" "$root/LICENCE.fr.txt" "$root/.gitignore" "$REPO/"
cp "$root/build-logic/src/main/kotlin/com/mountaya/build/licence/LicenceCheck.kt" "$REPO/build-logic/src/main/kotlin/com/mountaya/build/licence/"
cp "$root/LICENSE.txt" "$root/LICENCE.fr.txt" "$WS/sdk-typescript/"
cat >"$REPO/.agents/siblings" <<'FAKE'
#!/bin/sh
if [ "${1:-}" = at-pins ] && [ -s "$RS_STATE/siblings" ]; then
  cat "$RS_STATE/siblings"
  exit 1
fi
exit 0
FAKE

G() { git -C "$REPO" "$@"; }
# set_unreleased <notes>: the copy's CHANGELOG.md with <notes> (`\n` for a line break) under
# `## [Unreleased]`, and nothing else there.
set_unreleased() {
  awk -v notes="$1" '
    /^## \[Unreleased\][[:space:]]*$/ { print; print ""; if (notes != "") { print notes; print "" }; skip = 1; next }
    skip && /^## \[/ { skip = 0 }
    !skip { print }
  ' "$REPO/CHANGELOG.md" >"$WS/changelog" && cat "$WS/changelog" >"$REPO/CHANGELOG.md"
}
# The versions the test releases, past the one this checkout states.
current="$(sed -n 's/^mountaya = "\(.*\)"$/\1/p' "$root/gradle/libs.versions.toml")"
major="${current%%.*}"
minor="${current#*.}"
minor="${minor%%.*}"
next="$major.$((minor + 1)).0"
later="$major.$((minor + 2)).0"

set_unreleased ""
G init -q -b main
G remote add origin "$WS/entire.git"
G add -A
G commit -q -m "feat: First"
G push -q origin main 2>/dev/null

version() { (cd "$REPO" && bash scripts/version.sh "$@" </dev/null 2>&1); }
release() {
  (cd "$REPO" && PATH="$BIN:$PATH" RELEASE_MIRROR_URL="$WS/mirror.git" RELEASE_PORTAL_URL="https://portal.test" \
    RELEASE_POLL_SECONDS=1 RELEASE_WAIT_SECONDS=3 RELEASE_PORTAL_WAIT_SECONDS=4 \
    MOUNTAYA_SIGNING_KEY="$signing_key" MOUNTAYA_SIGNING_PASSWORD="$signing_password" \
    MOUNTAYA_CENTRAL_USERNAME="$central_username" MOUNTAYA_CENTRAL_PASSWORD="$central_password" \
    bash scripts/release.sh "$@" </dev/null 2>&1)
}
# sums: a checksum of each file mise run version writes.
sums() { (cd "$REPO" && cksum gradle/libs.versions.toml README.md CHANGELOG.md); }

echo "scripts/version.sh: the release written into the files, and nothing else"
out="$(version "$next")"
rc=$?
report "a CHANGELOG.md with nothing under [Unreleased] and no section for the version is refused" \
  "$([ "$rc" -eq 1 ] && holds "$out" "nothing under \`## [Unreleased]\`" && echo yes || echo no)" "exit $rc: $out"
out="$(version 1.02.0)"
rc=$?
report "a version that is not MAJOR.MINOR.PATCH is refused" "$([ "$rc" -eq 1 ] && holds "$out" "is no release version" && echo yes || echo no)" "exit $rc: $out"

set_unreleased '### Added\n\n- A note on Mountaya for the next release.'
G commit -q -am "docs: Note the next release"
G push -q origin main 2>/dev/null
before="$(G rev-parse HEAD)"
out="$(version "$next")"
rc=$?
today="$(date -u +%Y-%m-%d)"
installs="$(grep -c "com\.mountaya:mountaya-[a-z-]*:$next\"" "$REPO/README.md")"
report "it writes the version into the catalog, every README install line and a dated CHANGELOG.md section" \
  "$([ "$rc" -eq 0 ] && grep -qx "mountaya = \"$next\"" "$REPO/gradle/libs.versions.toml" \
    && [ "$installs" -eq "$(grep -c 'com\.mountaya:mountaya-[a-z-]*:' "$root/README.md")" ] && [ "$installs" -gt 0 ] \
    && ! grep -q "com\.mountaya:mountaya-[a-z-]*:$current\"" "$REPO/README.md" \
    && grep -qx "## \[$next\] - $today" "$REPO/CHANGELOG.md" && echo yes || echo no)" "exit $rc: $out"
report "the notes move under the dated section, an empty [Unreleased] left above it" \
  "$(awk -v section="## [$next] - $today" '$0 == "## [Unreleased]" { u = NR } $0 == section { s = NR } /A note on Mountaya/ { n = NR } END { exit !(u && s > u && n > s) }' "$REPO/CHANGELOG.md" \
    && [ -z "$(awk '/^## \[Unreleased\]/ { inside = 1; next } inside && /^## \[/ { exit } inside && NF { print }' "$REPO/CHANGELOG.md")" ] && echo yes || echo no)" \
  "$(sed -n '1,40p' "$REPO/CHANGELOG.md")"
report "and it changes those three files alone, and no git" \
  "$([ "$(G status --porcelain | awk '{ print $2 }' | LC_ALL=C sort | tr '\n' ' ')" = "CHANGELOG.md README.md gradle/libs.versions.toml " ] \
    && [ "$(G rev-parse HEAD)" = "$before" ] && [ -z "$(G tag --list)" ] && echo yes || echo no)" "$(G status --porcelain)"
first="$(sums)"
out="$(version "$next")"
rc=$?
report "run again, it changes nothing" "$([ "$rc" -eq 0 ] && [ "$(sums)" = "$first" ] && echo yes || echo no)" "exit $rc: $out"

G commit -q -am "version: Release v$next"
G push -q origin main 2>/dev/null
release_commit="$(G rev-parse HEAD)"
: >"$STATE/emulator"

echo ""
echo "scripts/release.sh: the checks, every refusal told together"
out="$(release --help)"
report "--help says how the release runs" "$(holds "$out" "mise run release <X.Y.Z> --check" && echo yes || echo no)" "$out"
out="$(release "$next" --dry-run)"
rc=$?
report "--dry-run is gone, and the refusal names --check" "$([ "$rc" -eq 1 ] && holds "$out" "mise run release <X.Y.Z> --check" && echo yes || echo no)" "exit $rc: $out"

at="$(mark)"
out="$(release "$next" --check)"
rc=$?
report "--check passes on the pushed commit CI passed, and builds, tags, uploads and pushes nothing" \
  "$([ "$rc" -eq 0 ] && holds "$out" "Every check passed" && [ -z "$(G tag --list)" ] && [ -z "$(since "$at")" ] \
    && grep -q -- "--repo et/mountaya/sdk-kotlin --sha $release_commit" "$STATE/depot.log" && echo yes || echo no)" "exit $rc: $out
$(since "$at")"
report "its plan names ci:local, the deployment validated before the tag, the tag, the publish and the mirror" \
  "$(holds "$out" "CI=true mise run ci:local" && holds "$out" "uploaded to" && holds "$out" "The annotated tag v$next" \
    && holds "$out" "published to Maven Central through the Portal" && holds "$out" "A wait until the mirror" && echo yes || echo no)" "$out"

out="$(release 9.9.9 --check)"
rc=$?
report "a version the files do not state is refused, with the command that writes it, beside the changelog's refusal" \
  "$([ "$rc" -eq 1 ] && holds "$out" "mise run version 9.9.9" && holds "$out" "no dated section for 9.9.9" && echo yes || echo no)" "exit $rc: $out"

printf 'failed\n' >"$STATE/ci"
out="$(release "$next" --check)"
rc=$?
report "a commit CI failed on is refused, naming the run to read" \
  "$([ "$rc" -eq 1 ] && holds "$out" "CI failed on" && holds "$out" "depot ci logs run_1" && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/ci"

: >"$REPO/stray.txt"
out="$(release "$next" --check)"
rc=$?
report "a working tree with changes is refused" "$([ "$rc" -eq 1 ] && holds "$out" "The working tree holds changes" && echo yes || echo no)" "exit $rc: $out"
rm -f "$REPO/stray.txt"

G commit -q --allow-empty -m "fix: Not pushed"
out="$(release "$next" --check)"
rc=$?
report "a commit Entire's main is not at is refused" "$([ "$rc" -eq 1 ] && holds "$out" "is not the one \`origin\`'s main is at" && echo yes || echo no)" "exit $rc: $out"
G reset -q --hard "$release_commit"

printf '../schemas is at 1234567, not at its pin abcdef0.\n' >"$STATE/siblings"
out="$(release "$next" --check)"
rc=$?
report "a sibling off its pin is refused, with what at-pins says" "$([ "$rc" -eq 1 ] && holds "$out" "../schemas is at 1234567, not at its pin abcdef0." && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/siblings"

: >"$STATE/licence-down"
out="$(release "$next" --check)"
rc=$?
report "a licence address that does not answer is refused" "$([ "$rc" -eq 1 ] && holds "$out" "answers 404" && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/licence-down"

rm -f "$STATE/emulator"
printf 'Linux\n' >"$STATE/host"
out="$(release "$next" --check)"
rc=$?
report "until ci:local passed on the commit, no emulator and a host other than macOS are refused, together" \
  "$([ "$rc" -eq 1 ] && holds "$out" "No emulator is running" && holds "$out" "This host is Linux" && echo yes || echo no)" "exit $rc: $out"
: >"$STATE/emulator"
rm -f "$STATE/host"

echo ""
echo "scripts/release.sh: the credentials, each checked online before anything runs"
at="$(mark)"
: >"$STATE/gpg-homes"
signing_password="the-wrong-passphrase"
out="$(release "$next" --yes)"
rc=$?
report "a passphrase the key does not sign with is refused, saying why, before anything runs" \
  "$([ "$rc" -eq 1 ] && holds "$out" "MOUNTAYA_SIGNING_PASSWORD, the signing key's passphrase, from the environment, does not work" \
    && holds "$out" "Bad passphrase" && [ -z "$(since "$at")" ] && [ -z "$(G tag --list)" ] && echo yes || echo no)" "exit $rc: $out"
signing_password="$RS_PASSPHRASE"
report "gpg ran in throwaway homes alone, each gone after" \
  "$([ -s "$STATE/gpg-homes" ] && ! grep -qx "${HOME:-/nonexistent}/.gnupg" "$STATE/gpg-homes" \
    && [ -z "$(while read -r home; do [ ! -e "$home" ] || echo "$home"; done <"$STATE/gpg-homes")" ] && echo yes || echo no)" "$(cat "$STATE/gpg-homes")"

signing_key="$PUBLIC_KEY"
out="$(release "$next" --check)"
rc=$?
report "a public key where the secret one belongs is refused" "$([ "$rc" -eq 1 ] && holds "$out" "It holds no secret key" && echo yes || echo no)" "exit $rc: $out"
signing_key="garbage"
out="$(release "$next" --check)"
rc=$?
report "and so is no key at all" "$([ "$rc" -eq 1 ] && holds "$out" "gpg imports no key from it" && echo yes || echo no)" "exit $rc: $out"
signing_key="$KEY"

central_password="not-the-token"
out="$(release "$next" --check)"
rc=$?
report "a token the Portal answers 401 to is refused, naming where to make one" \
  "$([ "$rc" -eq 1 ] && holds "$out" "MOUNTAYA_CENTRAL_PASSWORD, the second half of the Central Portal user token, from the environment, does not work" \
    && holds "$out" "answers 401" && holds "$out" "https://portal.test/usertoken" && echo yes || echo no)" "exit $rc: $out"
central_password="$RS_CENTRAL_PASSWORD"

: >"$STATE/portal-down"
out="$(release "$next" --check)"
rc=$?
report "a Portal that cannot be asked stops the release, and blames no token" \
  "$([ "$rc" -eq 1 ] && holds "$out" "could not be asked whether the token works: it answered 503" && ! holds "$out" "does not work" && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/portal-down"

central_username=""
out="$(release "$next" --check)"
rc=$?
report "a credential in neither the environment nor the Keychain, with no terminal to ask, stops the release" \
  "$([ "$rc" -eq 1 ] && holds "$out" "MOUNTAYA_CENTRAL_USERNAME, the first half of the Central Portal user token, is in neither the environment nor the Keychain" && echo yes || echo no)" "exit $rc: $out"
central_username="$RS_CENTRAL_USERNAME"

# --credentials, on a tree every other check refuses: a commit CI failed on, with changes.
printf 'failed\n' >"$STATE/ci"
: >"$REPO/stray.txt"
at="$(mark)"
asked="$(wc -l <"$STATE/depot.log" | tr -d ' ')"
: >"$STATE/gpg-homes"
out="$(release "$next" --credentials)"
rc=$?
report "--credentials takes and checks the credentials alone, and runs, asks and tags nothing else" \
  "$([ "$rc" -eq 0 ] && holds "$out" "Every credential of the release works" && [ -z "$(since "$at")" ] \
    && [ "$(wc -l <"$STATE/depot.log" | tr -d ' ')" = "$asked" ] && [ -s "$STATE/gpg-homes" ] && [ -z "$(G tag --list)" ] && echo yes || echo no)" "exit $rc: $out"
central_password="not-the-token"
out="$(release "$next" --credentials)"
rc=$?
report "and refuses one that does not work, as the release does" \
  "$([ "$rc" -eq 1 ] && holds "$out" "answers 401" && holds "$out" "MOUNTAYA_CENTRAL_PASSWORD" && echo yes || echo no)" "exit $rc: $out"
central_password="$RS_CENTRAL_PASSWORD"
rm -f "$STATE/ci" "$REPO/stray.txt"

at="$(mark)"
out="$(release "$next")"
rc=$?
report "without a terminal or --yes the release asks, and stops before it runs anything" \
  "$([ "$rc" -eq 1 ] && holds "$out" "nothing can answer here" && [ -z "$(since "$at")" ] && [ -z "$(G tag --list)" ] && echo yes || echo no)" "exit $rc: $out"
out="$(CLAUDECODE=1 release "$next" --yes)"
rc=$?
report "an agent's shell is refused, --yes or not, before it runs anything" \
  "$([ "$rc" -eq 1 ] && holds "$out" "a release is a person's" && [ -z "$(since "$at")" ] && [ -z "$(G tag --list)" ] && echo yes || echo no)" "exit $rc: $out"

echo ""
echo "scripts/release.sh: a release that stops, run again, resumes from what the remotes and the Portal say"
at="$(mark)"
: >"$STATE/validation-fails"
out="$(release "$next" --yes)"
rc=$?
seen="$(since "$at")"
report "a bundle the Portal does not validate stops the release before the tag, after ci:local" \
  "$([ "$rc" -eq 1 ] && holds "$out" "validated no deployment of $next" && holds "$out" "Invalid signature" \
    && in_order "$seen" "mise run disk:check" "mise run ci:local under CI=true" "gradle centralBundle" "portal upload sdk-kotlin-$next as USER_MANAGED" \
    && [ -z "$(G tag --list)" ] && ! holds "$seen" "entire " && echo yes || echo no)" "exit $rc: $out
$seen"
report "ci:local is handed no credential, Gradle the signing key alone, the upload the token alone" \
  "$(holds "$seen" "mise run ci:local under CI=true" && ! printf '%s\n' "$seen" | grep '^mise run' | grep -q ' with ' \
    && holds "$seen" "gradle centralBundle with MOUNTAYA_SIGNING_KEY MOUNTAYA_SIGNING_PASSWORD" && ! printf '%s\n' "$seen" | grep '^gradle' | grep -q CENTRAL \
    && ! holds "$seen" "with the signing key" && echo yes || echo no)" "$seen"
report "and leaves no lock behind" "$([ ! -e "$REPO/.git/mountaya/release.lock" ] && echo yes || echo no)"
rm -f "$STATE/validation-fails"

# ci:local passed on this commit, so neither the emulator nor macOS is asked for any more.
rm -f "$STATE/emulator"
printf 'Linux\n' >"$STATE/host"
at="$(mark)"
: >"$STATE/publish-fails"
out="$(release "$next" --yes)"
rc=$?
seen="$(since "$at")"
report "run again, ci:local is not run twice on one commit, and no device is asked for" \
  "$(holds "$out" "mise run ci:local passed on" && ! holds "$seen" "mise run" && echo yes || echo no)" "exit $rc: $out
$seen"
report "a publish the Portal refuses stops the release after the tag is pushed, naming the rerun" \
  "$([ "$rc" -eq 1 ] && holds "$out" "did not publish deployment dep-2" && holds "$out" "Run the release again" \
    && in_order "$seen" "gradle centralBundle" "portal upload sdk-kotlin-$next" "entire refs/tags/v$next" "portal publish dep-2" \
    && [ "$(git -C "$WS/entire.git" rev-parse -q --verify "refs/tags/v$next")" = "$(G rev-parse "refs/tags/v$next")" ] \
    && [ ! -e "$STATE/portal/published/mountaya-client-$next" ] && echo yes || echo no)" "exit $rc: $out
$seen"
report "the tag is annotated, named sdk-kotlin <version>, noted verbatim with the version's CHANGELOG.md section" \
  "$([ "$(G cat-file -t "v$next")" = tag ] && [ "$(G tag -l --format='%(contents:subject)' "v$next")" = "sdk-kotlin $next" ] \
    && G tag -l --format='%(contents:body)' "v$next" | grep -qx '### Added' \
    && G tag -l --format='%(contents:body)' "v$next" | grep -qx -- '- A note on Mountaya for the next release.' && echo yes || echo no)" \
  "$(G tag -l --format='%(contents)' "v$next")"

at="$(mark)"
out="$(release "$next" --yes)"
rc=$?
seen="$(since "$at")"
report "run again after the tag push, it publishes alone: the bundle built and uploaded again, that deployment published" \
  "$([ "$rc" -eq 0 ] && holds "$out" "its deployment's id went with it" && holds "$out" "origin holds v$next already" \
    && in_order "$seen" "gradle centralBundle" "portal upload sdk-kotlin-$next" "portal publish dep-3" \
    && ! holds "$seen" "mise run" && ! holds "$seen" "entire " && [ -e "$STATE/portal/published/mountaya-navigation-$next" ] \
    && holds "$out" "sdk-kotlin $next is released" && echo yes || echo no)" "exit $rc: $out
$seen"
report "the mirror lists the tag as it is here" \
  "$([ "$(git ls-remote "$WS/mirror.git" "refs/tags/v$next" | awk '{ print $1 }')" = "$(G rev-parse "refs/tags/v$next")" ] && echo yes || echo no)"

at="$(mark)"
out="$(release "$next" --yes)"
rc=$?
seen="$(since "$at")"
report "a version Maven Central holds already is not published again, and the release goes on to the mirror" \
  "$([ "$rc" -eq 0 ] && holds "$out" "Maven Central holds every module at $next already: nothing to publish" \
    && [ -z "$seen" ] && holds "$out" "The mirror lists v$next" && echo yes || echo no)" "exit $rc: $out
$seen"

# A deployment the Portal is publishing, beside one whose name holds this one's: the first is
# waited on, and nothing is uploaded or published again.
rm -f "$STATE/portal/published/"*"-$next"
printf 'sdk-kotlin-%s0 PUBLISHING\n' "$next" >"$STATE/portal/deployments/dep-89"
printf 'sdk-kotlin-%s PUBLISHING\n' "$next" >"$STATE/portal/deployments/dep-90"
at="$(mark)"
out="$(release "$next" --yes)"
rc=$?
seen="$(since "$at")"
report "a deployment of the version the Portal is publishing is waited on, its name matched whole" \
  "$([ "$rc" -eq 0 ] && holds "$out" "publishing deployment dep-90 of $next already" && [ -z "$seen" ] \
    && [ -e "$STATE/portal/published/mountaya-client-$next" ] && grep -qx "sdk-kotlin-${next}0 PUBLISHING" "$STATE/portal/deployments/dep-89" && echo yes || echo no)" "exit $rc: $out
$seen"

# A publish that outlasts the wait stops the release, and the rerun waits on that deployment.
rm -f "$STATE/portal/published/"*"-$next"
printf 'sdk-kotlin-%s PUBLISHING\n' "$next" >"$STATE/portal/deployments/dep-91"
: >"$STATE/publishing-stuck"
out="$(release "$next" --yes)"
rc=$?
report "a publish the Portal has not finished within the wait stops the release, naming the rerun" \
  "$([ "$rc" -eq 1 ] && holds "$out" "Deployment dep-91 is still PUBLISHING after 4 seconds" \
    && holds "$out" "it waits on a deployment the Portal is publishing" && [ ! -e "$STATE/portal/published/mountaya-client-$next" ] && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/publishing-stuck"
at="$(mark)"
out="$(release "$next" --yes)"
rc=$?
report "and run again, it waits on that deployment until Maven Central holds the version" \
  "$([ "$rc" -eq 0 ] && holds "$out" "publishing deployment dep-91 of $next already" && [ -z "$(since "$at")" ] \
    && [ -e "$STATE/portal/published/mountaya-elements-$next" ] && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/host"
: >"$STATE/emulator"

report "no release made a commit or pushed main" \
  "$([ "$(G rev-parse HEAD)" = "$release_commit" ] && [ "$(git -C "$WS/entire.git" rev-parse refs/heads/main)" = "$release_commit" ] \
    && [ -z "$(G status --porcelain)" ] && echo yes || echo no)" "$(G log --oneline -3)"

echo ""
echo "scripts/release.sh: a release from the version commit to the mirror, in one run"
set_unreleased '### Fixed\n\n- Another note.'
G commit -q -am "docs: Note the release after"
version "$later" >/dev/null
G commit -q -am "version: Release v$later"
G push -q origin main 2>/dev/null
later_commit="$(G rev-parse HEAD)"
for module in mountaya-client mountaya-geo; do : >"$STATE/portal/published/$module-$later"; done
out="$(release "$later" --check)"
rc=$?
report "a version Maven Central holds, with no tag on the commit, is refused" \
  "$([ "$rc" -eq 1 ] && holds "$out" "Maven Central already holds mountaya-client mountaya-geo at $later" && echo yes || echo no)" "exit $rc: $out"
rm -f "$STATE/portal/published/"*"-$later"

at="$(mark)"
out="$(release "$later" --yes)"
rc=$?
seen="$(since "$at")"
report "it runs ci:local, proves the deployment, tags, pushes the tag, publishes and waits for the mirror, in that order" \
  "$([ "$rc" -eq 0 ] && in_order "$seen" "mise run disk:check" "mise run ci:local under CI=true" "gradle centralBundle" \
    "portal upload sdk-kotlin-$later as USER_MANAGED" "entire refs/tags/v$later" "portal publish dep-4" \
    && holds "$out" "The mirror lists v$later" && [ -e "$STATE/portal/published/mountaya-admin-$later" ] \
    && [ "$(printf '%s\n' "$out" | sed '/^[[:space:]]*$/d' | tail -n 1 | grep -c "sdk-kotlin $later is released")" -eq 1 ] && echo yes || echo no)" "exit $rc: $out
$seen"
report "ci:local ran once, and the release pushed the tag alone: no main, no commit" \
  "$([ "$(printf '%s\n' "$seen" | grep -c '^mise run ci:local')" -eq 1 ] && ! holds "$seen" "entire refs/heads/main" \
    && [ "$(G rev-parse HEAD)" = "$later_commit" ] && [ "$(git -C "$WS/entire.git" rev-parse refs/heads/main)" = "$later_commit" ] && echo yes || echo no)" "$seen"
report "no credential stood on a command line: not the key, the passphrase, the token or its halves" \
  "$(! grep -qF -e "$RS_PASSPHRASE" -e "$RS_CENTRAL_PASSWORD" -e "$TOKEN" -e "PGP PRIVATE KEY" -e "Bearer" "$STATE/argv.log" && echo yes || echo no)" \
  "$(grep -F -e "$RS_PASSPHRASE" -e "$RS_CENTRAL_PASSWORD" -e "$TOKEN" -e "PGP PRIVATE KEY" -e "Bearer" "$STATE/argv.log")"
report "and leaves no lock behind" "$([ ! -e "$REPO/.git/mountaya/release.lock" ] && echo yes || echo no)"

echo ""
echo "$passed passed, $failed failed"
[ "$failed" -eq 0 ]
