package unit;

import com.example.seng4430plugin.HalsteadAnalysis;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HalsteadAnalysisTest extends BasePlatformTestCase {

    private HalsteadAnalysis halsteadAnalysis;
    private PsiFile psiFile;

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        halsteadAnalysis = new HalsteadAnalysis();
        psiFile = mock(PsiFile.class);
    }

    @Test
    public void testIsOperator() {
        // Call the isOperator method with different input
        assertTrue(halsteadAnalysis.isOperator("+"));
        assertTrue(halsteadAnalysis.isOperator("-"));
        assertTrue(halsteadAnalysis.isOperator("*"));
        assertTrue(halsteadAnalysis.isOperator("/"));
        assertFalse(halsteadAnalysis.isOperator("abc"));
    }

    @Test
    public void testIsOperand() {
        // Prepare test data
        PsiJavaToken token = mock(PsiJavaToken.class);

        // Mock the token type to represent an identifier
        when(token.getTokenType()).thenReturn(JavaTokenType.IDENTIFIER);

        // Call the isOperand method
        boolean isOperand = halsteadAnalysis.isOperand(token);

        // Verify the result
        assertTrue(isOperand);
    }

}
