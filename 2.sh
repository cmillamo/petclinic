#!/usr/bin/env bash
# Facts for experiment 2. Run through ./facts, which fetches this script.
lib="$(mktemp -d)"
trap 'rm -rf "$lib"' EXIT
git show origin/facts:lib.py > "$lib/lib.py"
PYTHONPATH="$lib" python3 - "$@" <<'PY'
from lib import *
ref = base(None)
prod = changed(ref, "src/main")
line("production files changed", len(prod))
line("test methods added", len(test_methods(ref)))
line("last test run", last_test_run())
print()
print("For the poll: did the agent ask you anything before it wrote code?")
PY
