package archive.extension.relocate

import groovy.transform.CompileStatic
import archive.extension.CallableExtension
import org.gradle.api.Project

@CompileStatic
class RelocationExtension extends CallableExtension {
    public List<Relocation> configurations = []

    private final Project project

    RelocationExtension(Project project) {
        this.project = project
    }

    @Override
    Object configure(Closure closure) {
        def configuration = new Relocation()
        def result = configure(configuration, closure)
        this.initialize(configuration)
        this.configurations << configuration

        return result
    }

    @Override
    Object call() {this.configure {}}

    Details call(String path) {
        for (def configuration : this.configurations) {
            def result = configuration.projector.apply(path)

            if (result in [Type.INCLUDE, Type.FLATTEN]) {
                def details = new Details()
                details.type = result
                details.to = configuration.to

                return details
            }
        }

        return null
    }

    protected void initialize(Relocation configuration) {
        if (configuration.to === null) {
            configuration.to = this.project.group
        }

        if (!configuration.projector) {
            configuration.include {true}
        }
    }
}
