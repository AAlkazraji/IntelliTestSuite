package com.example.seng4430plugin;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CyclomaticComplexityAnalysis extends AbstractCodeAnalyzerAction
{

    public int COMPLEXITY_THRESHOLD = 5;

    public CyclomaticComplexityAnalysis()
    {
        super("Analyze cyclomatic complexity", "Analyze cyclomatic complexity for all Java methods in the code base", getIcon());
    }

    public CyclomaticComplexityAnalysis(int threshold)
    {
        super("Analyze cyclomatic complexity", "Analyze cyclomatic complexity for all Java methods in the code base", getIcon());
        COMPLEXITY_THRESHOLD = threshold;
    }

    @Override
    protected void analyze()
    {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
        {
            try
            {
                // Collect all Java files in the project
                List<Path> javaFiles = collectJavaFiles(Paths.get(project.getBasePath()));

                // Parse the source code of each Java file
                Map<Path, CompilationUnit> parsedCode = parseCode(javaFiles);

                // Calculate cyclomatic complexity for each method in the parsed Java files
                Map<String, Map<Path, Integer>> highComplexityMethods = calculateComplexity(parsedCode);

                // Display the results
                ApplicationManager.getApplication().invokeLater(() -> displayResults(highComplexityMethods));

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    public Map<String, Map<Path, Integer>> calculateComplexity(Map<Path, CompilationUnit> parsedCode){
        Map<String, Map<Path, Integer>> highComplexityMethods = new HashMap<>();
        for (Map.Entry<Path, CompilationUnit> entry : parsedCode.entrySet())
        {
            CompilationUnit cu = entry.getValue();
            Path filePath = entry.getKey();

            cu.findAll(MethodDeclaration.class).forEach(method ->
            {
                int complexity = calculateCyclomaticComplexity(method);

                if (complexity >= COMPLEXITY_THRESHOLD)
                {
                    String methodSignature = method.getDeclarationAsString(false, false, false);
                    highComplexityMethods.computeIfAbsent(methodSignature, k -> new HashMap<>()).put(filePath, complexity);
                }
            });
        }
        return highComplexityMethods;
    }

    public void waitForUiUpdate() {
        try {
            SwingUtilities.invokeAndWait(() -> {});
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void displayResults(Map<String, Map<Path, Integer>> highComplexityMethods)
    {
        SwingUtilities.invokeLater(() ->
        {


            String[] columnNames = {"Method", "File Path", "Cyclomatic Complexity"};
            List<String[]> resultsData = new ArrayList<>();

            if (highComplexityMethods.isEmpty())
            {
                output.add(createTextBox("No methods with high cyclomatic complexity found."));
            } else
            {
                for (Map.Entry<String, Map<Path, Integer>> entry : highComplexityMethods.entrySet())
                {
                    String methodName = "Method: " + entry.getKey();
                    for (Map.Entry<Path, Integer> fileEntry : entry.getValue().entrySet())
                    {
                        resultsData.add(new String[]{methodName, fileEntry.getKey().toString(), fileEntry.getValue().toString()});
                    }
                }

                JTable resultsTable = new JTable(resultsData.toArray(new String[0][0]), columnNames);

                // Set custom cell renderer to make the method or file name look like a hyperlink
                resultsTable.setDefaultRenderer(Object.class, new TableCellRenderer()
                {
                    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
                    {
                        Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (c instanceof JLabel)
                        {
                            JLabel label = (JLabel) c;
                            if (column == 0 || column == 1)
                            {
                                if (column == 1)
                                {
                                    label.setForeground(Color.CYAN);
                                } else
                                {
                                    label.setForeground(Color.WHITE);
                                }
                                label.setText("<html><u>" + value.toString() + "</u></html>");
                            } else
                            {
                                label.setForeground(Color.WHITE);
                            }
                        }
                        return c;
                    }
                });

                // Add a custom MouseAdapter to handle click events on the table
                resultsTable.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        int row = resultsTable.rowAtPoint(e.getPoint());
                        int col = resultsTable.columnAtPoint(e.getPoint());

                        if (row >= 0 && col >= 0 && (col == 0 || col == 1))
                        {
                            String filePath = resultsTable.getModel().getValueAt(row, 1).toString();
                            // Navigate to the file location in IntelliJ
                            openFileInEditor(project, Paths.get(filePath));
                        }
                    }
                });

                JScrollPane resultsScrollPane = new JScrollPane(resultsTable);
                output.add(resultsScrollPane, BorderLayout.CENTER);
            }
        });
    }

    private int calculateCyclomaticComplexity(MethodDeclaration method)
    {
        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
        method.accept(visitor, null);
        return visitor.getComplexity();
    }
}
