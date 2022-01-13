public class Test {
    @lombok.SneakyThrows
    public static void main(String... args) {
        net.auoeke.reflect.Reflect.class.getDeclaredField("defaultClassLoader").get(null);
    }
}
