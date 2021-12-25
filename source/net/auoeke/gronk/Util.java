package net.auoeke.gronk;

import groovy.lang.Closure;
import org.gradle.api.Action;

public class Util {
    public static <T> Closure closure(Object owner, Object thisObject, Action<T> action) {
        return new Closure(owner, thisObject) {
            public void doCall(T object) {
                action.execute(object);
            }
        };
    }
}
