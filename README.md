This Gradle project plugin

- adds extension `gronk`
- adds extension `javaVersion` that sets `sourceCompatibility` and `targetCompatibility`
- adds extension `systemProperty` that does what it purports to do
- extends `Project::file` to join path component arrays
- adds Shadow JAR transformer `ManifestMerger`
- reduces source tree depth by moving source roots to the project's directory
- simplifies adding Maven repositories:
  - allows URL to be specified as the first argument to `RepositoryHandler::maven`
  - allows leading `https://` to be omitted
  - adds extension closures `username` and `password` mapped to `PasswordCredentials::set{Username,Password}`
- sets `latest.release` as the required (fallback) version for dependencies that do not have a version specified
- applies `JavaPluginExtension::withSourcesJar` when the Java plugin is present
- adds a default Maven publication to an empty publication container from the Java software component
- adds extension `url` and fills in the POM fields `name`, `description` and `url` in all Maven publications from the project
- configures all Maven publications to have their dependencies' dynamic versions resolved to concrete versions
- expands flat packages in source JARs
- (when the signing plugin is applied) signs all publications.

See the test case [`fat`](test/cases/fat/build.gradle).
