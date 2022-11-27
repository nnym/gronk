package net.auoeke.gronk;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer;
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.file.FileTreeElement;
import org.gradle.internal.MutableBoolean;
import org.gradle.util.Configurable;
import org.gradle.util.internal.ConfigureUtil;
import shadow.org.apache.tools.zip.ZipEntry;
import shadow.org.apache.tools.zip.ZipOutputStream;

public class ManifestMergerExtension extends ClosureExtension<ShadowJar, Void> implements Configurable<ManifestMergerExtension>, Transformer {
	private static final Predicate<ManifestContext> initialPredicate = context -> true;

	protected boolean configured;

	private Predicate<ManifestContext> include = initialPredicate;
	private Manifest manifest;
	private FileTreeElement file;

	public ManifestMergerExtension(ShadowJar owner) {
		super(owner);
	}

	public static void inject(ShadowJar owner) {
		inject(owner, "mergeManifests");
	}

	private static boolean invoke(Closure<Boolean> predicate, ManifestContext context) {
		var result = new MutableBoolean();
		ConfigureUtil.configureSelf(predicate.rightShift(Util.actionClosure(returnValue -> result.set(DefaultGroovyMethods.asType(returnValue, boolean.class)))), context);
		return result.get();
	}

	private static void put(Map<String, Object> source, Attributes destination) {
		source.forEach((key, value) -> {
			if (destination.getValue(key) == null) {
				destination.putValue(key, String.valueOf(value));
			}
		});
	}

	@Override public ManifestMergerExtension configure(Closure closure) {
		this.doCall();
		return ConfigureUtil.configureSelf(closure, this);
	}

	@Override public boolean canTransformResource(FileTreeElement file) {
		this.file = file;
		return JarFile.MANIFEST_NAME.equals(file.getRelativePath().getPathString());
	}

	@Override public void transform(TransformerContext context) {
		var bytes = context.getIs().readAllBytes();
		var manifestContext = new ManifestContext(this.file, new Manifest(new ByteArrayInputStream(bytes)), context.getRelocators(), context.getStats());

		if (this.include.test(manifestContext)) {
			if (this.manifest == null) {
				this.manifest = manifestContext.manifest;
			} else {
				manifestContext.manifest.getMainAttributes().forEach(this.manifest.getMainAttributes()::putIfAbsent);
				manifestContext.manifest.getEntries().forEach((name, attributes) -> {
					var mergedAttributes = this.manifest.getEntries().computeIfAbsent(name, n -> new Attributes());
					attributes.forEach(mergedAttributes::putIfAbsent);
				});
			}
		}
	}

	@Override public boolean hasTransformedResource() {
		return this.manifest != null;
	}

	@Override public void modifyOutputStream(ZipOutputStream output, boolean preserveFileTimestamps) {
		output.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
		this.manifest.write(output);
	}

	@Override public String getName() {
		return this.getClass().getSimpleName();
	}

	public void doCall() {
		if (!this.configured) {
			this.owner().doFirst(Util.actionClosure(task -> {
				this.manifest = new Manifest();
				put(this.owner().getManifest().getAttributes(), this.manifest.getMainAttributes());
				this.owner().getManifest().getEffectiveManifest().getSections().forEach((name, attributes) -> put(attributes, this.manifest.getEntries().computeIfAbsent(name, n -> new Attributes())));
			}));

			this.owner().transform(this);
			this.configured = true;
		}
	}

	public void include(Predicate<ManifestContext> predicate) {
		this.include = this.include == initialPredicate ? predicate : this.include.or(predicate);
	}

	public void include(Closure<Boolean> predicate) {
		this.include(context -> invoke(predicate, context));
	}

	public void exclude(Predicate<ManifestContext> predicate) {
		this.include = this.include.and(predicate.negate());
	}

	public void exclude(Closure<Boolean> predicate) {
		this.exclude(context -> invoke(predicate, context));
	}
}
