package unit;

import com.example.seng4430plugin.CodeDuplicationAnalysis;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CodeDuplicationAnalysisTest extends BasePlatformTestCase
{

    public void testFindDuplicates() throws IOException
    {
        // Prepare test data
        Map<Path, CompilationUnit> parsedCode = new HashMap<>();


        CodeDuplicationAnalysis codeDuplicationAnalysis = new CodeDuplicationAnalysis();

        // Read and parse test files
        Path testFile1Path = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData1.java");
        Path testFile2Path = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData2.java");

        String testFile1Content = Files.readString(testFile1Path);
        String testFile2Content = Files.readString(testFile2Path);

        JavaParser javaParser = new JavaParser();
        CompilationUnit testFile1CU = javaParser.parse(testFile1Content).getResult().orElse(null);
        CompilationUnit testFile2CU = javaParser.parse(testFile2Content).getResult().orElse(null);

        // Add parsed code from test files to the parsedCode map
        parsedCode.put(testFile1Path, testFile1CU);
        parsedCode.put(testFile2Path, testFile2CU);

        // Call the findDuplicates method
        Map<String, List<Path>> duplicates = codeDuplicationAnalysis.findDuplicates(parsedCode);

        // Verify the duplicates map is populated with the expected duplicates
        assertEquals(1, duplicates.size());
        String duplicateMethodSignature = "public int sum(int a, int b)";
        Assertions.assertTrue(duplicates.containsKey(duplicateMethodSignature));
        List<Path> duplicateFilePaths = duplicates.get(duplicateMethodSignature);
        assertEquals(3, duplicateFilePaths.size());
        Assertions.assertTrue(duplicateFilePaths.contains(testFile1Path));
        Assertions.assertTrue(duplicateFilePaths.contains(testFile2Path));
    }

    public void testParseCode() throws IOException
    {

        CodeDuplicationAnalysis codeDuplicationAnalysis = new CodeDuplicationAnalysis();

        // Prepare test data
        Path javaFile1 = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData1.java");
        Path javaFile2 = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData2.java");

        List<Path> javaFiles = Arrays.asList(javaFile1, javaFile2);

        // Call the method to test
        Map<Path, CompilationUnit> result = codeDuplicationAnalysis.parseCode(javaFiles);

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());

        CompilationUnit cu1 = result.get(javaFile1);
        CompilationUnit cu2 = result.get(javaFile2);

        assertNotNull(cu1);
        assertNotNull(cu2);

        assertEquals("CodeDuplicationAnalysisTestData1", cu1.getType(0).getNameAsString());
        assertEquals("CodeDuplicationAnalysisTestData2", cu2.getType(0).getNameAsString());
    }

    public void testCountTokens() throws IOException
    {
        CodeDuplicationAnalysis codeDuplicationAnalysis = new CodeDuplicationAnalysis();
        // Read test data
        Path javaFile1 = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData1.java");
        Path javaFile2 = Paths.get("src/test/resources/CodeDuplicationAnalysisTestData2.java");

        String code1 = Files.readString(javaFile1);
        String code2 = Files.readString(javaFile2);

        // Call the method to test
        int tokenCount1 = codeDuplicationAnalysis.countTokens(code1);
        int tokenCount2 = codeDuplicationAnalysis.countTokens(code2);

        // Assertions
        assertEquals(215, tokenCount1);
        assertEquals(60, tokenCount2);
    }

    public void testHashMethodCode()
    {
        CodeDuplicationAnalysis codeDuplicationAnalysis = new CodeDuplicationAnalysis();
        // Test data
        String methodCode1 = "public int sum(int a, int b) { return a + b; }";
        String methodCode2 = "public int multiply(int a, int b) { return a * b; }";

        // Call the method to test
        String hash1 = codeDuplicationAnalysis.hashMethodCode(methodCode1);
        String hash2 = codeDuplicationAnalysis.hashMethodCode(methodCode2);

        // Assertions
        assertNotEquals("", hash1);
        assertNotEquals("", hash2);
        assertNotEquals(hash1, hash2);
    }
}
