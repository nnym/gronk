package net.auoeke.gronk;

import groovy.lang.Closure;
import java.util.Optional;
import java.util.function.Consumer;
import org.gradle.api.Action;
import org.gradle.api.Project;

public class Util {
    public static <T> void extension(Project project, Class<T> extension, Consumer<T> configure) {
        Optional.ofNullable(project.getExtensions().findByType(extension)).ifPresent(configure);
    }

    public static <T> void extensionAfterEvaluation(Project project, Class<T> extension, Consumer<T> configure) {
        project.afterEvaluate(p -> extension(p, extension, configure));
    }

    public static <T> Closure closure(Object owner, Object thisObject, Action<T> action) {
        return new Closure(owner, thisObject) {
            public void doCall(T object) {
                action.execute(object);
            }
        };
    }
}
