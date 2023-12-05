/*
 * Classname: CyclomaticComplexityAnalysisTest
 * Programmer: Ahmad Al-kanini
 * Version: Java 17
 * Date: 15/05/2023
 * Description: Testing the different edge cases of cyclomatic complexity analysis
 */


package unit;

import com.example.seng4430plugin.CyclomaticComplexityAnalysis;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CyclomaticComplexityTest extends BasePlatformTestCase
{
    public void testCalculateComplexity_SimpleMethod() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(1);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(1, result.get("void simpleMethod()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithControlStructures() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(4);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(4, result.get("void methodWithAllControlStructures()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithIf() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(2);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(2, result.get("void methodWithIf()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithIfElse() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(2);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(2, result.get("void methodWithIfElse()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithForLoop() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(2);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(2, result.get("void methodWithForLoop()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithSwitch() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(3);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(3, result.get("void methodWithSwitch()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithWhileLoop() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(2);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(2, result.get("void methodWithWhileLoop()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithDoWhileLoop() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(2);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(2, result.get("void methodWithDoWhileLoop()").values().iterator().next().intValue());
    }

    public void testCalculateComplexity_MethodWithForEach() throws IOException
    {

        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis(1);
        Map<Path, CompilationUnit> file = getFile();
        Map<String, Map<Path, Integer>> result = analysis.calculateComplexity(file);

        assertEquals(1, result.get("void methodWithForEach()").values().iterator().next().intValue());
    }

    public Map<Path, CompilationUnit> getFile() throws IOException
    {
        List<Path> paths = new ArrayList<>();
        paths.add(Paths.get(myFixture.getTestDataPath() + "CyclomaticComplexityTestData.java"));
        CyclomaticComplexityAnalysis analysis = new CyclomaticComplexityAnalysis();
        return analysis.parseCode(paths);
    }


    @Override
    protected String getTestDataPath()
    {
        return "src/test/resources/";
    }
}
