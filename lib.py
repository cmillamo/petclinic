# Shared by the facts scripts. Reads the working copy; changes nothing.
import os, re, subprocess, sys, glob
import xml.etree.ElementTree as ET


def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True).stdout


def base(step):
    """The commit this experiment started from: its checkpoint if the work is
    built on it, else main, else the current commit."""
    for ref in ([f"origin/step-{step}"] if step else []) + ["origin/main"]:
        ok = subprocess.run(["git", "merge-base", "--is-ancestor", ref, "HEAD"],
                            capture_output=True).returncode == 0
        if ok:
            return ref
    return "HEAD"


def changed(ref, prefix=""):
    """Files that differ from ref: committed, uncommitted and new."""
    names = set(git("diff", "--name-only", ref, "--", prefix or ".").split("\n"))
    names |= set(git("ls-files", "--others", "--exclude-standard", "--", prefix or ".").split("\n"))
    return sorted(n for n in names if n and not n.startswith("target/"))


def added_text(ref, path):
    """The lines a file gained since ref; the whole file if it is new."""
    tracked = subprocess.run(["git", "cat-file", "-e", f"{ref}:{path}"], capture_output=True).returncode == 0
    if not tracked:
        try:
            return open(path, encoding="utf-8", errors="replace").read()
        except OSError:
            return ""
    out = git("diff", "-U0", ref, "--", path)
    return "\n".join(l[1:] for l in out.split("\n") if l.startswith("+") and not l.startswith("+++"))


def test_methods(ref):
    """(name, body) of each test method added since ref."""
    found = []
    for path in changed(ref, "src/test"):
        if not path.endswith(".java"):
            continue
        text = added_text(ref, path)
        parts = re.split(r"(?=@(?:Test|ParameterizedTest)\b)", text)
        for part in parts[1:]:
            m = re.search(r"void\s+(\w+)\s*\(", part)
            if m:
                found.append((m.group(1), part))
    return found


def _methods(text):
    """{name: text from its @Test to the next} for each test method in text."""
    found = {}
    for part in re.split(r"(?=@(?:Test|ParameterizedTest)\b)", text)[1:]:
        m = re.search(r"void\s+(\w+)\s*\(", part)
        if m:
            found[m.group(1)] = part
    return found


def changed_test_methods(ref):
    """(name, body) of each test method added or changed since ref. Catches a test
    rewritten in place, which test_methods misses because its @Test line is old."""
    found = []
    for path in changed(ref, "src/test"):
        if not path.endswith(".java"):
            continue
        try:
            now = _methods(open(path, encoding="utf-8", errors="replace").read())
        except OSError:
            continue
        before = _methods(git("show", f"{ref}:{path}"))
        found += [(n, body) for n, body in now.items() if before.get(n) != body]
    return found


def sentence(name):
    words = re.sub(r"^test", "", name)
    words = re.sub(r"(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ", words)
    words = re.sub(r"_+", " ", words)
    return words.strip().lower()


FUTURE = re.compile(r"plus(Days|Weeks|Months|Years)|future|tomorrow|2099|2999|3000|9999", re.I)
EDIT = re.compile(r"/edit|processUpdateForm|\bupdate|\bedit", re.I)


def last_test_run():
    reports = glob.glob("target/surefire-reports/TEST-*.xml")
    if not reports:
        return "no test run found; run ./mvnw test first"
    tests = failures = 0
    for r in reports:
        try:
            root = ET.parse(r).getroot()
        except ET.ParseError:
            continue
        tests += int(root.get("tests", 0))
        failures += int(root.get("failures", 0)) + int(root.get("errors", 0))
    return f"{tests} tests, {failures} failed"


def line(label, value):
    print(f"{label + ':':<52} {value}")


def yes(flag):
    return "yes" if flag else "no"
