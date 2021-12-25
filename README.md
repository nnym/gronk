This plugin
- reduces source tree depth by moving source directories to the top level;
- simplifies adding Maven repositories;
- sets `latest.release` as the preferred version for dependencies that do not have a required version specified;
- adds a default Maven publication to an empty publication container from the Java software component; and
- configures all Maven publications to contain the versions of dependencies that were resolved during the build
instead of the declared versions in order to allow myself to use dynamic versions
while exposing to dependent projects only the versions of dependencies that are known to work.

See the test case [`fat`](./test/cases/fat/build.gradle).
