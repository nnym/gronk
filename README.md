This Gradle project plugin
- reduces source tree depth by moving source roots to the project's directory;
- sets Kotlin compilation tasks' JVM target version to `JavaPluginExtension#getTargetCompatibility`;
- simplifies adding Maven repositories;
- sets `+` as the preferred version for dependencies that do not have a required version specified;
- applies `JavaPluginExtension#withSourcesJar` when the Java plugin is present;
- adds a default Maven publication to an empty publication container from the Java software component; and
- configures all Maven publications to contain the versions of dependencies that were resolved during the build
instead of the declared versions in order to allow myself to use dynamic versions
while exposing to dependent projects only the versions of dependencies that are known to work.

See the test case [`fat`](./test/cases/fat/build.gradle).

### license
For the purpose of this license, "this software" refers to the files wherewith this license text is shipped.

Indeed it is I who hereby grants every receiver of a verbatim copy of this software a worldwide non-exclusive perpetual irrevocable royalty-free license
to publicly use, perform, reproduce, display, distribute, sublicense and prepare derivative works of this software
as long as you don't break the law and as long as you don't claim it as your own.
Derivate works may be relicensed under different terms.
