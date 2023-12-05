package com.example.seng4430plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.TypeParameter;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CohesionClassAnalysis extends AbstractCodeAnalyzerAction
{

    public String[][] data;

    public CohesionClassAnalysis() {
        super("Analyze Cohesion Class level ", "Analyze Cohesion class level ", null);
    }

    // Add the getter method for the data variable
    public String[][] getData() {
        return data;
    }

    @Override
    public void analyze() {

        if (project != null) {
            // Your analysis code here
            PsiManager psiManager = PsiManager.getInstance(project);
            // Rest of your code
        } else {
            System.out.println("Project is null. Unable to perform analysis.");
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<PsiJavaFile> javaFiles = new ArrayList<>();

            PsiManager psiManager = PsiManager.getInstance(project);

            ApplicationManager.getApplication().runReadAction(() ->
            {
                Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
                for (VirtualFile virtualFile : virtualFiles)
                {
                    PsiFile psiFile = psiManager.findFile(virtualFile);
                    if (psiFile instanceof PsiJavaFile)
                    {
                        javaFiles.add((PsiJavaFile) psiFile);
                    }
                }

                // Analyze each Java file and calculate the cohesion of each class
                Map<String, Double> classCohesion = new HashMap<>();
                for (PsiJavaFile javaFile : javaFiles)
                {
                    if (javaFile.getClasses().length == 0)
                    {
                        continue;
                    }
                    PsiClass psiClass = javaFile.getClasses()[0];
                    String filePath = javaFile.getVirtualFile().getPath(); // Get the file path
                    PsiMethod[] methods = psiClass.getMethods();
                    Map<String, Integer> methodCalls = new HashMap<>();
                    Map<String, Integer> classMethodCalls = new HashMap<>();

                    // Calculate the number of method calls made by each method
                    for (PsiMethod method : methods)
                    {
                        method.accept(new JavaRecursiveElementVisitor()
                        {
                            @Override
                            public void visitMethodCallExpression(PsiMethodCallExpression expression)
                            {
                                String methodName = expression.getMethodExpression().getReferenceName();
                                if (methodName != null)
                                {
                                    Integer count = methodCalls.getOrDefault(methodName, 0);
                                    methodCalls.put(methodName, count + 1);
                                }
                                super.visitMethodCallExpression(expression);
                            }
                        });
                    }

                    // Calculate the number of method calls made to other methods in the same class
                    for (PsiMethod method : methods)
                    {
                        String methodName = method.getName();
                        method.accept(new JavaRecursiveElementVisitor()
                        {
                            @Override
                            public void visitMethodCallExpression(PsiMethodCallExpression expression)
                            {
                                PsiMethod calledMethod = (PsiMethod) expression.getMethodExpression().resolve();
                                if (calledMethod != null && calledMethod.getContainingClass() == psiClass)
                                {
                                    Integer count = classMethodCalls.getOrDefault(methodName, 0);
                                    classMethodCalls.put(methodName, count + 1);
                                }
                                super.visitMethodCallExpression(expression);
                            }
                        });
                    }

                    // Calculate the cohesion of each class
                    final int[] methodCallCount = {0};
                    Set<String> calledMethods = new HashSet<>();
                    for (PsiMethod method : methods)
                    {
                        methodCallCount[0] += methodCalls.getOrDefault(method.getName(), 0);
                        calledMethods.addAll(classMethodCalls.keySet());
                    }
                    if (methodCallCount[0] == 0 || calledMethods.size() == 0)
                    {
                        classCohesion.put(filePath + "." + psiClass.getName(), 0.0); // Add the file path to the class name
                    } else
                    {
                        double cohesion = 0;
                        for (String calledMethod : calledMethods)
                        {
                            int count = classMethodCalls.getOrDefault(calledMethod, 0);
                            cohesion += (double) count / methodCallCount[0];
                        }
                        cohesion /= calledMethods.size();
                        classCohesion.put(filePath + "." + psiClass.getName(), cohesion); // Add the file path to the class name
                    }
                }

                String[][] data = new String[classCohesion.size()][2];

                // Populate the array with the class names and cohesion values
                int i = 0;
                for (String className : classCohesion.keySet())
                {
                    double cohesion = classCohesion.get(className);
                    String cohesionStr = String.format("%.2f", cohesion); // format cohesion to 2 decimal places
                    data[i][0] = className;
                    data[i][1] = cohesionStr;
                    i++;
                }

                SwingUtilities.invokeLater(() ->
                {
                    JTable table = new JTable(data, new String[]{"Class Name", "Cohesion"});
                    table.setFillsViewportHeight(true);

                    JScrollPane scrollPane = new JBScrollPane(table);
                    scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                    output.add(scrollPane, BorderLayout.CENTER);
                });
            });
        });
    }

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
                e.getPresentation().setText("Cohesion Analysis for Methods in '" + className + "'");
            }
        }
    }

}

