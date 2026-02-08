package io.github.libnexus.sklite.debug;

import io.github.libnexus.sklite.core.SkPatternNode;
import io.github.libnexus.sklite.core.pattern.SkReferencePattern;
import io.github.libnexus.sklite.core.pattern.SkSimplePattern;

import java.util.ArrayList;
import java.util.List;

public class SkSyntaxPrinter {

    public static List<String> generateSyntax(SkPatternNode root) {
        List<String> results = new ArrayList<>();
        collectSyntax(root, "", results);
        return results;
    }

    private static void collectSyntax(SkPatternNode node, String currentPath, List<String> results) {

        String token = "";

        if (node.getPattern() instanceof SkSimplePattern) {
            token = ((SkSimplePattern) node.getPattern()).getPattern();
        } else if (node.getPattern() instanceof SkReferencePattern) {

            String refName = ((SkReferencePattern) node.getPattern()).getTargetPatternName();
            token = "%" + refName + "%";
        }


        if (!currentPath.isEmpty() && !token.isEmpty()) {
            currentPath += " ";
        }
        currentPath += token;


        if (node.getElementFactory() != null) {
            results.add(currentPath);
        }


        if (node.getChildren().isEmpty()) {
            return;
        }


        for (SkPatternNode child : node.getChildren()) {
            collectSyntax(child, currentPath, results);
        }
    }
}