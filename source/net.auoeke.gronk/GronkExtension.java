package net.auoeke.gronk;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import net.auoeke.reflect.Pointer;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.internal.JavadocOptionFile;
import org.gradle.plugins.signing.SigningPlugin;

import static net.auoeke.dycon.Dycon.*;

public class GronkExtension {
	public String fallbackVersion = "latest.release";

	/*
	url.set("https://github.com/auoeke/reflect")

	licenses {
		license {
			url.set("https://github.com/auoeke/reflect/blob/master/LICENSE.md")
		}
	}

	developers {
		developer {
			email.set("tjmnkrajyej@gmail.com")
		}
	}

	scm {
		url.set("https://github.com/auoeke/reflect/tree/master")
	}
	*/

	private final Project project;
	private PublishingConfiguration publishing;

	public GronkExtension(Project project) {
		this.project = project;
	}

	public void fallbackVersion(String version) {
		this.fallbackVersion = version;
	}

	public void javaVersion(Object version) {
		Util.javaExtension(this.project, extension -> {
			extension.setSourceCompatibility(version);
			extension.setTargetCompatibility(version);
		});
	}

	public void uncheck() {
		this.project.afterEvaluate(project -> Util.javaExtension(project, java -> {
			var names = new HashSet<>();
			project.getConfigurations().all(configuration -> names.add(configuration.getName()));
			project.getRepositories().mavenCentral();

			java.getSourceSets()
				.matching(set -> names.contains(set.getAnnotationProcessorConfigurationName()))
				.all(set -> project.getDependencies().add(set.getAnnotationProcessorConfigurationName(), "net.auoeke:uncheck"));
		}));
	}

	public void export(SourceSet set, String modulePackage, String otherModule) {
		if (otherModule == null) {
			otherModule = "ALL-UNNAMED";
		}

		var compileJava = set.getCompileJavaTaskName();
		var value = modulePackage + '=' + otherModule;

		this.project.getTasks().matching(task -> task.getName().equals(compileJava)).all(task -> {
			var arguments = ((JavaCompile) task).getOptions().getCompilerArgs();
			arguments.add("--add-exports");
			arguments.add(value);
		});

		var javadoc = set.getJavadocTaskName();

		this.project.getTasks().matching(task -> task.getName().equals(javadoc)).all(task -> {
			var options = (CoreJavadocOptions) ((Javadoc) task).getOptions();
			var optionFile = ldc(() -> Pointer.of(CoreJavadocOptions.class, "optionFile")).<JavadocOptionFile>getT(options);
			var option = Objects.requireNonNullElseGet((JavadocOptionFileOption<List<String>>) optionFile.getOptions().get("-add-exports"), () -> options.addMultilineStringsOption("-add-exports"));
			option.getValue().add(value);
		});
	}

	public void export(SourceSet set, String modulePackage) {
		this.export(set, modulePackage, null);
	}

	public void export(SourceSet set, Iterable<String> modulesPackages, String otherModule) {
		modulesPackages.forEach(modulePackage -> this.export(set, modulePackage, otherModule));
	}

	public void export(SourceSet set, Iterable<String> modulesPackages) {
		modulesPackages.forEach(modulePackage -> this.export(set, modulePackage, null));
	}

	public void publish(Action<? super PublishingConfiguration> configure) {
		if (this.publishing == null) {
			this.publishing = new PublishingConfiguration();
			var plugins = this.project.getPluginManager();
			plugins.apply(MavenPublishPlugin.class);
			plugins.apply(SigningPlugin.class);
			Util.javaExtension(this.project, JavaPluginExtension::withJavadocJar);

			this.project.afterEvaluate(project -> {
				var extension = project.getExtensions().getByType(PublishingExtension.class);

				// Fill in some POM fields from the project and the PublishingExtension.
				extension.getPublications().withType(MavenPublication.class, publication -> publication.pom(pom -> {
					fallback(pom.getName(), project.getName());
					fallback(pom.getDescription(), project.getDescription());
					fallback(pom.getUrl(), this.publishing.url());
					add(this.publishing.license, value -> pom.licenses(licenses -> licenses.license(license -> license.getUrl().set(value))));
					add(this.publishing.email, value -> pom.developers(developers -> developers.developer(developer -> developer.getEmail().set(value))));
					add(this.publishing.scm, value -> pom.scm(scm -> scm.getUrl().set(value)));
				}));

				// Add default repositories.
				var repositories = extension.getRepositories();
				repositories.mavenLocal();

				var url = this.project.findProperty("maven.repository");

				if (url != null) {
					((ExtensionAware) repositories).getExtensions().getByType(MavenRepositoryExtension.class).doCall(url)
						.credentials(credentials -> {
							credentials.setUsername((String) this.project.findProperty("maven.username"));
							credentials.setPassword((String) this.project.findProperty("maven.password"));
						});
				}
			});
		}

		this.project.configure(List.of(this.publishing), configure);
	}

	private static <T> void fallback(Property<T> property, T value) {
		if (!property.isPresent()) {
			property.set(value);
		}
	}

	private static <T> void add(T value, Action<? super T> initializer) {
		if (value != null) {
			initializer.execute(value);
		}
	}
}
