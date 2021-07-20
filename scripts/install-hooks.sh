#!/bin/bash

HOOK_NAMES="pre-commit"
# assuming the script is in a bin directory, one level into the repo
REPO_DIR=$(git rev-parse --show-toplevel)

HOOK_DIR=$REPO_DIR/.git/hooks

for hook in $HOOK_NAMES; do
    # If the hook already exists, and is not a symlink
    if [ ! -h $HOOK_DIR/$hook -a -f $HOOK_DIR/$hook ]; then
        mv $HOOK_DIR/$hook $HOOK_DIR/$hook.local
    fi
    # create the symlink, overwriting the file if it exists
    # probably the only way this would happen is if you're using an old version of git
    # -- back when the sample hooks were not executable, instead of being named ____.sample
    ln -s -f $REPO_DIR/scripts/git-hooks/$hook $HOOK_DIR/$hook
    chmod +x $REPO_DIR/scripts/git-hooks/$hook
done