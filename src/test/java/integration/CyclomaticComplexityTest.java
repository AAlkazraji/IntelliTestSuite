package integration;

import com.example.seng4430plugin.CyclomaticComplexityAnalysis;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.awt.*;

public class CyclomaticComplexityTest extends BasePlatformTestCase {
    public void testCodeD() throws Exception {
        myFixture.configureByFile("CyclomaticComplexityTestData.java");

        //placing data context for the action menu
        MapDataContext dataContext = new MapDataContext();
        dataContext.put(CommonDataKeys.PROJECT, myFixture.getProject());
        dataContext.put(CommonDataKeys.EDITOR, myFixture.getEditor());
        dataContext.put(CommonDataKeys.PSI_FILE, myFixture.getFile());
        AnActionEvent event = AnActionEvent.createFromDataContext(ActionPlaces.CONTEXT_TOOLBAR, new Presentation(), dataContext);

        // Mocking the action
        CyclomaticComplexityAnalysis action = new CyclomaticComplexityAnalysis();
        action.actionPerformed(event);

        Component[] d = action.getOutput().getComponents();
        assertNotNull(d);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/";
    }
}
