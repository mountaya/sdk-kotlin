#!/usr/bin/env bash
# USAGE-START
# Uploads a release bundle to the Central Portal as a deployment you publish yourself, and waits
# until the Portal has validated it. `mise run release` runs it; run it alone to upload a bundle a
# release made and could not hand over.
#
#   bash scripts/central-upload.sh <bundle.zip> <deployment name>
#
# The Central Portal user token is MOUNTAYA_CENTRAL_USERNAME and MOUNTAYA_CENTRAL_PASSWORD, its two
# halves. RELEASE_PORTAL_URL names another Portal than https://central.sonatype.com, such as a
# stand-in a test answers from.
#
# It prints the deployment's id on its standard output once the Portal has validated it, and fails,
# printing the Portal's own errors, where the Portal refuses it. It never publishes: the deployment
# waits in the Portal until you publish it there, or drop it.
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

# The token travels in a header read from a file only this user reads, never on a command line.
umask 077
header="$(mktemp)"
answer="$(mktemp)"
trap 'rm -f "$header" "$answer"' EXIT
token="$(printf '%s:%s' "$MOUNTAYA_CENTRAL_USERNAME" "$MOUNTAYA_CENTRAL_PASSWORD" | base64 | tr -d '\n')"
printf 'Authorization: Bearer %s\n' "$token" >"$header"

info "Uploading $bundle to $portal as $name, a deployment you publish yourself." >&2
code="$(curl -sS --max-time 900 -o "$answer" -w '%{http_code}' -H "@$header" \
  -F "bundle=@$bundle;type=application/octet-stream" \
  "$portal/api/v1/publisher/upload?publishingType=USER_MANAGED&name=$name" || true)"
if [ "$code" != "201" ]; then
  die "The Central Portal refused the upload, answering ${code:-nothing}: $(cat "$answer")" \
    "A 401 is a token it does not know: generate another at https://central.sonatype.com/usertoken."
fi
deployment="$(tr -d '"[:space:]' <"$answer")"
[ -n "$deployment" ] || die "The Central Portal took the upload and named no deployment." \
  "Find it at $portal/publishing/deployments."

# The Portal validates a deployment in a few minutes; it is waited on for half an hour.
deadline=$(($(date +%s) + 1800))
state=""
while :; do
  code="$(curl -sS --max-time 60 -o "$answer" -w '%{http_code}' -X POST -H "@$header" \
    "$portal/api/v1/publisher/status?id=$deployment" || true)"
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
          "Fix what it names, drop the deployment at $portal/publishing/deployments, then upload again."
        ;;
    esac
  fi
  if [ "$(date +%s)" -ge "$deadline" ]; then
    die "Deployment $deployment is still ${state:-unanswered} after half an hour." \
      "Follow it at $portal/publishing/deployments."
  fi
  info "Deployment $deployment is ${state:-not yet answered}; asking again in 10 seconds." >&2
  sleep 10
done
