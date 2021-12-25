package net.auoeke.gronk;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

public class GronkExtension {
    public String fallbackVersion = "+";

    private final Project project;

    public GronkExtension(Project project) {
        this.project = project;
    }

    public void fallbackVersion(String version) {
        this.fallbackVersion = version;
    }

    public void javaVersion(Object version) {
        Util.whenExtensionPresent(this.project, JavaPluginExtension.class, extension -> {
            extension.setSourceCompatibility(version);
            extension.setTargetCompatibility(version);
        });
    }
}
