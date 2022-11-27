package archive.extension.relocate

import groovy.transform.CompileStatic
import org.intellij.lang.annotations.RegExp

import java.nio.file.Path
import java.util.function.Function
import java.util.function.Predicate
import java.util.regex.Pattern

@CompileStatic
class Relocation {
	protected Projector projector
	protected Function<Path, Path> to
	protected Path pkg
	protected boolean constant

	private static Path path(String path) {Path.of(path.replace('.', '/'))}

	void to(Function<Path, Path> to) {
		this.to = to
		this.constant = false
		this.pkg = null
	}

	void to(String to) {
		this.to = {this.pkg}
		this.pkg = path(Objects.requireNonNull(to))
		this.constant = true

		if (to.endsWith("/")) {
			throw new IllegalArgumentException(/Relocation target package "${to}" ends with an illegal character./)
		}
	}

	void include(boolean flatten = false, Predicate<Path> predicate) {
		this.project(Type.INCLUDE, predicate) {path ->
			def result = this.to.apply(path)
			def filename = result.fileName

			return result.resolve(pkg.relativize(path)).resolve(filename)
		}
	}

	void include(boolean append = false, @RegExp String pattern) {
		this.include(append, pathPredicate(pattern))
	}

	void includePackage(boolean append = false, String pkg) {this.includePackage(append, path(pkg))}

	void includePackage(boolean append = false, File pkg) {this.includePackage(append, pkg.toPath())}

	void includePackage(boolean append = false, Path pkg) {
		this.project(Type.INCLUDE, path -> pkg.empty ? !path.parent : path.startsWith((pkg)) && path.nameCount === pkg.nameCount + 1, this.packageMap(append, pkg))
	}

	void includeTree(boolean append = false, String pkg) {this.includeTree(append, path(pkg))}

	void includeTree(boolean append = false, File pkg) {this.includeTree(append, pkg.toPath())}

	void includeTree(boolean append = false, Path pkg) {
		pkg.empty ? this.project(Type.INCLUDE, true) : this.project(Type.INCLUDE, path -> path.startsWith(pkg), this.packageMap(append, pkg))
	}

	void exclude(Predicate<Path> predicate) {this.project(Type.EXCLUDE, predicate)}

	void exclude(@RegExp String pattern) {this.exclude(pathPredicate(pattern))}

	void excludePackage(String pkg) {this.excludePackage(path(pkg))}

	void excludePackage(File pkg) {this.excludePackage(pkg.toPath())}

	void excludePackage(Path pkg) {
		this.exclude(path -> pkg.empty ? !path.parent : path.startsWith(pkg) && path.nameCount === pkg.nameCount + 1)
	}

	void excludeTree(String pkg) {this.excludeTree(path(pkg))}

	void excludeTree(File pkg) {this.excludeTree(pkg.toPath())}

	void excludeTree(Path pkg) {
		pkg.empty ? this.project(Type.EXCLUDE, false) : this.exclude(path -> path.startsWith(pkg))
	}

	private void project(Type type, Predicate<Path> predicate, Function<Path, Path> map = null) {
		this.projector = new Projector(this.projector, type, predicate, map)
	}

	private void project(Type type, boolean predicate) {
		this.projector = new Projector(type, {predicate})
	}

	private Function<Path, Path> packageMap(boolean append, Path pkg) {
		return (Path path) -> {
			def result = this.to.apply(path)
			def filename = result.fileName

			if (append) {
				result = result.resolveSibling(pkg)
			}

			return result.resolve(pkg.relativize(path)).resolve(filename)
		}
	}

	private static Predicate<Path> pathPredicate(String pattern) {
		def compiledPattern = Pattern.compile(pattern)

		return (path) -> compiledPattern.matcher(path.toString()).matches()
	}
}
