package archive.extension

import groovy.transform.CompileStatic
import org.gradle.util.Configurable
import org.gradle.util.ConfigureUtil

@CompileStatic
abstract class CallableExtension extends Closure implements Configurable {
	CallableExtension() {
		super(null)
	}

	protected static <V> V configure(Object object, Closure<V> closure) {
		return ConfigureUtil.configureSelf(closure, object) as V
	}

	@Override
	def getProperty(String name) {this.metaClass.getProperty(this, name)}

	@Override
	void setProperty(String name, Object value) {this.metaClass.setProperty(this, name, value)}

	@Override
	Object configure(Closure closure) {
		return configure(this, closure)
	}

	final void doCall() {
		this.call()
	}
}
