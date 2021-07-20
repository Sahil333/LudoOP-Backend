#!/bin/bash

HOOK_NAMES="pre-commit"
# assuming the script is in a bin directory, one level into the repo
REPO_DIR=$(git rev-parse --show-toplevel)

HOOK_DIR=$REPO_DIR/.git/hooks

for hook in $HOOK_NAMES; do
    # rm symlink
    rm $HOOK_DIR/$hook

    # If the hook.local exists, mv it back
    if [ -f $HOOK_DIR/$hook.local ]; then
        mv $HOOK_DIR/$hook.local $HOOK_DIR/$hook
    fi

done