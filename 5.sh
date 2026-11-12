#!/usr/bin/env bash
# Facts for experiment 5. Run through ./facts, which fetches this script.
lib="$(mktemp -d)"
trap 'rm -rf "$lib"' EXIT
git show origin/facts:lib.py > "$lib/lib.py"
PYTHONPATH="$lib" python3 - "$@" <<'PY'
from lib import *
ref = base(5)
methods = test_methods(ref)
msgs = sorted(glob.glob("src/main/resources/messages/messages*.properties"))
aged = [m for m in msgs if re.search(r"^age\b", open(m, encoding="utf-8", errors="replace").read(), re.M | re.I)]
line("last test run", last_test_run())
line("language files with an age text", f"{len(aged)} of {len(msgs)}")
line("test methods added", len(methods))
for n, _ in methods:
    print(f"    {sentence(n)}")
print()
print("For the poll: how many of the six scenarios does a test above name?")
PY
