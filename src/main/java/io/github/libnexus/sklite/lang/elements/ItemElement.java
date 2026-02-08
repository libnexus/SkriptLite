package io.github.libnexus.sklite.lang.elements;

import io.github.libnexus.sklite.api.ScriptElement;
import io.github.libnexus.sklite.api.SkContext;
import io.github.libnexus.sklite.core.SkParser;
import io.github.libnexus.sklite.core.SkSyntaxParser;

public class ItemElement implements ScriptElement {
    public static void register(SkParser parser) {

    }

    @Override
    public Object run(SkContext context) {
        return null;
    }

    @Override
    public Class<?> getType() {
        return ItemElement.class;
    }
}
