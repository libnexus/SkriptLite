package io.github.libnexus.sklite.api;

public interface ScriptElement {

    Object run(SkContext context);

    record Literal(Object value) implements ScriptElement {
        @Override
        public Object run(SkContext context) {
            return value;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }
    }

    Class<?> getType();
}