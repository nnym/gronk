This Gradle project plugin
- adds extension `gronk`
- adds extension `javaVersion` that sets `sourceCompatibility` and `targetCompatibility`
- adds extension `systemProperty` that does what it purports to do
- adds Shadow JAR transformer `ManifestMerger`
- reduces source tree depth by moving source roots to the project's directory
- sets Kotlin compilation tasks' JVM target version to `JavaPluginExtension::getTargetCompatibility`
- simplifies adding Maven repositories:
  - allows URL to be specified as the first argument to `RepositoryHandler::maven`
  - allows leading `https://` to be omitted
  - adds extension closures `username` and `password` mapped to `PasswordCredentials::set{Username,Password}`
- sets `+` as the preferred (fallback) version for dependencies that do not have a version specified
- applies `JavaPluginExtension::withSourcesJar` when the Java plugin is present
- adds a default (signed with signing plugin present) Maven publication to an empty publication container from the Java software component
- adds extension `url` and fills in the POM fields `name`, `description` and `url` in all Maven publications from the project
- configures all Maven publications to contain the versions of dependencies that were resolved during the build
instead of the declared versions in order to allow myself to use dynamic versions
while exposing to dependent projects only the versions of dependencies that are known to work.

See the test case [`fat`](test/cases/fat/build.gradle).
