package net.auoeke.gronk;

import java.util.stream.Stream;
import groovy.lang.Closure;
import org.gradle.api.plugins.ExtensionAware;

public abstract class ClosureExtension<A, B> extends Closure<B> {
    public ClosureExtension(A owner) {
        super(owner);
    }

    public static void inject(Object object, String name, Class<?> type, Object... arguments) {
        ((ExtensionAware) object).getExtensions().create(name, type, Stream.concat(Stream.of(object), Stream.of(arguments)).toArray());
    }

    protected A owner() {
        return (A) this.getOwner();
    }
}
