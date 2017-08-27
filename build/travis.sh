#!/usr/bin/env bash

for i in $(seq 16 25); do
    # Skip Android Wear
    if [[ "$i" == "20" ]]; then
        continue
    fi

    export ROBOLECTRIC_SDKS=$i
    echo "Running tests with SDK $ROBOLECTRIC_SDKS"

    gradle --stacktrace --continue testFdroidDebugUnitTest || exit
done
