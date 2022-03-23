package net.auoeke.gronk;

import java.util.stream.Stream;
import groovy.lang.Closure;
import org.gradle.api.plugins.ExtensionAware;

public abstract class ClosureExtension<A, B> extends Closure<B> {
    private static final StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public ClosureExtension(A owner) {
        super(owner);
    }

    protected static void inject(Object object, String name, Object... arguments) {
        ((ExtensionAware) object).getExtensions().create(name, stackWalker.getCallerClass(), Stream.concat(Stream.of(object), Stream.of(arguments)).toArray());
    }

    protected A owner() {
        return (A) this.getOwner();
    }
}
