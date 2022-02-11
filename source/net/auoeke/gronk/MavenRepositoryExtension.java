package net.auoeke.gronk;

import java.io.File;
import java.net.URL;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;

@SuppressWarnings("unused")
public class MavenRepositoryExtension extends ClosureExtension<RepositoryHandler, MavenArtifactRepository> {
    private final Project project;

    public MavenRepositoryExtension(RepositoryHandler owner, Project project) {
        super(owner);

        this.project = project;
    }

    public static void inject(Project project, RepositoryHandler repositories) {
        inject(repositories, "maven", MavenRepositoryExtension.class, project);
    }

    public MavenArtifactRepository doCall(Object url, Action<MavenArtifactRepository> configure) {
        return this.repositories().maven(repository -> {
            var url1 = url instanceof String string ? Util.tryCatch(() -> this.project.file(string))
                .filter(File::exists)
                .map(Object.class::cast)
                .or(() -> Util.tryCatch(() -> new URL(string)))
                .orElse("https://" + string) : url;

            repository.setUrl(url1);
            repository.setName(url1.toString().replaceFirst("^.*?://", "").replace('/', '-'));

            if (repository instanceof ExtensionAware) {
                UsernameExtension.inject(repository);
                PasswordExtension.inject(repository);
            }

            this.project.configure(List.of(repository), configure);

            if (repository.getName().equals("maven")) {
                // Panic because further attempts to access this extension will fail.
                throw new IllegalArgumentException("\"maven\" is a bad repository name.");
            }
        });
    }

    public MavenArtifactRepository doCall(Object url) {
        return this.doCall(url, repository -> {});
    }

    private RepositoryHandler repositories() {
        return (RepositoryHandler) this.getOwner();
    }
}
