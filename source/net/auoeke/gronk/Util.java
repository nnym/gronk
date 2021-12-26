package net.auoeke.gronk;

import groovy.lang.Closure;
import java.util.Optional;
import java.util.function.Consumer;
import org.gradle.api.Action;
import org.gradle.api.Project;
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

    public static <T> Closure closure(Object owner, Object thisObject, Action<T> action) {
        return new Closure(owner, thisObject) {
            public void doCall(T object) {
                action.execute(object);
            }
        };
    }
}
