/*
 * Classname: FanOutAnalysisTest
 * Programmer: Josh O'Brien
 * Version: Java 17
 * Date: 16/05/2023
 * Description: Unit tests which checks that fan out displays correct result
 */


package unit;


import com.example.seng4430plugin.FanOutAnalysis;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Collection;

public class FanOutAnalysisTest extends BasePlatformTestCase
{
    public void testFanInCount_NormalMethod()
    {
        PsiClass psiClass = getClass("Class1");
        PsiMethod psiMethod = psiClass.findMethodsByName("method1", false)[0];

        FanOutAnalysis analysis = new FanOutAnalysis();
        int result = analysis.performFanOutAnalysis(psiMethod, myFixture.getProject());

        assertEquals(3, result);
    }

    //helper method getting the correct class from the file
    public PsiClass getClass( String name )
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