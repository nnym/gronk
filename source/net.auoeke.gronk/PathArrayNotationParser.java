package net.auoeke.gronk;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.internal.file.FileOrUriNotationConverter;
import org.gradle.internal.exceptions.DiagnosticsVisitor;
import org.gradle.internal.typeconversion.NotationParser;
import org.gradle.internal.typeconversion.TypeConversionException;

public class PathArrayNotationParser implements NotationParser<Object, Object> {
	private final NotationParser<Object, Object> parent = FileOrUriNotationConverter.parser();

	@Override public Object parseNotation(Object notation) throws TypeConversionException {
		var stream = notation instanceof Object[] array ? Stream.of(array)
			 	  : notation instanceof Collection<?> collection ? collection.stream()
				  : null;

		if (stream == null) {
			return this.parent.parseNotation(notation);
		}

		var paths = stream.map(this.parent::parseNotation).map(path -> {
			if (path instanceof File file) {
				return file;
			}

			throw new TypeConversionException("Cannot convert path to File. path='%s'".formatted(path));
		}).collect(Collectors.toList());

		if (paths.isEmpty()) {
			throw new TypeConversionException("Collection is empty.");
		}

		var result = paths.remove(0);

		for (var path : paths) {
			if (path.isAbsolute()) {
				throw new TypeConversionException("Path is absolute. Path='%s'".formatted(path));
			}

			result = new File(result, path.getPath());
		}

		return result;
	}

	@Override public void describe(DiagnosticsVisitor visitor) {
		this.parent.describe(visitor);
		visitor.candidate("A non-empty Collection instance or array of any of the above representing an absolute or relative path followed by relative paths.");
	}
}
