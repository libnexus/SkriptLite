package io.github.libnexus.sklite.core;

import io.github.libnexus.sklite.api.ScriptElement;
import io.github.libnexus.sklite.core.pattern.SkPattern;

import java.util.*;
import java.util.function.Function;

public class SkParser {

    private static final SkPatternNode ROOT = new SkPatternNode(null, null);
    private final Map<String, SkPatternNode> patterns = new HashMap<>();
    private final Deque<List<SkPatternNode>> optionalStack = new ArrayDeque<>();
    private final Deque<Map<SkPatternNode, Integer>> loopStack = new ArrayDeque<>();
    private List<SkPatternNode> currentTips = new ArrayList<>();

    public SkParser() {
        this.currentTips.add(ROOT);
    }


    public SkParser command(String name) {
        SkPatternNode node = new SkPatternNode(null, null);
        patterns.put(name, node);
        ROOT.addChild(node);
        this.currentTips.clear();
        this.currentTips.add(node);
        return this;
    }

    public SkParser expression(String name) {
        SkPatternNode node = new SkPatternNode(null, null);
        patterns.put(name, node);
        this.currentTips.clear();
        this.currentTips.add(node);
        return this;
    }

    public void setElementFactory(Function<Map<String, ScriptElement>, ScriptElement> factory) {
        for (SkPatternNode tip : currentTips) tip.setElementFactory(factory);
    }


    public SkParser includePattern(String patternName, String captureKey) {
        return include(new SkReferencePattern(this, patternName, null), captureKey);
    }

    public SkParser includePattern(String name, String captureKey, Class<?> expectedType) {
        return include(new SkReferencePattern(this, name, expectedType), captureKey);
    }

    public SkParser include(String literal) {
        return include(new SkSimplePattern(literal), null);
    }

    public SkParser includeGroup(String type, String captureKey) {
        return include(new SkSimplePattern(type), captureKey);
    }

    public SkParser includeRegex(String regex, Function<String, Object> converter, String captureKey) {
        return include(new SkRegexPattern(regex, converter), captureKey);
    }

    public SkParser include(SkPattern pattern) {
        return include(pattern, null);
    }

    private SkParser include(SkPattern pattern, String captureKey) {
        SkPatternNode newNode = new SkPatternNode(pattern, captureKey);
        for (SkPatternNode tip : currentTips) {
            tip.addChild(newNode);
        }
        currentTips.clear();
        currentTips.add(newNode);
        return this;
    }

    public SkParser includeOptional() {
        optionalStack.push(new ArrayList<>(currentTips));
        return this;
    }

    public SkParser endOptional() {
        if (optionalStack.isEmpty()) throw new IllegalStateException("Called endOptional without includeOptional");
        List<SkPatternNode> preOptionalTips = optionalStack.pop();
        currentTips.addAll(preOptionalTips);
        return this;
    }

    public SkParser includeChoice(String... options) {
        List<SkPatternNode> newTips = new ArrayList<>();
        for (SkPatternNode tip : currentTips) {
            for (String option : options) {
                SkPatternNode choiceNode = new SkPatternNode(new SkSimplePattern(option), null);
                tip.addChild(choiceNode);
                newTips.add(choiceNode);
            }
        }
        currentTips = newTips;
        return this;
    }

    public SkPatternNode getPattern(String name) {
        return patterns.get(name);
    }


    public ScriptElement parse(String input) {
        String cleanInput = input.strip();
        GraphResult result = matchGraph(ROOT, cleanInput);

        
        if (result != null
                && result.value != null
                && result.consumed == cleanInput.length()) { 

            return result.value;
        }

        
        if (result != null && result.consumed < cleanInput.length()) {
            System.out.println("Error: syntax '" + input + "' matched partially but failed at: '" + input.substring(result.consumed) + "'");
        }

        return null;
    }

