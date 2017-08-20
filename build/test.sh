
gradle --stacktrace --continue clean testFdroidDebugUnitTest 2>&1 >/dev/null &

while ps aux | grep gradle; do
    sleep 1
done
