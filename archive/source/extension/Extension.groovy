package archive.extension

import groovy.transform.CompileStatic
import org.gradle.api.Project

@CompileStatic
class Extension {
    private final Project project

    Extension(Project project) {
        this.project = project
    }
}
