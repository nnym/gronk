package net.auoeke.gronk;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import net.auoeke.reflect.Accessor;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.jvm.tasks.Jar;
import org.gradle.plugins.signing.SigningExtension;

public class Gronk implements Plugin<Project> {
	@Override public void apply(Project project) {
		Accessor.putReference(((DefaultProject) project).getFileResolver(), "fileNotationParser", new PathArrayNotationParser());

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
			main.getResources().srcDir("resources");
			var test = sets.getByName("test").getJava();
			test.setSrcDirs(List.of(project.file("test/source").exists() ? "test/source" : "test"));

			sets.all(set -> {
				// Add extension `export` to source sets.
				Util.tryAddExtension(set, "export", Util.functionClosure(arguments ->
					InvokerHelper.invokeMethod(extension, "export", DefaultGroovyMethods.plus(new Object[]{set}, InvokerHelper.asArray(arguments)))
				));

				project.getTasks()
					.withType(Jar.class)
					.matching(task -> task.getName().equals(set.getSourcesJarTaskName()))
					.all(task -> task.eachFile(file -> file.setPath(file.getPath().replaceAll("\\.(?=.*/)", "/"))));
			});

			Util.whenPluginPresent(project, "com.github.johnrengelman.shadow", plugin -> {
				project.getTasks().withType(ShadowJar.class, ManifestMergerExtension::inject);
				project.getExtensions().add(Class.class, "ManifestMerger", ManifestMerger.class);
			});
		});

		// Add the Maven repository extension.
		MavenRepositoryExtension.inject(project, project.getRepositories());

		// Use the fallback (latest) version for dependencies without required versions.
		project.getConfigurations().all(configuration -> configuration.getDependencies().withType(ExternalDependency.class, dependency -> {
			if (dependency.getVersion() == null || dependency.getVersion().isEmpty()) {
				dependency.version(constraint -> constraint.require(extension.fallbackVersion));
			}
		}));

		Util.whenExtensionPresent(project, PublishingExtension.class, publish -> {
			// Generate a source JAR if a publishing plugin and the Java plugin are present.
			Util.javaExtension(project, JavaPluginExtension::withSourcesJar);

			// Add the Maven repository extension.
			MavenRepositoryExtension.inject(project, publish.getRepositories());

			project.afterEvaluate(p -> publish.publications(publications -> {
				// Ensure that a publication exists if the Maven publishing plugin is applied and the group is not empty.
				if (publications.isEmpty() && !project.getGroup().toString().isEmpty()) {
					publications.register("maven", MavenPublication.class, pub -> configure(project.getComponents(), "java", pub::from));
				}

				publications.withType(MavenPublication.class, publication -> {
					// Expose only resolved versions of dependencies instead of the declared versions in publications.
					publication.versionMapping(strategy -> strategy.allVariants(VariantVersionMappingStrategy::fromResolutionResult));

					if (!project.getPluginManager().hasPlugin("com.gradle.plugin-publish")) {
						// Sign publications if the signing plugin is applied.
						Util.whenExtensionPresent(project, SigningExtension.class, signing -> signing.sign(publication));
					}
				});
			}));
		});
	}

	private static <T> void configure(NamedDomainObjectCollection<T> collection, String name, Consumer<T> configure) {
		Optional.ofNullable(collection.findByName(name)).ifPresent(configure);
	}
}
