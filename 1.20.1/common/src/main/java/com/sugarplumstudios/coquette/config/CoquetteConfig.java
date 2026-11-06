package com.sugarplumstudios.coquette.config;

import com.sugarplumstudios.coquette.platform.Services;

/**
 * The mod's two config files, {@code config/coquettefurniture-common.toml} (server rules) and
 * {@code config/coquettefurniture-client.toml}. Same layout and keys on every loader and version.
 */
public final class CoquetteConfig {
    private static final TomlConfig.Builder COMMON_BUILDER = new TomlConfig.Builder();
    public static final Common COMMON = new Common(COMMON_BUILDER);
    private static final TomlConfig COMMON_FILE = COMMON_BUILDER.build("coquettefurniture-common.toml");
    private static final TomlConfig.Builder CLIENT_BUILDER = new TomlConfig.Builder();
    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    private static final TomlConfig CLIENT_FILE = CLIENT_BUILDER.build("coquettefurniture-client.toml");

    private CoquetteConfig() {
    }

    /** Server rules; read at mod start. */
    public static void loadCommon() {
        COMMON_FILE.load(Services.PLATFORM.configDir());
    }

    /** Client options; read when the client starts. */
    public static void loadClient() {
        CLIENT_FILE.load(Services.PLATFORM.configDir());
    }

    public static final class Common {
        public final TomlConfig.BooleanValue seating;
        public final TomlConfig.BooleanValue dyeRecoloring;
        public final TomlConfig.BooleanValue sinksGiveWater;
        public final TomlConfig.BooleanValue vanityEffect;
        public final TomlConfig.IntValue vanityCooldown;
        public final TomlConfig.BooleanValue bathRegeneration;
        public final TomlConfig.BooleanValue teaSetPoursTea;
        public final TomlConfig.BooleanValue musicBoxMelodies;
        public final TomlConfig.BooleanValue bookshelfEnchanting;
        public final TomlConfig.BooleanValue catalogDeliveryInSurvival;
        public final TomlConfig.BooleanValue giveCatalogOnFirstJoin;
        public final TomlConfig.BooleanValue unlockAllRecipes;

        Common(TomlConfig.Builder builder) {
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
        public final TomlConfig.BooleanValue featureTooltips;
        public final TomlConfig.BooleanValue setTooltips;
        public final TomlConfig.BooleanValue birdhouseChirps;
        public final TomlConfig.BooleanValue displayCabinetSpin;

        Client(TomlConfig.Builder builder) {
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
