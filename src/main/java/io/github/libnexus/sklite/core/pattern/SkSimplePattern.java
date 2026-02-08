package io.github.libnexus.sklite.core.pattern;

public class SkSimplePattern extends SkPattern {
    private final String pattern;

    public SkSimplePattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public SkPattern match(String input) {

        if (input.startsWith(pattern)) {
            return this;
        }
        return null;
    }
}