package net.auoeke.gronk

import groovy.transform.CompileStatic
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

@CompileStatic
class RenamingClassWriter(private val name: String) : ClassVisitor(Opcodes.ASM9, ClassWriter(0)) {
    val writer: ClassWriter get() = this.cv as ClassWriter

    private val descriptor: String = "L" + this.name + ";"

    @Override
    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
        super.visit(version, access, this.name, signature?.replace("L$name;", this.descriptor), superName, interfaces)
    }

    @Override
    override fun visitSource(source: String, debug: String ) {
        super.visitSource(source, debug)
    }
}
