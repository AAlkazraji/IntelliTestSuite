/*
 * Classname: FanInAnalysisIntegrationTest
 * Programmer: Josh O'Brien
 * Version: Java 17
 * Date: 15/05/2023
 * Description: Integration test which tests the whole workflow of the
 *              Fan-out analysis action
 */

package integration;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.example.seng4430plugin.FanInAnalysis;

public class FanInAnalysisIntegrationTest extends BasePlatformTestCase {

    public void testFanInAnalysis()
    {
        myFixture.configureByFile("FanClasses.java");

        //placing data context for the action menu
        MapDataContext dataContext = new MapDataContext();
        dataContext.put(CommonDataKeys.PROJECT, myFixture.getProject());
        dataContext.put(CommonDataKeys.EDITOR, myFixture.getEditor());
        dataContext.put(CommonDataKeys.PSI_FILE, myFixture.getFile());
        AnActionEvent event = AnActionEvent.createFromDataContext(ActionPlaces.CONTEXT_TOOLBAR, new Presentation(), dataContext);

        //mocking the action
        FanInAnalysis action = new FanInAnalysis();
        action.actionPerformed(event);

        //testing the output
        String output = action.getOutputText().getText();
        assertTrue( output.contains(
                "abstractMethod                1         \n" + "\n" +
                "method1                       1         \n" + "\n" +
                "methodOverload                1         \n" + "\n" +
                "methodOverload                1         \n" + "\n" +
                "methodOverload                1         \n" + "\n" +
                "methodRecursive               1         \n" + "\n" +
                "abstractMethod                0         \n" + "\n" +
                "method1                       1         \n" + "\n" +
                "method2                       2    ") );
    }
    @Override
    protected String getTestDataPath()
    {
        return "src/test/resources/";
    }
}

