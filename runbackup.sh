#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Two arguments required."
else
    cd classes
    java cs5204.fs.master.MasterLauncher --backup $@
fi
