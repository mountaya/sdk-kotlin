#!/usr/bin/env bash
# USAGE-START
# Writes a release of sdk-kotlin into its files: the version every module is published at, the
# catalog's `mountaya` in gradle/libs.versions.toml, the README's install lines, and CHANGELOG.md's
# `[Unreleased]` notes, moved under the version and today's date.
#
#   mise run version <X.Y.Z>
#
# It edits files alone, so an agent may run it, and run again it changes nothing. Commit what it
# wrote as "version: Release v<X.Y.Z>" and push main: `mise run release <X.Y.Z>` tags that commit,
# once CI passed on it. It refuses a CHANGELOG.md with no section for the version and nothing under
# `## [Unreleased]`.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
# shellcheck source=lib/release.sh
. "$(dirname "$0")/lib/release.sh"
answer_help "$@"
require_cmd awk "awk, which comes with the operating system"
require_cmd cmp "cmp, which comes with the operating system"
require_cmd sed "sed, which comes with the operating system"

root="$(cd "$(dirname "$0")/.." && pwd -P)"
cd "$root"

[ "$#" -le 1 ] || die "The version takes one argument, and was given $#." "mise run version <X.Y.Z>, such as: mise run version 0.2.0"
version="${1:-}"
release_is_version "$version" || die "\`$version\` is no release version: a release is MAJOR.MINOR.PATCH, each a number with no leading zero." \
  "mise run version <X.Y.Z>, such as: mise run version 0.2.0"

catalog="gradle/libs.versions.toml"
grep -Eq '^mountaya = "[^"]*"$' "$catalog" || die "$catalog states no \`mountaya = \"<version>\"\`, the version every module is published at." \
  "Restore that line under [versions], then run this again."
[ -f CHANGELOG.md ] || die "CHANGELOG.md is missing, so the release would say nothing of what it carries." \
  "Write it with an \`## [Unreleased]\` section holding the notes, then run this again."
if ! release_dated CHANGELOG.md "$version" \
  && [ -z "$(awk '/^## \[Unreleased\]/ { inside = 1; next } inside && /^## \[/ { exit } inside && NF { print; exit }' CHANGELOG.md)" ]; then
  die "CHANGELOG.md has no section for $version and nothing under \`## [Unreleased]\`, so the release would say nothing of what it carries." \
    "Write what $version carries under \`## [Unreleased]\`, then run this again."
fi

# write <file> <rewritten file>: the file replaced by its rewrite where the two differ, so a second
# run leaves every file as it found it.
write() {
  if cmp -s "$1" "$2"; then
    rm -f "$2"
  else
    cat "$2" >"$1"
    rm -f "$2"
  fi
}

rewritten="$(mktemp)"
sed -E 's/^mountaya = "[^"]*"$/mountaya = "'"$version"'"/' "$catalog" >"$rewritten"
write "$catalog" "$rewritten"

# Every install line names a module's coordinate and its version, `com.mountaya:mountaya-<module>:<version>`.
rewritten="$(mktemp)"
sed -E 's/(com\.mountaya:mountaya-[a-z-]+:)[^")[:space:]]+/\1'"$version"'/g' README.md >"$rewritten"
write README.md "$rewritten"

rewritten="$(mktemp)"
release_date_unreleased CHANGELOG.md "$version" >"$rewritten"
write CHANGELOG.md "$rewritten"

ok "sdk-kotlin is $version in $catalog, the README's install lines and CHANGELOG.md."
echo "Commit it as \"version: Release v$version\", push main, then, once CI passed: mise run release $version"