    public GraphResult match(SkPatternNode node, String input, Map<String, ScriptElement> captures, int totalConsumed) {
        String currentInput = input;
        int localConsumed = 0;

        if (node.getPattern() != null) {
            SkPattern pattern = node.getPattern();
            SkPattern match = pattern.match(currentInput);
            if (match == null) return null;

            Object rawValue = null;

            if (pattern instanceof SkSimplePattern) {
                String pat = ((SkSimplePattern) pattern).getPattern();
                localConsumed = pat.length();
                rawValue = pat;
            } else if (pattern instanceof SkRegexPattern) {
                localConsumed = ((SkRegexPattern) pattern).getLastConsumed();
                rawValue = ((SkRegexPattern) pattern).getLastValue();
            } else if (pattern instanceof SkReferencePattern) {
                localConsumed = ((SkReferencePattern) pattern).getLastConsumed();
                
                rawValue = ((SkReferencePattern) pattern).getLastValue();
            }

            if (localConsumed > currentInput.length()) return null;

            
            String rawNext = currentInput.substring(localConsumed);
            String nextInput = rawNext.stripLeading();
            int skippedWhitespace = rawNext.length() - nextInput.length();

            currentInput = nextInput;
            totalConsumed += localConsumed + skippedWhitespace;

            
            if (node.getCaptureKey() != null) {
                
                
                ScriptElement element = (rawValue instanceof ScriptElement)
                        ? (ScriptElement) rawValue
                        : new ScriptElement.Literal(rawValue);

                captures.put(node.getCaptureKey(), element);
            }

            captures.put("@%s".formatted(captures.size()), new ScriptElement.Literal(rawValue));
        }

        
        for (SkPatternNode child : node.getChildren()) {
            Map<String, ScriptElement> nextCaptures = new HashMap<>(captures);
            GraphResult res = match(child, currentInput, nextCaptures, totalConsumed);
            if (res != null) return res;
        }

        
        if (node.getElementFactory() != null) {
            
            ScriptElement builtElement = node.getElementFactory().apply(captures);
            return new GraphResult(builtElement, totalConsumed, new HashMap<>(captures));
        }


        return null;
    }

    public SkParser startLoop() {

        Map<SkPatternNode, Integer> tracker = new HashMap<>();
        for (SkPatternNode tip : currentTips) {
            tracker.put(tip, tip.getChildren().size());
        }
        loopStack.push(tracker);
        return this;
    }


    public SkParser endLoop() {
        if (loopStack.isEmpty()) throw new IllegalStateException("Called endLoop without startLoop");


        Map<SkPatternNode, Integer> parents = loopStack.pop();

        for (Map.Entry<SkPatternNode, Integer> entry : parents.entrySet()) {
            SkPatternNode parent = entry.getKey();
            int startIndex = entry.getValue();
            List<SkPatternNode> children = parent.getChildren();


            for (int i = startIndex; i < children.size(); i++) {
                SkPatternNode loopEntryNode = children.get(i);


                for (SkPatternNode tip : currentTips) {
                    tip.addChild(loopEntryNode);
                }
            }
        }
        return this;
    }
    public GraphResult matchGraph(SkPatternNode root, String input) {
        return recursiveMatch(root, input.stripLeading(), new HashMap<>(), 0);
    }
    private GraphResult recursiveMatch(SkPatternNode node, String input, Map<String, ScriptElement> captures, int totalConsumed) {
        String currentInput = input;
        int localConsumed = 0;

        
        if (node.getPattern() != null) {
            SkPattern pattern = node.getPattern();
            SkPattern match = pattern.match(currentInput);
            if (match == null) return null;

            Object rawValue = null;

            if (pattern instanceof SkSimplePattern) {
                String pat = ((SkSimplePattern) pattern).getPattern();
                localConsumed = pat.length();
                rawValue = pat;
            } else if (pattern instanceof SkRegexPattern) {
                localConsumed = ((SkRegexPattern) pattern).getLastConsumed();
                rawValue = ((SkRegexPattern) pattern).getLastValue();
            } else if (pattern instanceof SkReferencePattern) {
                localConsumed = ((SkReferencePattern) pattern).getLastConsumed();
                rawValue = ((SkReferencePattern) pattern).getLastValue();
            }

            if (localConsumed > currentInput.length()) return null;

            String rawNext = currentInput.substring(localConsumed);
            String nextInput = rawNext.stripLeading();
            int skippedWhitespace = rawNext.length() - nextInput.length();

            currentInput = nextInput;
            totalConsumed += localConsumed + skippedWhitespace;

            if (node.getCaptureKey() != null) {
                ScriptElement element = (rawValue instanceof ScriptElement)
                        ? (ScriptElement) rawValue
                        : new ScriptElement.Literal(rawValue);

                captures.put(node.getCaptureKey(), element);
            }

            captures.put("@%s".formatted(captures.size()), new ScriptElement.Literal(rawValue));
        }

        GraphResult bestResult = null;

        
        for (SkPatternNode child : node.getChildren()) {
            Map<String, ScriptElement> nextCaptures = new HashMap<>(captures);
            GraphResult res = recursiveMatch(child, currentInput, nextCaptures, totalConsumed);

            if (res != null) {
                
                if (bestResult == null || res.consumed > bestResult.consumed) {
                    bestResult = res;
                }
            }
        }

        
        if (bestResult != null) return bestResult;

        
        if (node.getElementFactory() != null) {
            ScriptElement builtElement = node.getElementFactory().apply(captures);
            return new GraphResult(builtElement, totalConsumed, new HashMap<>(captures));
        }

        return null;
    }

    public record ParseResult(Map<String, Object> captures, SkExecutor executor) {
    }


    public record GraphResult(ScriptElement value, int consumed, Map<String, Object> captures) {
    }

}