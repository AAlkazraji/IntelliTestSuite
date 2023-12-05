package com.example.seng4430plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class HalsteadAnalysis extends AbstractCodeAnalyzerAction
{

    private List<Object[]> results;

    public HalsteadAnalysis()
    {
        super("Analyze Halstead Class level", "Analyze Halstead class level", null);
    }

    // Getter method for the results field
    public List<Object[]> getResults() {
        return results;
    }

    // Setter method to set the psiJavaFile field for testing purposes
    public void setPsiJavaFileForTesting(PsiJavaFile psiJavaFile) {
        this.psiJavaFile = psiJavaFile;
    }

    @Override
    public void analyze() {
        results = new ArrayList<>();

        if (psiJavaFile != null)
        {
            PsiClass[] classes = psiJavaFile.getClasses();
            for (PsiClass clazz : classes)
            {
                PsiMethod[] methods = clazz.getMethods();
                for (PsiMethod method : methods)
                {
                    if (!isMainMethod(method))
                    {
                        analyzeMethod(method);
                    }
                }
            }
        }
        DefaultTableModel tableModel = new DefaultTableModel();
        // Add columns to the table model
        tableModel.addColumn("Method Name");
        tableModel.addColumn("Path");
        tableModel.addColumn("Metric");
        tableModel.addColumn("Value");
        // Add the result data to the table model
        for (Object[] result : results)
        {
            tableModel.addRow(result);
        }
        // Create the JTable using the table model
        JBTable table = new JBTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        output.add(scrollPane, BorderLayout.CENTER);
    }

    private void analyzeMethod(PsiMethod method) {
        // Perform Halstead analysis for the given method
        if (method != null)
        {
            String methodName = method.getName();
            String methodPath = method.getContainingFile().getVirtualFile().getPath();

            int n1 = 0; // Operand count (distinct operators and operands)
            int n2 = 0; // Operator count
            int N1 = 0; // Operand count (total operands)
            int N2 = 0; // Operator count (total operators)
            Set<String> operators = new HashSet<>();

            PsiElement[] children = method.getChildren();
            for (PsiElement child : children)
            {
                if (child instanceof PsiJavaToken)
                {
                    PsiJavaToken token = (PsiJavaToken) child;
                    String text = token.getText();
                    if (isOperator(text))
                    {
                        operators.add(text);
                        n2++;
                        N2++;
                    } else if (isOperand(token))
                    {
                        n1++;
                        N1++;
                    }
                }
            }

            int N = n1 + n2; // Program Vocabulary
            int L = N1 + N2; // Program Length
            double V = N * Math.log(N) / Math.log(2); // Volume
            double D = (n1 / 2.0) * (N2 / (double) n2); // Difficulty
            double E = V * D; // Effort
            double T = E / 18.0; // Time Required
            double B = (E * E) / 3000.0; // Number of Bugs

            // Add the result data to the list
            results.add(new Object[]{methodName, methodPath, "Operand Count (n1)", n1});
            results.add(new Object[]{methodName, methodPath, "Operator Count (n2)", n2});
            results.add(new Object[]{methodName, methodPath, "Operand Count (N1)", N1});
            results.add(new Object[]{methodName, methodPath, "Operator Count (N2)", N2});
            results.add(new Object[]{methodName, methodPath, "Program Vocabulary (N)", N});
            results.add(new Object[]{methodName, methodPath, "Program Length (L)", L});
            results.add(new Object[]{methodName, methodPath, "Volume (V)", V});
            results.add(new Object[]{methodName, methodPath, "Difficulty (D)", D});
            results.add(new Object[]{methodName, methodPath, "Effort (E)", E});
            results.add(new Object[]{methodName, methodPath, "Time Required (T)", T});
            results.add(new Object[]{methodName, methodPath, "Number of Bugs (B)", B});
        }
    }

    private boolean isOperator(String text) {
        // Set of known operator symbols
        Set<String> operatorSymbols = new HashSet<>();
        operatorSymbols.add("+");
        operatorSymbols.add("-");
        operatorSymbols.add("*");
        operatorSymbols.add("/");
        // Add more operator symbols as needed

        return operatorSymbols.contains(text);
    }

    private boolean isOperand(PsiJavaToken token) {
        // Check if the token is an operand
        return token.getTokenType().equals(JavaTokenType.IDENTIFIER);
    }

    private boolean isMainMethod(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        if (modifierList != null)
        {
            String methodName = method.getName();
            String[] modifiers = modifierList.getText().split(" ");
            boolean isPublic = false;
            boolean isStatic = false;
            boolean isVoid = false;

            for (String modifier : modifiers)
            {
                if (modifier.equals(PsiModifier.PUBLIC))
                {
                    isPublic = true;
                } else if (modifier.equals(PsiModifier.STATIC))
                {
                    isStatic = true;
                }
            }

            PsiType returnType = method.getReturnType();
            if (returnType != null && returnType.equals(PsiType.VOID))
            {
                isVoid = true;
            }

            return isPublic && isStatic && isVoid && methodName.equals("main");
        }
        return false;
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
                e.getPresentation().setText("Halsted Analysis for Methods in '" + className + "'");
            }
        }
    }

}