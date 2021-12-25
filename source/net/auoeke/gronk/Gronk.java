package net.auoeke.gronk;

import java.util.Optional;
import java.util.function.Consumer;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;

public class Gronk implements Plugin<Project> {
    private static <T> void configure(Project project, Class<T> extension, Consumer<T> configure) {
        project.afterEvaluate(p -> Optional.ofNullable(p.getExtensions().findByType(extension)).ifPresent(configure));
    }

    private static <T> void configure(NamedDomainObjectCollection<T> collection, String name, Consumer<T> configure) {
        Optional.ofNullable(collection.findByName(name)).ifPresent(configure);
    }

    @Override
    public void apply(Project project) {
        // Set up source sets.
        configure(project, JavaPluginExtension.class, extension -> {
            var sets = extension.getSourceSets();
            var main = sets.getByName("main");
            main.getJava().srcDir("source");
            main.resources(resources -> resources.srcDir("resources"));

            var test = sets.getByName("test");
            var source = project.file("test/source");
            var testJava = test.getJava();

            if (source.exists()) {
                testJava.srcDir(source);
            } else {
                testJava.srcDir("test");
            }
        });

        // Add the Maven repository extension.
        var repositories = (ExtensionAware) project.getRepositories();
        repositories.getExtensions().create("maven", MavenRepositoryExtension.class, project, repositories);

        // Fall back to latest.release for dependencies without required versions.
        project.getConfigurations().all(configuration -> configuration.getDependencies().withType(ExternalDependency.class).all(dependency ->
            dependency.version(constraint -> constraint.prefer("latest.release"))
        ));

        configure(project, PublishingExtension.class, publish -> publish.publications(publications -> {
            if (project.getPluginManager().hasPlugin("maven-publish")) {
                // Ensure that a publication exists if the Maven publishing plugin is applied and the group is not empty.
                if (publications.isEmpty() && !project.getGroup().toString().isEmpty()) {
                    publications.register("maven", MavenPublication.class, publication -> {
                        configure(project.getComponents(), "java", publication::from);
                    });
                }

                // Expose only resolved versions of dependencies in publications instead of the declared versions.
                publications.withType(MavenPublication.class).all(publication ->
                    publication.versionMapping(strategy -> strategy.allVariants(VariantVersionMappingStrategy::fromResolutionResult))
                );
            }
        }));
    }
}
