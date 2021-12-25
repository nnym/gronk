package archive.extension.relocate

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.util.function.Function
import java.util.function.Predicate

@CompileStatic
class Projector {
    private final Projector parent
    private final Type type
    private final Predicate<Path> predicate
    private final Function<Path, Path> map

    Projector(Projector parent = null, Type type, Predicate<Path> predicate, Function<Path, Path> map = null) {
        this.parent = parent
        this.type = type
        this.predicate = predicate
        this.map = map
    }

    Type match(Path path) {
        return this.defaultMatch(path) ?: this.parent?.match(path)
    }

    String map(Path path) {
        return this.map?.apply(path)
    }

    private Type defaultMatch(Path path) {
        return this.predicate.test(path) ? this.type : null
    }
}
