#!/bin/bash

function configure_hook {

    commit_msg_link=.git/hooks/$1

    # Create symbolic link if needed
    if [[ ! -L $commit_msg_link ]];
    then

        if [[ -f $commit_msg_link ]];
        then
            rm $commit_msg_link
        fi

        current_dir=$(pwd)
        ln -s "${current_dir}/scripts/git/$1" $commit_msg_link

        chmod +x "${current_dir}/scripts/git/$1"

    fi
}

configure_hook "pre-commit"
configure_hook "commit-msg"
