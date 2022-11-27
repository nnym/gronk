import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

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

		var project = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(testCase("fat/build/publications/maven/pom-default.xml").toFile()).getFirstChild();
		var xPath = XPathFactory.newDefaultInstance().newXPath();

		assert xPath.evaluate("name", project).equals("fat");
		assert xPath.evaluate("url", project).equals("https://github.com/auoeke/gronk/tree/master/test/cases/fat");
		assert xPath.evaluate("description", project).equals("gronk fat test project");
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
}
