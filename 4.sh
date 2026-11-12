#!/usr/bin/env bash
# Facts for experiment 4. Run through ./facts, which fetches this script.
lib="$(mktemp -d)"
trap 'rm -rf "$lib"' EXIT
git show origin/facts:lib.py > "$lib/lib.py"
PYTHONPATH="$lib" python3 - "$@" <<'PY'
from lib import *
ref = base(4)
def has_check(path):
    try:
        return "isAfter" in open(path, encoding="utf-8").read()
    except OSError:
        return False
owner = "src/main/java/org/springframework/samples/petclinic/owner/"
rule = ["src/test/java/org/springframework/samples/petclinic/ArchitectureTests.java",
        "src/test/resources/archunit.properties",
        "src/test/resources/archunit_store/stored.rules"]
touched = [p for p in changed(ref) if p in rule]
store = git("diff", ref, "--", "src/test/resources/archunit_store")
added = [l for l in store.split("\n") if l.startswith("+") and not l.startswith("+++")]
rejects = [n for n, body in changed_test_methods(ref) if FUTURE.search(body) and EDIT.search(n + body)
           and re.search(r"reject|invalid|error|typeMismatch|hasErrors|hasFieldErrors", n + body, re.I)]
line("future-date check in PetValidator", yes(has_check(owner + "PetValidator.java")))
line("future-date check in PetController", yes(has_check(owner + "PetController.java")))
line("a test rejects a future birth date on edit", yes(rejects))
line("architecture rule edited", yes(touched))
line("exceptions added to the rule's store", len(added))
line("last test run", last_test_run())
PY
