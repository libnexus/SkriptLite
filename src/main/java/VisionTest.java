import io.github.libnexus.sklite.api.ScriptElement;
import io.github.libnexus.sklite.api.SkContext;
import io.github.libnexus.sklite.api.annotation.Pattern;
import io.github.libnexus.sklite.core.SkParser;
import io.github.libnexus.sklite.registry.SkriptLoader;

public class VisionTest {
    public static void main(String[] args) {
        SkParser parser = new SkParser();
        SkriptLoader loader = new SkriptLoader(parser);


        parser.expression("itemstack").includeChoice("diamond", "gold").setElementFactory(m -> new ScriptElement.Literal("ITEM_STUB(%s)".formatted(m.get("@0"))));
        parser.expression("player").includeChoice("Steve", "Alex", "Hippopotamus").setElementFactory(m -> new ScriptElement.Literal("PLAYER_STUB(%s)".formatted(m.get("@0"))));
        parser.expression("entity").includeChoice("villager", "trader").setElementFactory(m -> new ScriptElement.Literal("ENTITY_STUB(%s)".formatted(m.get("@0"))));

        loader.registerCommand("give %itemstack:item% [to %player:pl%]", GiveCommand.class);
        loader.registerCommand("give %itemstack:item% to %entity:et%", GiveEntityCommand.class);


        System.out.println("--- TEST 1: Full Command ---");
        ScriptElement script1 = parser.parse("give diamond to Steve");
        if (script1 != null) script1.run(new SkContext());

        System.out.println("\n--- TEST 2: Optional Missing ---");
        ScriptElement script2 = parser.parse("give gold");
        if (script2 != null) script2.run(new SkContext());

        System.out.println("\n--- TEST 3: To an entity ---");
        ScriptElement script3 = parser.parse("give emerald to villager");
        if (script3 != null) script3.run(new SkContext());
    }

    public static class GiveCommand implements ScriptElement {

        @Pattern("item")
        private ScriptElement itemStack;

        @Pattern("pl")
        private ScriptElement targetPlayer;

        @Override
        public Object run(SkContext ctx) {
            System.out.println("EXECUTING GIVE COMMAND:");
            System.out.println(" - Item: " + itemStack.run(ctx));


            if (targetPlayer != null) {
                System.out.println(" - Target: " + targetPlayer.run(ctx));
            } else {
                System.out.println(" - Target: Self (Default)");
            }
            return null;
        }

        @Override
        public Class<?> getType() {
            return null;
        }
    }

    public static class GiveEntityCommand implements ScriptElement {

        @Pattern("item")
        private ScriptElement itemStack;

        @Pattern("et")
        private ScriptElement targetEntity;

        @Override
        public Object run(SkContext ctx) {
            System.out.println("EXECUTING ENTITY GIVE COMMAND:");
            System.out.println(" - Item: " + itemStack.run(ctx));


            if (targetEntity != null) {
                System.out.println(" - Target: " + targetEntity.run(ctx));
            }
            return null;
        }

        @Override
        public Class<?> getType() {
            return null;
        }
    }

    public record SwordElement(ScriptElement level) implements ScriptElement {
        @Override
        public Object run(SkContext context) {

            int lvl = (int) level.run(context);
            return "DIAMOND_SWORD{Level:" + lvl + "}";
        }

        @Override
        public Class<?> getType() {
            return null;
        }
    }

    public record GiveElement(ScriptElement item) implements ScriptElement {
        @Override
        public Object run(SkContext context) {

            Object itemResult = item.run(context);
            System.out.println("GIVING PLAYER: " + itemResult);
            return null;
        }

        @Override
        public Class<?> getType() {
            return null;
        }
    }
}