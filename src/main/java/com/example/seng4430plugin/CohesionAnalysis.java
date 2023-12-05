package com.example.seng4430plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.TypeParameter;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
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
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CohesionAnalysis extends AbstractCodeAnalyzerAction
{
    int methodCallCount = 0;

    public CohesionAnalysis()
    {
        super("Analyze Cohesion", "Analyze Cohesion", getIcon());
    }

    @Override
    protected void analyze()
    {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
        {
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

                // Analyze each Java file and calculate the cohesion of each method
                Map<String, Double> methodCohesion = new HashMap<>();
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

                    // Calculate the cohesion of each method
                    for (PsiMethod method : methods)
                    {
                        final int[] methodCallCount = {0};
                        Set<String> calledMethods = new HashSet<>();
                        method.accept(new JavaRecursiveElementVisitor()
                        {
                            @Override
                            public void visitMethodCallExpression(PsiMethodCallExpression expression)
                            {
                                String methodName = expression.getMethodExpression().getReferenceName();
                                if (methodName != null)
                                {
                                    methodCallCount[0]++;
                                    calledMethods.add(methodName);
                                }
                                super.visitMethodCallExpression(expression);
                            }
                        });
                        if (methodCallCount[0] == 0)
                        {
                            methodCohesion.put(filePath + "." + method.getName(), 0.0); // Add the file path to the method name
                        } else
                        {
                            double cohesion = 0;
                            for (String calledMethod : calledMethods)
                            {
                                int count = methodCalls.getOrDefault(calledMethod, 0);
                                cohesion += (double) count / methodCallCount[0];
                            }
                            cohesion /= calledMethods.size();
                            methodCohesion.put(filePath + "." + method.getName(), cohesion); // Add the file path to the method name
                        }
                    }
                }

                // Display the results in a dialog
                Map<String, String[]> resultsData = new HashMap<>();
                for (String methodName : methodCohesion.keySet())
                {
                    Double cohesion = methodCohesion.get(methodName);
                    String cohesionString = cohesion != null ? String.format("%.2f", cohesion) : "N/A";
                    String[] methodData = new String[]{cohesionString, methodName};
                    resultsData.put(methodName, methodData);
                }

                SwingUtilities.invokeLater(() -> displayResults(resultsData));
            });
        });
    }

    public void displayResults(Map<String, String[]> resultsData)
    {
        // Create table model and table
        String[] columnNames = {"Method", "Cohesion score", "Path"};
        Object[][] rowData = new Object[resultsData.size()][3];
        int i = 0;
        for (String methodNameWithPath : resultsData.keySet())
        {
            String[] methodData = resultsData.get(methodNameWithPath);
            String[] methodNameWithPathParts = methodNameWithPath.split("\\."); // split the path and method name
            String methodName = methodNameWithPathParts[methodNameWithPathParts.length - 1]; // get the last part as method name
            String filePath = methodNameWithPath.substring(0, methodNameWithPath.lastIndexOf(".")); // get the path by removing the method name

            double cohesion = Double.parseDouble(methodData[0]);
            String cohesionStr = String.format("%.2f", cohesion); // format cohesion to 2 decimal places

            rowData[i][0] = methodName; // set the method name in the first column
            rowData[i][1] = cohesionStr; // set the cohesion score in the second column
            rowData[i][2] = filePath; // set the path in the third column
            i++;
        }
        JTable table = new JTable(rowData, columnNames);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        output.add(scrollPane, BorderLayout.CENTER);
    }

}
