package com.example.seng4430plugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.github.javaparser.TokenRange;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeDuplicationAnalysis extends AbstractCodeAnalyzerAction
{
    private static final int MIN_TOKEN_COUNT = 10;

    public CodeDuplicationAnalysis()
    {
        super("Analyze code duplication", "Analyze code duplication in the code base", getIcon());
    }

    @Override
    protected void analyze() {
            try {
                // Collect all Java files in the project
                List<Path> javaFiles = collectJavaFiles(Paths.get(project.getBasePath()));

                // Parse and normalize the source code of each Java file
                Map<Path, CompilationUnit> parsedCode = parseCode(javaFiles);

                // Find duplicates by comparing the ASTs
                Map<String, List<Path>> duplicates = findDuplicates(parsedCode);

                // Display the results in your dialog
                displayResults(duplicates);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    public Map<String, List<Path>> findDuplicates(Map<Path, CompilationUnit> parsedCode)
    {
        Map<String, List<Path>> duplicates = new HashMap<>();
        Map<String, List<Path>> methodHashes = new HashMap<>();

        for (Map.Entry<Path, CompilationUnit> entry : parsedCode.entrySet())
        {
            Path filePath = entry.getKey();
            CompilationUnit cu = entry.getValue();

            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class))
            {
                String methodCode = method.toString();
                String methodSignature = method.getDeclarationAsString();

                // Skip methods with fewer tokens than the minimum token count
                if (countTokens(methodCode) < MIN_TOKEN_COUNT)
                {
                    continue;
                }

                String hash = hashMethodCode(methodCode);

                if (!methodHashes.containsKey(hash))
                {
                    methodHashes.put(hash, new ArrayList<>());
                }
                methodHashes.get(hash).add(filePath);

                if (methodHashes.get(hash).size() > 1)
                {
                    duplicates.put(methodSignature, methodHashes.get(hash));
                }
            }
        }

        return duplicates;
    }


    public int countTokens(String code)
    {
        try
        {
            CompilationUnit cu = new JavaParser().parse(code).getResult().orElse(null);
            if (cu != null)
            {
                TokenRange tokenRange = cu.getTokenRange().orElse(null);
                if (tokenRange != null)
                {
                    int tokenCount = 0;
                    JavaToken token = tokenRange.getBegin();
                    while (token != null && !token.getRange().isEmpty())
                    {
                        tokenCount++;
                        token = token.getNextToken().orElse(null);
                    }
                    return tokenCount;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }


    public String hashMethodCode(String methodCode)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(methodCode.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return "";
        }
    }


    private void displayResults(Map<String, List<Path>> duplicates)
    {
        if (duplicates.isEmpty())
        {
            output.add(createTextBox("No duplicate code found."));
        }
        else
        {
            // Find the maximum number of paths for a code snippet
            int maxPaths = duplicates.values().stream().mapToInt(List::size).max().orElse(1);

            // Create column names
            String[] columnNames = new String[1 + maxPaths];
            columnNames[0] = "Code Snippet";
            for (int i = 1; i <= maxPaths; i++)
            {
                columnNames[i] = "File Path " + i;
            }

            String[][] resultsData = new String[duplicates.size()][1 + maxPaths];
            int i = 0;
            for (Map.Entry<String, List<Path>> entry : duplicates.entrySet())
            {
                String codeSnippet = entry.getKey();
                resultsData[i][0] = codeSnippet;

                List<Path> filePaths = entry.getValue();
                for (int j = 0; j < filePaths.size(); j++)
                {
                    resultsData[i][j + 1] = filePaths.get(j).toString();
                }
                i++;
            }

            JTable resultsTable = new JTable(resultsData, columnNames);

            // Set renderer for clickable paths
            for (int j = 1; j <= maxPaths; j++)
            {
                resultsTable.getColumn("File Path " + j).setCellRenderer(new ClickablePathRenderer());
            }

            resultsTable.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    int row = resultsTable.rowAtPoint(e.getPoint());
                    int col = resultsTable.columnAtPoint(e.getPoint());
                    if (col > 0)
                    {
                        String pathString = (String) resultsTable.getValueAt(row, col);
                        if (pathString != null && !pathString.isEmpty())
                        {
                            Path filePath = Paths.get(pathString);
                            openFileInEditor(project, filePath);
                        }
                    }
                }
            });
            JScrollPane resultsScrollPane = new JScrollPane(resultsTable);
            output.add(resultsScrollPane, BorderLayout.CENTER);
        }
    }
}
