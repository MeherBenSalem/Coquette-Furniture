package com.sugarplumstudios.coquette.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class CoquetteConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();
        Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
    }

    private CoquetteConfig() {
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue seating;
        public final ForgeConfigSpec.BooleanValue dyeRecoloring;
        public final ForgeConfigSpec.BooleanValue sinksGiveWater;
        public final ForgeConfigSpec.BooleanValue vanityEffect;
        public final ForgeConfigSpec.IntValue vanityCooldown;
        public final ForgeConfigSpec.BooleanValue bathRegeneration;
        public final ForgeConfigSpec.BooleanValue teaSetPoursTea;
        public final ForgeConfigSpec.BooleanValue musicBoxMelodies;
        public final ForgeConfigSpec.BooleanValue bookshelfEnchanting;
        public final ForgeConfigSpec.BooleanValue catalogDeliveryInSurvival;
        public final ForgeConfigSpec.BooleanValue giveCatalogOnFirstJoin;
        public final ForgeConfigSpec.BooleanValue unlockAllRecipes;

        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("How furniture behaves").push("furniture");
            seating = builder
                    .comment("Let players sit on chairs, sofas, benches, pillows, swings and bathtubs.")
                    .define("seating", true);
            dyeRecoloring = builder
                    .comment("Right-clicking placed furniture with a dye repaints it in that colourway (keeping its contents).")
                    .define("dyeRecoloring", true);
            sinksGiveWater = builder
                    .comment("Kitchen and bathroom sinks fill buckets and bottles with endless water.")
                    .define("sinksGiveWater", true);
            vanityEffect = builder
                    .comment("Pampering yourself at a vanity table gives 10 seconds of Regeneration.")
                    .define("vanityEffect", true);
            vanityCooldown = builder
                    .comment("Seconds before the vanity can pamper the same player again.")
                    .defineInRange("vanityCooldownSeconds", 120, 0, 3600);
            bathRegeneration = builder
                    .comment("Relaxing in a filled bathtub slowly regenerates health.")
                    .define("bathRegeneration", true);
            teaSetPoursTea = builder
                    .comment("Tea sets fill glass bottles with cups of strawberry tea.")
                    .define("teaSetPoursTea", true);
            musicBoxMelodies = builder
                    .comment("Jewelry boxes play their melody when opened.")
                    .define("jewelryBoxMelodies", true);
            bookshelfEnchanting = builder
                    .comment("Coquette bookshelves power enchanting tables like vanilla bookshelves.")
                    .define("bookshelfEnchanting", true);
            builder.pop();

            builder.comment("The Interior Design Catalog").push("catalog");
            catalogDeliveryInSurvival = builder
                    .comment("Allow the catalog to deliver furniture outside creative mode (a cheat; off by default).")
                    .define("deliveryInSurvival", false);
            giveCatalogOnFirstJoin = builder
                    .comment("Give each player an Interior Design Catalog the first time they join.")
                    .define("giveCatalogOnFirstJoin", true);
            unlockAllRecipes = builder
                    .comment("Unlock every furniture recipe in the recipe book when a player joins.")
                    .define("unlockAllRecipes", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue featureTooltips;
        public final ForgeConfigSpec.BooleanValue setTooltips;
        public final ForgeConfigSpec.BooleanValue birdhouseChirps;
        public final ForgeConfigSpec.BooleanValue displayCabinetSpin;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side presentation").push("display");
            featureTooltips = builder
                    .comment("List what each piece of furniture can do in its tooltip.")
                    .define("featureTooltips", true);
            setTooltips = builder
                    .comment("Show which furniture sets an item belongs to in its tooltip.")
                    .define("setTooltips", true);
            birdhouseChirps = builder
                    .comment("Birdhouses chirp now and then during the day.")
                    .define("birdhouseChirps", true);
            displayCabinetSpin = builder
                    .comment("Items in display cabinets slowly turn.")
                    .define("displayCabinetSpin", true);
            builder.pop();
        }
    }
}
