package unit;

import com.example.seng4430plugin.CohesionClassAnalysis;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.intellij.openapi.editor.Editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CohesionClassAnalysisTest extends BasePlatformTestCase {

    private CohesionClassAnalysis cohesionClassAnalysis;
    private Project project;
    private Editor editor;
    private PsiFile psiFile;
    private PsiClass psiClass;


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp(); // Call the parent class's setUp() method
        cohesionClassAnalysis = new CohesionClassAnalysis();
        project = getProject(); // Use the inherited getProject() method
    }


    @Test
    public void testGetData() {
        // Set up test data
        String[][] testData = {{"ClassA", "0.50"}, {"ClassB", "0.25"}};
        cohesionClassAnalysis.data = testData;

        // Test the getData() method
        String[][] result = cohesionClassAnalysis.getData();

        // Verify the result
        assertEquals(testData, result);
    }

}
