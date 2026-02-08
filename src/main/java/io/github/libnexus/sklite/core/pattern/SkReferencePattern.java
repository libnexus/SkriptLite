package io.github.libnexus.sklite.core.pattern;

import io.github.libnexus.sklite.core.SkParser;
import io.github.libnexus.sklite.core.SkPatternNode;

public class SkReferencePattern extends SkPattern {
    private final SkParser parser;
    private final String targetPatternName;
    private final Class<?> expectedType;
    private Object lastValue;
    private int lastConsumed;

    public SkReferencePattern(SkParser parser, String targetPatternName, Class<?> expectedType) {
        this.parser = parser;
        this.targetPatternName = targetPatternName;
        this.expectedType = expectedType;
    }

    public String getTargetPatternName() {
        return targetPatternName;
    }

    @Override
    public SkPattern match(String input) {
        SkPatternNode targetRoot = parser.getPattern(targetPatternName);
        if (targetRoot == null) return null;


        var result = parser.matchGraph(targetRoot, input);

        if (result != null) {
            if (expectedType != null && !expectedType.isInstance(result.value().getType())) {
                return null;
            }

            this.lastConsumed = result.consumed();
            this.lastValue = result.value();
            return this;
        }

        return null;
    }

    public int getLastConsumed() {
        return lastConsumed;
    }

    public Object getLastValue() {
        return lastValue;
    }
}