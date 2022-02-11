package net.auoeke.gronk;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;

public class Gronk implements Plugin<Project> {
    @Override public void apply(Project project) {
        // Add the main extension.
        var extension = project.getExtensions().create("gronk", GronkExtension.class, project);

        Util.tryAddExtension(project, "systemProperty", Util.<String, String>functionClosure(System::getProperty));

        Util.javaExtension(project, java -> {
            // Add javaVersion to the project.
            Util.tryAddExtension(project, "javaVersion", Util.actionClosure(extension::javaVersion));

            // Set up source sets.
            var sets = java.getSourceSets();
            var main = sets.getByName("main");
            main.getJava().srcDir("source");
            main.resources(resources -> resources.srcDir("resources"));
            var test = sets.getByName("test").getJava();
            test.setSrcDirs(List.of(project.file("test/source").exists() ? "test/source" : "test"));

            // Configure Kotlin from a Gradle script because its classes can't be loaded here for some reason.
            project.apply(configuration -> configuration.from(Gronk.class.getResource("kotlin.gradle")));
        });

        // Add the Maven repository extension.
        MavenRepositoryExtension.inject(project, project.getRepositories());

        // Use the fallback (latest) version for dependencies without required versions.
        project.getConfigurations().all(configuration -> configuration.getDependencies().withType(ExternalDependency.class, dependency -> {
            if (dependency.getVersion() == null || dependency.getVersion().isEmpty()) {
                dependency.version(constraint -> constraint.prefer(extension.fallbackVersion));
            }
        }));

        Util.whenExtensionPresent(project, PublishingExtension.class, publish -> {
            // Generate a source JAR if a publishing plugin and the Java plugin are present.
            Util.javaExtension(project, JavaPluginExtension::withSourcesJar);

            // Add the Maven repository extension.
            MavenRepositoryExtension.inject(project, publish.getRepositories());

            project.afterEvaluate(p -> p.getPluginManager().withPlugin("maven-publish", plugin -> publish.publications(publications -> {
                // Ensure that a publication exists if the Maven publishing plugin is applied and the group is not empty.
                if (publications.isEmpty() && !project.getGroup().toString().isEmpty()) {
                    publications.register("maven", MavenPublication.class, publication -> configure(project.getComponents(), "java", publication::from));
                }

                // Expose only resolved versions of dependencies instead of the declared versions in publications.
                publications.withType(MavenPublication.class, publication -> publication.versionMapping(strategy -> strategy.allVariants(VariantVersionMappingStrategy::fromResolutionResult)));
            })));
        });
    }

    private static <T> void configure(NamedDomainObjectCollection<T> collection, String name, Consumer<T> configure) {
        Optional.ofNullable(collection.findByName(name)).ifPresent(configure);
    }
}
