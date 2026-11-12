#!/usr/bin/env bash
# Facts for experiment 3. Run through ./facts, which fetches this script.
lib="$(mktemp -d)"
trap 'rm -rf "$lib"' EXIT
git show origin/facts:lib.py > "$lib/lib.py"
PYTHONPATH="$lib" python3 - "$@" <<'PY'
from lib import *
ref = base(3)
methods = test_methods(ref)
hits = [n for n, body in methods if FUTURE.search(body) and EDIT.search(n + body)]
suspect = [p for p in changed(ref, "src/test") if re.search(r"SUSPECT|GAP|BUG", added_text(ref, p))]
line("test methods added", len(methods))
line("a test edits a pet with a birth date after today", yes(hits))
for n in hits:
    print(f"    {sentence(n)}")
line("tests that mark something as suspect", len(suspect))
line("production files changed", len(changed(ref, "src/main")))
line("last test run", last_test_run())
PY
