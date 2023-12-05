public class Class1 extends SuperClass
{
    public void abstractMethod()
    {
        methodOverload();
    }

    public void method1()
    {
        System.out.println("Fan-out analysis should include this method");
        superMethod();
        methodRecursive(1);
        abstractMethod();
    }

    //testing overloading methods
    public void methodOverload()
    {
        method1();
        methodOverload(1);
    }

    public void methodOverload(int i)
    {
        methodOverload(i, 2);
    }

    public void methodOverload(int i, int j)
    {
        return;
    }

    //testing recursive methods
    public void methodRecursive( int i )
    {
        while (i < 2)
        {
            methodRecursive(i++);
        }
    }
}

public class Class2
{
    //testing abstractmethod() but in a class not using inheritance
    public void abstractMethod()
    {
        method1();
        method2();
    }

    public void method1()
    {
        method2();
    }

    public void method2()
    {
        return;
    }
}

public class Class3 extends SuperClass
{
    //testing that this method isnt included in Class1.abstractMethod() fan count
    public void abstractMethod()
    {
        method1();
    }

    //testing that this method isnt included in Class1.method1() fan count
    public void method1()
    {
        superMethod();
    }
}

public abstract class SuperClass
{
    //testing superclass method
    public void superMethod()
    {
        return;
    }

    public abstract void abstractMethod();
}
