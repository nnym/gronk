public class Test {
    public static void main(String... args) {
        net.auoeke.reflect.Reflect.class.getDeclaredMethod("instrument").invoke(null);
    }
}
