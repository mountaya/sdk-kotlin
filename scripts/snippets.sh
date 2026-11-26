#!/usr/bin/env bash
# Compiles the ```kotlin fences of the Markdown documents it is handed, a documentation site's pages among them, against
# the modules' public API, for the jvm and Android, warnings as errors, the way the consumer build compiles the README's.
#
# USAGE-START
# mise run snippets -- <file.md> [<file.md>...]
#
# Every ```kotlin fence of each document is compiled, wherever it stands in it, inside a list item or a {% code-block %}
# tag included. A fence opts out with `<!-- kotlin:skip <reason> -->` on the line directly above it. Each fence is
# printed into a file of its own under consumer/build/generated/snippets, never committed: common code, the jvm's
# where it imports what the jvm alone declares or reads `System`, or Android's where it imports Android, AndroidX or
# a composable, or reads an app's `BuildConfig`. A fence of statements is a function's body; a fence of declarations,
# a composable and its activity, is an object's members, which read one another in any order. A path is read from the
# directory the command is run in.
#
# It prints how many fences compiled and how many were skipped, and exits non-zero naming each failing fence's
# document and line, with the compiler's message. Gradle's own log is kept at build/snippets/gradle.log.
# USAGE-END
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"
require_pinned java
require_cmd awk "awk, which comes with the operating system"

if [ "$#" -eq 0 ]; then
  die "Name the Markdown documents whose Kotlin fences to compile." \
    "mise run snippets -- <file.md> [<file.md>...]"
fi

# Paths are read from where the command was run, which mise hands a task as MISE_ORIGINAL_CWD.
base="${MISE_ORIGINAL_CWD:-$PWD}"
root="$(cd "$(dirname "$0")/.." && pwd)"
documents=""
for path in "$@"; do
  case "$path" in
    /*) absolute="$path" ;;
    *) absolute="$base/$path" ;;
  esac
  if [ ! -f "$absolute" ]; then
    die "$path is not a file, read from $base." "mise run snippets -- <file.md> [<file.md>...]"
  fi
  absolute="$(cd "$(dirname "$absolute")" && pwd)/$(basename "$absolute")"
  documents="${documents:+$documents:}$absolute"
done

log="$root/build/snippets/gradle.log"
index="$root/consumer/build/generated/snippets/fences.tsv"
mkdir -p "$(dirname "$log")"
rm -f "$index"

cd "$root"
set +e
./gradlew checkDocSnippets --console=plain --continue \
  -Pmountaya.targets=jvm,android \
  "-Pmountaya.snippets=$documents" \
  "-Pmountaya.snippetsFrom=$base" 2>&1 | tee "$log"
status="${PIPESTATUS[0]}"
set -e

if [ "$status" -eq 0 ]; then
  ok "Every Kotlin fence compiled, as the tally above says."
  exit 0
fi

if [ ! -f "$index" ]; then
  die "The fences were not printed: Gradle's message above says why." "Gradle's log: $log"
fi

# Each compiler message about a printed fence, `e: file:///…/Snippet3.kt:12:9 message`, named by its document's line,
# or, with `fences`, each failing fence once, by its document and the line it opens on.
mapped() {
  awk -F '\t' -v mode="$1" '
    FNR == NR { start[$1] = $2; document[$1] = $3; fence[$1] = $4; first[$1] = $5; size[$1] = $6; next }
    /^[ew]: / {
      kind = substr($0, 1, 1)
      rest = substr($0, 4)
      sub(/^file:\/\//, "", rest)
      if (!match(rest, /\.kt:[0-9]+:[0-9]+/)) next
      file = substr(rest, 1, RSTART + 2)
      if (!(file in start)) next
      split(substr(rest, RSTART + 4, RLENGTH - 4), where, ":")
      message = substr(rest, RSTART + RLENGTH + 1)
      offset = where[1] - start[file]
      at = (offset >= 0 && offset < size[file]) ? first[file] + offset : fence[file]
      failed[document[file] ":" fence[file]] = 1
      said = sprintf("%s:%d: %s: %s (the fence at line %d)", document[file], at, (kind == "e" ? "error" : "warning"), message, fence[file])
      # A fence of common code is compiled for each target, and its message told once.
      if (mode == "messages" && !(said in told)) { told[said] = 1; print said }
    }
    END { if (mode == "fences") for (name in failed) print name }
  ' "$index" "$log"
}

messages="$(mapped messages)"
if [ -z "$messages" ]; then
  die "The build failed outside the documents' fences: Gradle's message above says why." "Gradle's log: $log"
fi
fences="$(mapped fences | sort)"
count="$(echo "$fences" | wc -l | tr -d ' ')"

echo "" >&2
echo "$messages" >&2
echo "" >&2
echo "Failing fences, by the line each opens on:" >&2
while IFS= read -r fence; do echo "  $fence" >&2; done <<<"$fences"
die "Kotlin fences that failed to compile: $count, each named above by its document and line." \
  "Fix each fence, or mark one that cannot compile with <!-- kotlin:skip <reason> --> on the line above it" \
  "Gradle's log: $log"
