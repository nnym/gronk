public class Test {
    public static void main(String... args) {
        net.auoeke.reflect.Reflect.class.getDeclaredField("defaultClassLoader").get(null);
    }
}
