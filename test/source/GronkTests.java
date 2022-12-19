import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.gradle.testkit.runner.GradleRunner;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.w3c.dom.Node;

@Testable
class GronkTests {
	static Path testCase(String name) {
		return Path.of("test/cases", name);
	}

	static GradleRunner runner(String testCase, String... arguments) {
		var args = new ArrayList<>(List.of(arguments));
		args.add("-S");

		var home = Path.of(System.getProperty("user.home"), ".gradle");
		var init = home.resolve("init.gradle");

		if (Files.exists(init)) {
			args.add("-I");
			args.add(init.toString());
		}

		var propertiesPath = home.resolve("gradle.properties");

		if (Files.exists(propertiesPath)) {
			var properties = new Properties();
			properties.load(Files.newInputStream(propertiesPath));

			properties.forEach((key, value) -> {
				args.add("-P" + key + "=" + value);
			});
		}

		return GradleRunner.create()
			.withProjectDir(testCase(testCase).toFile())
			.withDebug(true)
			.withPluginClasspath()
			.withArguments(args)
			.forwardOutput();
	}

	@Test
	void fat() {
		runner("fat", "clean", "build", "publish", "publishToMavenLocal").build();

		var path = new ProjectPath("fat/build/publications/maven/pom-default.xml");
		path.eq("name", "fat")
			.eq("url", "https://github.com/auoeke/gronk/tree/master/test/cases/fat")
			.eq("description", "gronk fat test project")
			.eq("licenses/license/url", "https://github.com/auoeke/gronk/blob/master/LICENSE.md")
			.eq("developers/developer/email", "tjmnkrajyej@gmail.com")
			.eq("scm/url", path.evaluate("url"));
	}

	@Test
	void noPublish() {
		runner("no-publish", "clean", "build").build();
	}

	@Test
	void noJava() {
		runner("no-java", "build").build();
	}

	// @Test
	void dependencies() {
		runner("fat").build();
	}

	record ProjectPath(Node project, XPath xPath) {
		static final DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();

		ProjectPath(String path) {
			this(factory.newDocumentBuilder().parse(testCase(path).toFile()).getFirstChild(), XPathFactory.newDefaultInstance().newXPath());
		}

		String evaluate(String path) {
			return this.xPath.evaluate(path, this.project);
		}

		ProjectPath eq(@Language("XPath") String path, String value) {
			assert this.xPath.evaluate(path, this.project).equals(value);
			return this;
		}
	}
}
