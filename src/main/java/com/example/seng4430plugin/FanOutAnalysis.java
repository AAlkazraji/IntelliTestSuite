/*
 * Classname: FanOutAnalysis
 * Programmer: Josh O'Brien
 * Version: Java 17
 * Date: 10/05/2023
 * Description: DES class for the base algorithm with no changes to it
 */

package com.example.seng4430plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class FanOutAnalysis extends AbstractCodeAnalyzerAction
{
    public FanOutAnalysis()
    {
        super("Analyze Fan-out", "Analyze the fan-out of the selected Java method or class", null);
    }

    @Override
    protected void analyze()
    {
        StringBuilder outputText = new StringBuilder();
        //getting all methods of class
        PsiMethod[] methods = PsiTreeUtil.findChildrenOfType( psiJavaFile, PsiMethod.class ).toArray( PsiMethod[]::new );
        for ( PsiMethod method : methods )
        {
            //adding result to output
            String formattedOutput = String.format( "%-30s%-10d\n\n", method.getName(), performFanOutAnalysis( method, project ) );
            outputText.append( formattedOutput );
        }
        //adding textbox to output window
        output.add( createTextBox(outputText.toString()) );
    }


    //method which displays which class the metric will run on
    @Override
    public void update(@NotNull AnActionEvent e)
    {
        super.update(e);
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project != null && editor != null && psiFile instanceof PsiJavaFile)
        {
            int caretOffset = editor.getCaretModel().getOffset();
            PsiElement psiElement = psiFile.findElementAt(caretOffset);
            PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
            if (psiClass != null)
            {
                String className = psiClass.getName();
                e.getPresentation().setText("Fan-out Analysis for Methods in '" + className + "'");
            }
        }
    }

    public int performFanOutAnalysis(PsiMethod targetMethod, Project project)
    {
        //getting all unique calls in method
        PsiCallExpression[] callExpressions = PsiTreeUtil.findChildrenOfType(targetMethod.getBody(), PsiCallExpression.class).toArray(PsiCallExpression[]::new);
        Set<String> uniqueMethodCalls = new HashSet<>();
        for (PsiCallExpression callExpression : callExpressions)
        {
            PsiElement element = callExpression.resolveMethod();
            if (element != null)
            {
                //getting context of the method
                PsiMethod method = (PsiMethod) element;
                PsiFile methodFile = method.getContainingFile();
                VirtualFile virtualFile = methodFile.getVirtualFile();
                ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                //determining if the method is in the project or external
                if (fileIndex.isInSourceContent(virtualFile))
                {
                    uniqueMethodCalls.add(method.getName());
                }
            }
        }
        return uniqueMethodCalls.size();
    }
}
