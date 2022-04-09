package net.auoeke.gronk;

import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer;
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.file.FileTreeElement;
import org.gradle.internal.MutableBoolean;
import org.gradle.util.internal.ConfigureUtil;
import shadow.org.apache.tools.zip.ZipEntry;
import shadow.org.apache.tools.zip.ZipOutputStream;

public class ManifestMerger implements Transformer {
    private static final Predicate<ManifestContext> initialPredicate = context -> true;

    private Predicate<ManifestContext> include = initialPredicate;
    private Manifest manifest;
    private FileTreeElement file;

    private boolean invoke(Closure<Boolean> predicate, ManifestContext context) {
        var result = new MutableBoolean();
        ConfigureUtil.configureSelf(predicate.rightShift(Util.actionClosure(returnValue -> result.set(DefaultGroovyMethods.asType(returnValue, boolean.class)))), context);
        return result.get();
    }

    public void include(Predicate<ManifestContext> predicate) {
        this.include = this.include == initialPredicate ? predicate : this.include.or(predicate);
    }

    public void include(Closure<Boolean> predicate) {
        this.include(context -> this.invoke(predicate, context));
    }

    public void exclude(Predicate<ManifestContext> predicate) {
        this.include = this.include.and(predicate.negate());
    }

    public void exclude(Closure<Boolean> predicate) {
        this.exclude(context -> this.invoke(predicate, context));
    }

    @Override public boolean canTransformResource(FileTreeElement file) {
        this.file = file;
        return JarFile.MANIFEST_NAME.equals(file.getRelativePath().getPathString());
    }

    @Override public void transform(TransformerContext context) {
        var manifestContext = new ManifestContext(this.file, context.getPath(), new Manifest(context.getIs()), context.getRelocators(), context.getStats());

        if (this.include.test(manifestContext)) {
            if (this.manifest == null) {
                this.manifest = manifestContext.manifest;
            } else {
                manifestContext.manifest.getMainAttributes().forEach((key, value) -> this.manifest.getMainAttributes().putIfAbsent(key, value));
            }
        } else {
            this.manifest = null;
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
}
