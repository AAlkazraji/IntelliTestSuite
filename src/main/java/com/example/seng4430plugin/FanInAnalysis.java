/*
 * Classname: FanInAnalysis
 * Programmer: Josh O'Brien
 * Version: Java 17
 * Date: 10/05/2023
 * Description: Analysis class for the fan in metric
 */

package com.example.seng4430plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class FanInAnalysis extends AbstractCodeAnalyzerAction
{
    public FanInAnalysis()
    {
        super("Analyze Fan-in", "Analyze the fan-in of the selected Java method or class", null);
    }

    @Override
    protected void analyze()
    {
        StringBuilder outputText = new StringBuilder();
        PsiMethod[] methods = PsiTreeUtil.findChildrenOfType(psiJavaFile, PsiMethod.class).toArray(PsiMethod[]::new);
        for (PsiMethod method : methods)
        {
            //removing abstract methods
            if (method.getBody() != null)
            {
                String formattedOutput = String.format("%-30s%-10d\n\n", method.getName(), performFanInAnalysis(method, project));
                outputText.append( formattedOutput );
            }
        }
        output.add(createTextBox(outputText.toString()));
    }

    //displays which class the metric will run on
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
                e.getPresentation().setText("Fan-in Analysis for Methods in '" + className + "'");
            }
        }
    }

    public int performFanInAnalysis(PsiMethod targetMethod, Project project)
    {
        PsiShortNamesCache cache = PsiShortNamesCache.getInstance(project);
        PsiMethod[] methodsWithSameName = cache.getMethodsByName( targetMethod.getName(), GlobalSearchScope.projectScope(project) );
        int fanInCount = 0;
        for (PsiMethod method : methodsWithSameName)
        {
            if (method.getSignature(PsiSubstitutor.EMPTY).equals(targetMethod.getSignature(PsiSubstitutor.EMPTY)) &&
                    method.getContainingClass().equals(targetMethod.getContainingClass()))
            {
                long recursiveCalls = Arrays.stream(PsiTreeUtil.findChildrenOfType(method.getBody(), PsiCallExpression.class).toArray(PsiCallExpression[]::new))
                        .filter(expression ->
                        {
                            PsiMethod calledMethod = expression.resolveMethod();
                            if (calledMethod != null && calledMethod.getName().equals(targetMethod.getName()))
                            {
                                PsiMethod enclosingMethod = PsiTreeUtil.getParentOfType(expression, PsiMethod.class);
                                return enclosingMethod != null && enclosingMethod.equals(calledMethod);
                            }
                            return false;
                        })
                        .count();
                fanInCount += ( ReferencesSearch.search(method).findAll().size()-recursiveCalls );
            }
        }
        return fanInCount;
    }
}