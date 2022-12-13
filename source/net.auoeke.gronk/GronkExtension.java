package net.auoeke.gronk;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import net.auoeke.reflect.Pointer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.JavadocOptionFileOption;
import org.gradle.external.javadoc.internal.JavadocOptionFile;

import static net.auoeke.dycon.Dycon.*;

public class GronkExtension {
	public String fallbackVersion = "latest.release";
	public String url;

	private final Project project;

	public GronkExtension(Project project) {
		this.project = project;
	}

	public void url(String url) {
		this.url = url;
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
}
