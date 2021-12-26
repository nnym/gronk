package net.auoeke.gronk;

import org.gradle.api.Project;

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
        Util.javaExtension(this.project, extension -> {
            extension.setSourceCompatibility(version);
            extension.setTargetCompatibility(version);
        });
    }
}
