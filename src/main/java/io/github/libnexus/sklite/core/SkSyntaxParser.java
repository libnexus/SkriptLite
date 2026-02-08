package io.github.libnexus.sklite.core;

public class SkSyntaxParser {

    public static void apply(SkParser parser, String syntax) {
        char[] chars = syntax.toCharArray();
        StringBuilder buffer = new StringBuilder();
        boolean escape = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (escape) {
                buffer.append(c);
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == ' ') {
                flush(parser, buffer);
            } else if (c == '[') {
                flush(parser, buffer);
                parser.includeOptional();
            } else if (c == ']') {
                flush(parser, buffer);
                parser.endOptional();
            } else if (c == '<') {
                flush(parser, buffer);
                parser.startLoop();
            } else if (c == '>') {
                flush(parser, buffer);
                parser.endLoop();
            } else if (c == '(') {
                flush(parser, buffer);

                int start = i + 1;
                int depth = 1;
                while (depth > 0 && ++i < chars.length) {
                    if (chars[i] == '\\') {
                        i++;
                        continue;
                    }
                    if (chars[i] == '(') depth++;
                    if (chars[i] == ')') depth--;
                }

                String group = new String(chars, start, i - start);


                parser.includeChoice(group.split("\\|"));
            } else {
                buffer.append(c);
            }
        }
        flush(parser, buffer);
    }

    private static void flush(SkParser parser, StringBuilder buffer) {
        if (buffer.isEmpty()) return;
        String token = buffer.toString();
        buffer.setLength(0);

        if (token.startsWith("%") && token.endsWith("%")) {
            String content = token.substring(1, token.length() - 1);
            String[] parts = content.split(":");
            String type = parts[0];
            String key = parts.length > 1 ? parts[1] : null;

            parser.includePattern(type, key);
        } else {
            parser.include(token);
        }
    }
}