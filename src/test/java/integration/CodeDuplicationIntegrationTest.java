/*
 * Classname:
 * Programmer:
 * Version: Java 17
 * Date: 20/05/2023
 * Description:
 */

package integration;

import com.example.seng4430plugin.CodeDuplicationAnalysis;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.awt.*;

public class CodeDuplicationIntegrationTest extends BasePlatformTestCase
{

    public void testCodeD()
    {
        myFixture.configureByFiles("CodeDuplicationAnalysisTestData1.java", "CodeDuplicationAnalysisTestData2.java");

        //placing data context for the action menu
        MapDataContext dataContext = new MapDataContext();
        dataContext.put(CommonDataKeys.PROJECT, myFixture.getProject());
        dataContext.put(CommonDataKeys.EDITOR, myFixture.getEditor());
        dataContext.put(CommonDataKeys.PSI_FILE, myFixture.getFile());
        AnActionEvent event = AnActionEvent.createFromDataContext(ActionPlaces.CONTEXT_TOOLBAR, new Presentation(), dataContext);

        //mocking the action
        CodeDuplicationAnalysis action = new CodeDuplicationAnalysis();
        action.actionPerformed(event);

        //components for Jtable
        Component[] d = action.getOutput().getComponents();
        assertNotNull(d);
    }

    @Override
    protected String getTestDataPath()
    {
        return "src/test/resources/";
    }
}

