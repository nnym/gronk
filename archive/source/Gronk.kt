package net.auoeke.gronk

import groovy.transform.CompileStatic
import org.gradle.api.plugins.JavaPluginExtension
import java.util.HashSet
import net.auoeke.gronk.extension.Extension
import net.auoeke.gronk.extension.relocate.RelocationExtension
import net.auoeke.gronk.extension.relocate.Type
import net.auoeke.gronk.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import java.io.File
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream

@CompileStatic
internal class Gronk : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("gronk", Extension(project))

        project.extensions.getByType(JavaPluginExtension::class.java).sourceSets.all {set ->
            val relocation = RelocationExtension(project)
            set.extensions.add("relocate", relocation)

            project.afterEvaluate {
                val classes = project.tasks.getByName(set.classesTaskName)

                classes.doLast {
                    classes.taskDependencies.getDependencies(classes).flatMapTo(HashSet()) {it.outputs.files.files}.map(File::toPath).forEach {outputs ->
                        val relocated = HashSet<Path>()

                        if (outputs.exists) {
                            outputs.walkFiles {file ->
                                val path = outputs.relativize(file)
                                val pathString = path.string
                                val details = relocation.call(pathString)

                                if (details != null) {
                                    val destination = outputs.resolve(details.to).letIf(details.type == Type.INCLUDE) {it.resolve(path)}

                                    if (!relocated.contains(path)) {
                                        destination.parent.mkdirs()

                                        if (file.string.endsWith(".class") && destination != file) {
                                            RenamingClassWriter(pathString.replace(".class", "")).also {
                                                ClassReader(file.inputStream()).accept(it, 0)
                                                destination.write(it.writer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                                            }

                                            file.delete()
                                        } else {
                                            file.move(destination, StandardCopyOption.REPLACE_EXISTING)
                                        }

                                        relocated.add(destination)

                                        try {
                                            file.parent.delete()
                                        } catch (ignored1: DirectoryNotEmptyException) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
