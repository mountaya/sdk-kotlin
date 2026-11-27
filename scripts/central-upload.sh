#!/usr/bin/env bash
# USAGE-START
# Uploads a release bundle to the Central Portal as a deployment that waits in it until it is
# published, and waits until the Portal has validated it. `mise run release` runs it, before it
# tags, and publishes the deployment once the tag is on Entire.
#
#   bash scripts/central-upload.sh <bundle.zip> <deployment name>
#
# The Central Portal user token is MOUNTAYA_CENTRAL_USERNAME and MOUNTAYA_CENTRAL_PASSWORD, its two
# halves, read from the environment alone. RELEASE_PORTAL_URL names another Portal than
# https://central.sonatype.com, such as a stand-in a test answers from, RELEASE_POLL_SECONDS how
# long to wait between two asks (10), and RELEASE_PORTAL_WAIT_SECONDS how long to wait at most for
# the validation (1800).
#
# It prints the deployment's id on its standard output once the Portal has validated it, and fails,
# printing the Portal's own errors, where the Portal refuses it. It never publishes: the deployment
# is uploaded as USER_MANAGED, so it stays VALIDATED until the release publishes it.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"
require_cmd curl "curl, which comes with the operating system"
require_cmd base64 "base64, which comes with the operating system"

[ "$#" -eq 2 ] || die "The upload takes the bundle and the deployment's name." \
  "bash scripts/central-upload.sh <bundle.zip> <deployment name>"
bundle="$1"
name="$2"
[ -f "$bundle" ] || die "$bundle is no file, so there is nothing to upload." \
  "Make the bundle first: mise run release <version>, which writes build/release/central-bundle.zip"
printf '%s\n' "$name" | grep -Eq '^[A-Za-z0-9._-]+$' || die "\`$name\` is no deployment name: letters, digits, dots, dashes and underscores only." \
  "Name it as the release does: sdk-kotlin-<version>"
[ -n "${MOUNTAYA_CENTRAL_USERNAME:-}" ] && [ -n "${MOUNTAYA_CENTRAL_PASSWORD:-}" ] || die \
  "MOUNTAYA_CENTRAL_USERNAME and MOUNTAYA_CENTRAL_PASSWORD do not both hold the Central Portal user token." \
  "Generate one at https://central.sonatype.com/usertoken and export its two halves."

portal="${RELEASE_PORTAL_URL:-https://central.sonatype.com}"
portal="${portal%/}"
poll="${RELEASE_POLL_SECONDS:-10}"

umask 077
answer="$(mktemp)"
trap 'rm -f "$answer"' EXIT
token="$(printf '%s:%s' "$MOUNTAYA_CENTRAL_USERNAME" "$MOUNTAYA_CENTRAL_PASSWORD" | base64 | tr -d '\n')"

# portal <curl argument>...: one call to the Portal, printing the HTTP status (000 where nothing
# answered). The token travels in a header curl reads from its standard input (`--config -`),
# never on a command line and never in a file.
portal() {
  printf 'header = "Authorization: Bearer %s"\n' "$token" | curl -sS --config - -o "$answer" -w '%{http_code}' "$@" || true
}

info "Uploading $bundle to $portal as $name, a deployment the release publishes once its tag is on Entire." >&2
code="$(portal --max-time 900 -F "bundle=@$bundle;type=application/octet-stream" \
  "$portal/api/v1/publisher/upload?publishingType=USER_MANAGED&name=$name")"
if [ "$code" != "201" ]; then
  die "The Central Portal refused the upload, answering ${code:-nothing}: $(cat "$answer")" \
    "A 401 or a 403 is a token it does not take: generate another at https://central.sonatype.com/usertoken."
fi
deployment="$(tr -d '"[:space:]' <"$answer")"
[ -n "$deployment" ] || die "The Central Portal took the upload and named no deployment." \
  "Find it at $portal/publishing/deployments."

# The Portal validates a deployment in a few minutes; it is waited on for half an hour.
limit="${RELEASE_PORTAL_WAIT_SECONDS:-1800}"
deadline=$(($(date +%s) + limit))
state=""
while :; do
  code="$(portal --max-time 60 -X POST "$portal/api/v1/publisher/status?id=$deployment")"
  if [ "$code" = "200" ]; then
    state="$(sed -n 's/.*"deploymentState"[[:space:]]*:[[:space:]]*"\([A-Z_]*\)".*/\1/p;' "$answer" | sed -n 1p)"
    case "$state" in
      VALIDATED | PUBLISHING | PUBLISHED)
        ok "Deployment $deployment is $state." >&2
        echo "$deployment"
        exit 0
        ;;
      FAILED)
        die "The Central Portal refused deployment $deployment: $(cat "$answer")" \
          "Fix what it names, drop the deployment at $portal/publishing/deployments, then run the release again."
        ;;
    esac
  fi
  if [ "$(date +%s)" -ge "$deadline" ]; then
    die "Deployment $deployment is still ${state:-unanswered} after $limit seconds." \
      "Follow it at $portal/publishing/deployments."
  fi
  info "Deployment $deployment is ${state:-not yet answered}; asking again in $poll seconds." >&2
  sleep "$poll"
done
