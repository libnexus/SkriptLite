package io.github.libnexus.sklite.core.pattern;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkRegexPattern extends SkPattern {
    private final Pattern regex;
    private final Function<String, Object> converter;
    private Object lastValue;
    private int lastConsumed;

    
    public SkRegexPattern(String regex, Function<String, Object> converter) {
        this.regex = Pattern.compile("^" + regex); 
        this.converter = converter;
    }

    @Override
    public SkPattern match(String input) {
        String clean = input.stripLeading();
        Matcher m = regex.matcher(clean);

        if (m.find()) {
            String match = m.group();
            this.lastConsumed = (input.length() - clean.length()) + match.length();

            
            this.lastValue = converter.apply(match);
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