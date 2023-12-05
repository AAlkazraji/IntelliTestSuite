/*
 * Classname: AbstractCodeAnalyzerAction
 * Programmer: Collaborative
 * Version: Java 17
 * Date: 16/05/2023
 * Description: General class for metric actions, responsible for handling action events
 *              and for outputting results to the window
 */


package com.example.seng4430plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCodeAnalyzerAction extends AnAction
{
    protected PsiJavaFile psiJavaFile;
    protected Project project;
    protected JPanel output;

    public AbstractCodeAnalyzerAction(@NotNull String text, @NotNull String description, Icon icon)
    {
        //shows the action label and optional label in the window
        super(text, description, icon);
    }

    protected static Icon getIcon()
    {
        return IconLoader.getIcon("/META-INF/logo.png", AbstractCodeAnalyzerAction.class);
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        //gets context of the project
        project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        initializeOutputWindow();
        //sets java file for class metrics
        if ( project != null && editor != null && psiFile instanceof PsiJavaFile )
        {
            psiJavaFile = (PsiJavaFile) psiFile;
        }
        analyze();
    }

    protected abstract void analyze();

    public Map<Path, CompilationUnit> parseCode(List<Path> javaFiles) throws IOException
    {
        Map<Path, CompilationUnit> parsedCode = new HashMap<>();
        JavaParser javaParser = new JavaParser();
        for (Path filePath : javaFiles)
        {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(filePath.toFile());
            if (virtualFile != null)
            {
                String sourceCode = VfsUtilCore.loadText(virtualFile);
                javaParser.parse(sourceCode).getResult().ifPresent(cu -> parsedCode.put(filePath, cu));
            }
        }
        return parsedCode;
    }

    public void initializeOutputWindow()
    {
        //adding a panel to the tool window
        output = new JPanel(new BorderLayout());
        EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
        output.setBackground(colorsScheme.getDefaultBackground());

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        //resetting the result window is it is present from prev action
        if (toolWindowManager.getToolWindow("CodeAnalyzr") != null)
        {
            toolWindowManager.unregisterToolWindow("CodeAnalyzr");
        }
        //displaying the new tool window
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("CodeAnalyzr", false, ToolWindowAnchor.BOTTOM);
        Content content = ContentFactory.SERVICE.getInstance().createContent(output, "", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.show();
    }

    protected static List<Path> collectJavaFiles(Path directory) throws IOException
    {
        try (Stream<Path> walk = Files.walk(directory))
        {
            return walk.filter(Files::isRegularFile).filter(file -> file.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    protected static void openFileInEditor(Project project, Path filePath)
    {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(filePath.toFile());
        if (virtualFile != null)
        {
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        } else
        {
            String message = "File not found: " + filePath.toString();
            Messages.showErrorDialog(project, message, "Error");
        }
    }

    //helper method for integration tests
    public JTextArea getOutputText()
    {
        return (JTextArea) output.getComponent(0);
    }

    public JPanel getOutput()
    {
        return output;
    }

    protected JTextArea createTextBox(String text)
    {
        JTextArea outputText = new JTextArea(text);

        //default intellij settings
        EditorColorsScheme colorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
        outputText.setFont(colorsScheme.getFont(EditorFontType.PLAIN));
        outputText.setForeground(colorsScheme.getDefaultForeground());
        outputText.setBackground(colorsScheme.getDefaultBackground());

        //window view settings
        outputText.setEditable(false);
        outputText.setLineWrap(true);
        outputText.setWrapStyleWord(true);
        return outputText;
    }
}
