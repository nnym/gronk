package net.auoeke.gronk;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;

public class Util {
    public static <T> Optional<T> extension(Project project, Class<T> type) {
        return Optional.ofNullable(project.getExtensions().findByType(type));
    }

    public static <T> void extension(Project project, Class<T> type, Consumer<T> configure) {
        extension(project, type).ifPresent(configure);
    }

    public static <T> void extensionAfterEvaluation(Project project, Class<T> extension, Consumer<T> configure) {
        project.afterEvaluate(p -> extension(p, extension, configure));
    }

    public static <T> void whenExtensionPresent(Project project, Class<T> type, Consumer<T> configure) {
        extension(project, type).ifPresentOrElse(configure, () -> extensionAfterEvaluation(project, type, configure));
    }

    public static void javaExtension(Project project, Consumer<JavaPluginExtension> configure) {
        whenExtensionPresent(project, JavaPluginExtension.class, configure);
    }

    public static boolean plugin(Project project, String name, Runnable action) {
        if (project.getPluginManager().hasPlugin(name)) {
            action.run();

            return true;
        }

        return false;
    }

    public static void whenPluginPresent(Project project, String name, Runnable action) {
        project.getPluginManager().withPlugin(name, plugin -> action.run());
    }

    public static void whenPluginPresent(Project project, String name, Consumer<Plugin<Project>> action) {
        project.getPluginManager().withPlugin(name, plugin -> action.accept(project.getPlugins().getPlugin(name)));
    }

    public static <T> T tryAddExtension(ExtensionAware object, String name, T extension) {
        if (object.getExtensions().findByName(name) == null) {
            object.getExtensions().add(name, extension);
        }

        return extension;
    }

    public static MavenArtifactRepository repository(Project project, String url) {
        return project.getRepositories().withType(MavenArtifactRepository.class).stream()
            .filter(repository -> repository.getUrl().toString().matches(Pattern.quote(url) + "/?"))
            .findAny()
            .orElse(project.getRepositories().maven(repository -> repository.setUrl(url)));
    }

    public static <T> Optional<T> tryCatch(Callable<T> callable) {
        try {
            return Optional.of(callable.call());
        } catch (Throwable throwable) {
            return Optional.empty();
        }
    }

    public static <T> Closure actionClosure(Action<T> action) {
        return new Closure(action) {
            @SuppressWarnings("unused")
            public void doCall(T object) {
                action.execute(object);
            }
        };
    }

    public static <T, R> Closure functionClosure(Function<T, R> function) {
        return new Closure(function) {
            @SuppressWarnings("unused")
            public R doCall(T object) {
                return function.apply(object);
            }
        };
    }
}
