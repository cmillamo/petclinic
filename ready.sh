#!/usr/bin/env bash
# The check on experiment 1's first slide. Run it in the sandbox, in a shell:
#
#   git fetch -q && git show origin/facts:ready.sh | bash
#
# It sets Claude Code's settings for the day in ~/.claude/settings.json and
# keeps every other setting there:
#
#   tui: "fullscreen"               With no renderer set, Claude Code 2.1.281
#                                   offers the fullscreen renderer in the middle
#                                   of a turn, and answering the offer ends the
#                                   turn.
#   respondToBashCommands: false    The record slides run !./facts N at Claude
#                                   Code's prompt. By default the agent then
#                                   answers the poll's question itself, before
#                                   the attendee votes.
#   syncClaudeAiSkills: false       The sign-in brings the skills, plugins and
#   syncClaudeAiPlugins: false      connectors enabled on the attendee's
#   disableClaudeAiConnectors: true claude.ai account into the container, so
#                                   each agent would see different things.
#   autoMemoryEnabled: false        Nothing one session learns reaches the
#   autoDreamEnabled: false         next: each experiment starts a new session.
#   permissions.defaultMode:        File edits need no answer, and ./mvnw is
#     "acceptEdits"                 allowed. In the default mode the agent asks
#   permissions.allow +=            before its first file write and some shell
#     "Bash(./mvnw:*)"              commands; a headless run on 19 November
#                                   stopped at the first refused write. The
#                                   account's own mode varies by attendee.
#
# Then it prints the first request, so the room can see that the facts branch
# is there.

set -eu

settings="$HOME/.claude/settings.json"
mkdir -p "$HOME/.claude"
if [ -s "$settings" ]; then current="$(cat "$settings")"; else current='{}'; fi

if ! updated="$(printf '%s\n' "$current" | jq '
    .tui = "fullscreen"
  | .respondToBashCommands = false
  | .syncClaudeAiSkills = false
  | .syncClaudeAiPlugins = false
  | .disableClaudeAiConnectors = true
  | .autoMemoryEnabled = false
  | .autoDreamEnabled = false
  | .permissions.defaultMode = "acceptEdits"
  | .permissions.allow = ((.permissions.allow // []) + ["Bash(./mvnw:*)"] | unique)')"; then
  echo "ready: cannot update $settings. Put your hand up." >&2
  exit 1
fi
printf '%s\n' "$updated" > "$settings"

echo "Claude Code renderer: fullscreen"
echo "Claude Code after a ! command: no reply"
echo "Account skills, plugins, connectors, memory: off"
echo "Claude Code permissions: edits and ./mvnw allowed"
git show origin/facts:ask/1
