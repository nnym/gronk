This Gradle project plugin
- reduces source tree depth by moving source roots to the project's directory;
- simplifies adding Maven repositories;
- sets `+` as the preferred version for dependencies that do not have a required version specified;
- adds a default Maven publication to an empty publication container from the Java software component; and
- configures all Maven publications to contain the versions of dependencies that were resolved during the build
instead of the declared versions in order to allow myself to use dynamic versions
while exposing to dependent projects only the versions of dependencies that are known to work.

See the test case [`fat`](./test/cases/fat/build.gradle).

### license
I hereby grant you a worldwide non-exclusive perpetual irrevocable royalty-free license
to publicly use, perform, reproduce, display, distribute and prepare derivative works of this software
as long as you don't break the law and as long as you don't claim it as your own.
This license is propagated to every receiver of verbatim copies of this software.
Permission to relicense derivate works is granted.
