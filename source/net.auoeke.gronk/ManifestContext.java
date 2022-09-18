package net.auoeke.gronk;

import java.util.List;
import java.util.jar.Manifest;
import com.github.jengelman.gradle.plugins.shadow.ShadowStats;
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator;
import org.gradle.api.file.FileTreeElement;

public final class ManifestContext {
    public final FileTreeElement file;
    public final Manifest manifest;
    public final List<Relocator> relocators;
    public final ShadowStats stats;

    public ManifestContext(FileTreeElement file, Manifest manifest, List<Relocator> relocators, ShadowStats stats) {
        this.file = file;
        this.manifest = manifest;
        this.relocators = relocators;
        this.stats = stats;
    }

    public FileTreeElement getFile() {
        return this.file;
    }

    public Manifest getManifest() {
        return this.manifest;
    }

    public List<Relocator> getRelocators() {
        return this.relocators;
    }

    public ShadowStats getStats() {
        return this.stats;
    }
}
