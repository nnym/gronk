import java.nio.file.Path;
import java.util.stream.Stream;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class GronkTest {
    static Path testCase(String name) {
        return Path.of("test/cases", name);
    }

    static GradleRunner runner(String testCase, String... arguments) {
        return GradleRunner.create()
            .withProjectDir(testCase(testCase).toFile())
            .withDebug(true)
            .withPluginClasspath()
            .withArguments(Stream.concat(Stream.of("-S"), Stream.of(arguments)).toList())
            .forwardOutput();
    }

    @Test
    void fat() {
        runner("fat", "clean", "build", "publishToMavenLocal").build();
    }

    // @Test
    void noPublish() {
        runner("no-publish", "clean", "build").build();
    }

    // @Test
    void noJava() {
        runner("no-java", "build").build();
    }
}
