---
name: pin-behavior
description: Use before changing existing code, when asked to pin or characterize what it does today.
---

# Pin current behavior before changing code

A pin is a test that records what the code does today, so a later change that alters it fails loudly. Pins describe the code as it is, odd parts included. They are not a statement of what the code should do.

## Steps

1. **List the code the change will touch, and the data it reads.** Name the methods the change will edit. Then, for every value those methods read, follow it back to each place it enters the system: every form, every handler, every validator, the schema. Those places are in scope too.
2. **List every rule and outcome in that code.** For each rule, note the normal case, the boundary, and what happens to bad input. Where two paths handle the same value, compare them rule by rule.
3. **Run the existing tests that cover this code, and name them.** Pin only what they leave uncovered.
4. **Write the pins in a new test class** named after the subject. Name each pin as a sentence that states what the code does today. Leave existing tests unchanged.
5. **Take each expected value from a run.** Write your best guess, run the test, and if the guess was wrong, copy the actual value into the assertion.
6. **Mark what looks wrong.** If a pin records behavior you believe is a bug, or two paths that disagree, keep the pin and add a comment starting `SUSPECT:` that says why.
7. **Prove one pin per rule can fail.** Break the rule once, see the pin fail, undo the change, and check with `git diff` that production code is back as it was.
8. **Run the whole suite on the unchanged code.** Everything is green.

## Reply with

1. The code and the entry points you covered.
2. One line per pin: what the code does, and the test's name.
3. Every `SUSPECT:` pin, first, with the reason.
4. The suite's result.
