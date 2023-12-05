public class CyclomaticClasses {

    //CC = 1
    public void simpleMethod() {
        System.out.println("Simple Method");
    }

    //CC = 2
    public void methodWithIf() {
        int a = 10;
        if (a > 5) {
            System.out.println("Greater than 5");
        }
    }

    //CC = 2
    public void methodWithIfElse() {
        int a = 10;
        if (a > 5) {
            System.out.println("Greater than 5");
        } else {
            System.out.println("Not greater than 5");
        }
    }

    //CC = 2
    public void methodWithForLoop() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Number : " + i);
        }
    }

    //CC = 3
    public void methodWithSwitch() {
        int a = 1;
        switch (a) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            default:
                System.out.println("Default");
                break;
        }
    }

    //CC = 2
    public void methodWithWhileLoop() {
        int i = 0;
        while (i < 10) {
            System.out.println("Number : " + i);
            i++;
        }
    }

    //CC = 1
    public void methodWithDoWhileLoop() {
        int i = 0;
        do {
            System.out.println("Number : " + i);
            i++;
        } while (i < 10);
    }

    //CC = 2
    public void methodWithForEach() {
        int[] arr = {1, 2, 3, 4, 5};
        for (int num : arr) {
            System.out.println(num);
        }
    }

    //CC = 4
    public void methodWithAllControlStructures() {
        int[] arr = {1, 2, 3, 4, 5};
        for (int num : arr) {
            if (num < 3) {
                System.out.println(num);
            } else if (num == 3) {
                System.out.println("Three");
            } else {
                while (num > 0) {
                    num--;
                }
            }
        }
    }
}
