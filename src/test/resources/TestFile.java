public class TestFile {
    public void method1() {
        // Method implementation
        System.out.println("Executing method1");
    }

    public void method2() {
        // Method implementation
        System.out.println("Executing method2");
        method1(); // Call method1 from method2
    }

    public void method3() {
        // Method implementation
        System.out.println("Executing method3");
    }
}