package io.github.libnexus.sklite.registry;

import io.github.libnexus.sklite.api.ScriptElement;
import io.github.libnexus.sklite.api.annotation.Pattern;
import io.github.libnexus.sklite.core.SkParser;
import io.github.libnexus.sklite.core.SkSyntaxParser;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

public class SkriptLoader {

    private final SkParser parser;

    public SkriptLoader(SkParser parser) {
        this.parser = parser;
    }

    public void registerCommand(String syntax, Class<? extends ScriptElement> clazz) {
        parser.command("TEMP_ROOT");
        SkSyntaxParser.apply(parser, syntax);

        Function<Map<String, ScriptElement>, ScriptElement> factory = createFactory(clazz);
        parser.setElementFactory(factory);
    }

    private Function<Map<String, ScriptElement>, ScriptElement> createFactory(Class<? extends ScriptElement> clazz) {
        return (captures) -> {
            try {
                ScriptElement instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Pattern.class)) {
                        String key = field.getAnnotation(Pattern.class).value();
                        if (captures.containsKey(key)) {
                            field.setAccessible(true);
                            field.set(instance, captures.get(key));
                        }
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
            }
        };
    }
}