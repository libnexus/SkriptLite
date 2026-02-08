package io.github.libnexus.sklite.core;

import io.github.libnexus.sklite.api.ScriptElement;
import io.github.libnexus.sklite.core.pattern.SkPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SkPatternNode {

    public static final SkPatternNode ROOT = new SkPatternNode(null, null);

    private final List<SkPatternNode> children = new ArrayList<>();
    private final SkPattern pattern;
    private final String captureKey;

    
    private Function<Map<String, ScriptElement>, ScriptElement> elementFactory;

    public SkPatternNode(SkPattern pattern, String captureKey) {
        this.pattern = pattern;
        this.captureKey = captureKey;
    }

    public void addChild(SkPatternNode child) {
        this.children.add(child);
    }

    public List<SkPatternNode> getChildren() {
        return children;
    }

    public SkPattern getPattern() {
        return pattern;
    }

    public String getCaptureKey() {
        return captureKey;
    }

    public void setElementFactory(Function<Map<String, ScriptElement>, ScriptElement> factory) {
        this.elementFactory = factory;
    }

    public Function<Map<String, ScriptElement>, ScriptElement> getElementFactory() {
        return elementFactory;
    }
}