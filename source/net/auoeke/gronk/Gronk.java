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
    private static <T> void configure(NamedDomainObjectCollection<T> collection, String name, Consumer<T> configure) {
        Optional.ofNullable(collection.findByName(name)).ifPresent(configure);
    }

    @Override
    public void apply(Project project) {
        // Add the extension.
        var extension = project.getExtensions().create("gronk", GronkExtension.class, project);

        // Set up source sets.
        Util.javaExtension(project, java -> {
            var sets = java.getSourceSets();
            var main = sets.getByName("main");
            main.getJava().srcDir("source");
            main.resources(resources -> resources.srcDir("resources"));

            var test = sets.getByName("test");
            var testSource = project.file("test/source").exists() ? "test/source" : "test";
            test.getJava().srcDir(testSource);

            // Configure Kotlin from a Gradle script because its classes can't be loaded here for some reason.
            project.apply(configuration -> configuration.from(Gronk.class.getResource("kotlin.gradle")));
        });

        // Add the Maven repository extension.
        var repositories = (ExtensionAware) project.getRepositories();
        repositories.getExtensions().create("maven", MavenRepositoryExtension.class, project, repositories);

        // Use the fallback (latest) version for dependencies without required versions.
        project.getConfigurations().all(configuration -> configuration.getDependencies().withType(ExternalDependency.class).all(dependency ->
            dependency.version(constraint -> constraint.prefer(extension.fallbackVersion))
        ));

        Util.whenExtensionPresent(project, PublishingExtension.class, publish -> {
            // Generate a source JAR if a publishing plugin and the Java plugin are present.
            Util.javaExtension(project, JavaPluginExtension::withSourcesJar);

            project.afterEvaluate(p -> p.getPluginManager().withPlugin("maven-publish", plugin -> publish.publications(publications -> {
                // Ensure that a publication exists if the Maven publishing plugin is applied and the group is not empty.
                if (publications.isEmpty() && !project.getGroup().toString().isEmpty()) {
                    publications.register("maven", MavenPublication.class, publication -> configure(project.getComponents(), "java", publication::from));
                }

                // Expose only resolved versions of dependencies in publications instead of the declared versions.
                publications.withType(MavenPublication.class, publication -> publication.versionMapping(strategy -> strategy.allVariants(VariantVersionMappingStrategy::fromResolutionResult)));
            })));
        });
    }
}
