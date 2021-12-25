package net.auoeke.gronk;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class MavenRepositoryExtension extends Closure<Void> {
    private final Project project;

    public MavenRepositoryExtension(Project project, RepositoryHandler owner) {
        super(owner);

        this.project = project;
    }

    public MavenArtifactRepository doCall(Object url, Closure configure) {
        return this.repositories().maven(repository -> {
            repository.setUrl(url);
            this.project.configure(repository, configure);
        });
    }

    public MavenArtifactRepository doCall(Object url) {
        return this.repositories().maven(repository -> repository.setUrl(url));
    }

    private RepositoryHandler repositories() {
        return (RepositoryHandler) this.getOwner();
    }
}
