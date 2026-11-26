#!/usr/bin/env bash
# Stops the gate before it starts where the disk it builds on lacks room for it. A build that runs
# the disk out of space fails part-way, can leave a file it was writing cut short, and stops every
# other program on the machine that writes.
#
#   bash scripts/check-disk.sh
#
# A first gate fills Gradle's caches, Kotlin/Native's toolchain and every module's build directory,
# about 10 GB, so it asks for 25 GB free where the root `build/` does not exist yet. A later gate
# rewrites what it built in place and asks for 15 GB. MOUNTAYA_MIN_FREE_GB, when set, asks for that
# many instead. Under CI the runner is sized for the gate, and the check passes saying so.
set -euo pipefail

# shellcheck source=lib/common.sh
. "$(dirname "$0")/lib/common.sh"
answer_help "$@"

if [ -n "${CI:-}" ]; then
  ok "CI: the runner is sized for the gate, so the free space is not checked"
  exit 0
fi

if [ -n "${MOUNTAYA_MIN_FREE_GB:-}" ]; then
  need="$MOUNTAYA_MIN_FREE_GB"
elif [ -d build ]; then
  need=15
else
  need=25
fi

free_gb="$(df -Pk . | awk 'NR == 2 { printf "%d", $4 / 1048576 }')"

if [ "$free_gb" -lt "$need" ]; then
  die "The disk this repository builds on has ${free_gb} GB free, and the gate asks for ${need} GB." \
    "mise run clean -- --dry-run          # what this repository's builds left behind and no longer use" \
    "mise run clean -- --all              # this repository's build directories too, rebuilt by the next gate" \
    "mise run clean:toolchains            # yours to run, never an agent's: the shared toolchains no checkout pins, listed; -- --yes deletes them" \
    "MOUNTAYA_MIN_FREE_GB=<n> mise run ci # ask for less, knowing a full disk stops the build part-way"
fi
ok "${free_gb} GB free on the disk this repository builds on; the gate asks for ${need} GB"
