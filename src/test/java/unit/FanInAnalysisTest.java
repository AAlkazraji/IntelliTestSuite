/*
 * Classname: FanInAnalysisTest
 * Programmer: Josh O'Brien
 * Version: Java 17
 * Date: 15/05/2023
 * Description: Testing the different edge cases of fan in analysis
 */


package unit;

import com.intellij.psi.PsiClass;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.example.seng4430plugin.FanInAnalysis;

import java.util.Collection;

public class FanInAnalysisTest extends BasePlatformTestCase
{
    public void testFanInCount_NormalMethod()
    {

        PsiClass psiClass = getClass("Class1");
        PsiMethod psiMethod = psiClass.findMethodsByName("method1", false)[0];

        FanInAnalysis analysis = new FanInAnalysis();
        int result = analysis.performFanInAnalysis(psiMethod, myFixture.getProject());

        assertEquals(1, result);
    }

    public void testFanInCount_OverloadingMethods()
    {
        PsiClass psiClass = getClass("Class1");
        PsiMethod[] psiMethod = psiClass.findMethodsByName("methodOverload", false);

        FanInAnalysis analysis = new FanInAnalysis();

        //methodOverload()
        int result1 = analysis.performFanInAnalysis(psiMethod[0], myFixture.getProject());
        //methodOverload( int i )
        int result2 = analysis.performFanInAnalysis(psiMethod[1], myFixture.getProject());
        //methodOverload( int i, int j )
        int result3 = analysis.performFanInAnalysis(psiMethod[2], myFixture.getProject());

        assertEquals(1, result1);
        assertEquals(1, result2);
        assertEquals(1, result3);
    }

    public void testFanInCount_AbstractMethod()
    {
        PsiClass psiClass = getClass("Class1");
        PsiMethod psiMethod = psiClass.getMethods()[0];

        FanInAnalysis analysis = new FanInAnalysis();
        int result = analysis.performFanInAnalysis(psiMethod, myFixture.getProject());

        assertEquals(1, result);
    }

    public void testFanInCount_SuperMethod()
    {
        PsiClass psiClass = getClass("SuperClass");
        PsiMethod psiMethod = psiClass.findMethodsByName("superMethod", false)[0];

        FanInAnalysis analysis = new FanInAnalysis();
        int result = analysis.performFanInAnalysis(psiMethod, myFixture.getProject());

        assertEquals(2, result);
    }

    public void testFanInCount_RecursiveMethod()
    {
        PsiClass psiClass = getClass("Class1");
        PsiMethod psiMethod = psiClass.findMethodsByName("methodRecursive", false)[0];

        FanInAnalysis analysis = new FanInAnalysis();
        int result = analysis.performFanInAnalysis(psiMethod, myFixture.getProject());

        assertEquals(1, result);
    }

    public PsiClass getClass(String name)
    {
        myFixture.configureByFile("FanClasses.java");

        Collection<PsiClass> psiClasses = PsiTreeUtil.findChildrenOfType(myFixture.getFile(), PsiClass.class);
        for (PsiClass clazz : psiClasses)
        {
            if (clazz.getName().equals(name))
            {
                return clazz;
            }
        }
        return null;
    }

    @Override
    protected String getTestDataPath()
    {
        return "src/test/resources/";
    }
}