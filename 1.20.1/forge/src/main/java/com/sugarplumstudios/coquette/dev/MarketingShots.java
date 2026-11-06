package com.sugarplumstudios.coquette.dev;

import com.sugarplumstudios.coquette.block.BathtubBlock;
import com.sugarplumstudios.coquette.block.ConnectedSeatBlock;
import com.sugarplumstudios.coquette.block.ConnectingBlock;
import com.sugarplumstudios.coquette.block.DecorBlock;
import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.LampBlock;
import com.sugarplumstudios.coquette.block.PillowBlock;
import com.sugarplumstudios.coquette.block.SinkBlock;
import com.sugarplumstudios.coquette.block.StorageBlock;
import com.sugarplumstudios.coquette.block.StoveBlock;
import com.sugarplumstudios.coquette.block.TallFurnitureBlock;
import com.sugarplumstudios.coquette.block.BookStackBlock;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.client.screen.CatalogScreen;
import com.sugarplumstudios.coquette.registry.ModBlocks;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Development only (stripped from the release jar). {@code ./gradlew :forge:runClient -Pmarketing} builds a pastel
 * dollhouse street, one room per furniture set, and saves the 1920x1080 listing captures to
 * {@code run/screenshots/marketing_*.png}, then quits. Run it under a 1920x1080 display.
 */
public final class MarketingShots {
    public static final boolean ENABLED = Boolean.getBoolean("coquettefurniture.marketing");
    private static final int Y = -60;
    private static final Direction S = Direction.SOUTH;

    /** A capture: camera position and angles, the time of day, and what to do on the client first. */
    private record Shot(String name, double x, double y, double z, float yaw, float pitch, long time, Consumer<Minecraft> setup) {
    }

    private static final List<Shot> SHOTS = new ArrayList<>();
    private static final List<BlockPos> PLACED = new ArrayList<>();
    private static boolean worldRequested;
    private static boolean built;
    private static int tick;
    private static int shotIndex;
    private static int phase = -1;

    private MarketingShots() {
    }

    public static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!worldRequested && mc.level == null && mc.screen != null && mc.getOverlay() == null) {
            worldRequested = true;
            mc.options.onboardAccessibility = false;
            mc.options.guiScale().set(3);
            mc.options.save();
            mc.getWindow().setWindowed(1920, 1080);
            LevelSettings settings = new LevelSettings("Coquette Marketing", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel("coquette_marketing_" + System.currentTimeMillis(), settings,
                    new WorldOptions(7L, false, false),
                    access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
            return;
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
            return;
        }
        tick++;
        if (tick == 40 && !built) {
            built = true;
            mc.options.hideGui = true;
            mc.options.renderDistance().set(12);
            mc.getSingleplayerServer().execute(() -> build(mc.getSingleplayerServer().overworld()));
            return;
        }
        if (tick < 140) {
            return;
        }
        if (shotIndex >= SHOTS.size()) {
            mc.stop();
            return;
        }
        phase++;
        Shot shot = SHOTS.get(shotIndex);
        if (phase == 0) {
            mc.setScreen(null);
            if (mc.options.guiScale().get() != 3) {
                mc.options.guiScale().set(3);
                mc.resizeDisplay();
            }
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            ServerLevel level = mc.getSingleplayerServer().overworld();
            mc.getSingleplayerServer().execute(() -> {
                level.setDayTime(shot.time);
                ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
                player.teleportTo(level, shot.x, shot.y, shot.z, shot.yaw, shot.pitch);
            });
        } else if (phase == 60) {
            shot.setup.accept(mc);
        } else if (phase == 100) {
            parkMouse(mc);
        } else if (phase == 110) {
            Screenshot.grab(mc.gameDirectory, "marketing_" + shot.name + ".png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (phase > 112) {
            phase = -1;
            shotIndex++;
        }
    }

    /** Puts the (invisible) cursor in the far corner so no tooltip shows on screen captures. */
    private static void parkMouse(Minecraft mc) {
        try {
            for (String name : new String[]{"xpos", "ypos"}) {
                Field field = MouseHandler.class.getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(mc.mouseHandler, name.equals("xpos") ? 1919 : 1079);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    // ------------------------------------------------------------------ helpers

    private static BlockState f(FurnitureType type, FurnitureColor color, Direction facing) {
        BlockState state = ModBlocks.furniture(type, color).defaultBlockState();
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.setValue(HorizontalDirectionalBlock.FACING, facing) : state;
    }

    private static BlockState d(DecorType decor, Direction facing) {
        BlockState state = ModBlocks.decor(decor).defaultBlockState();
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.setValue(HorizontalDirectionalBlock.FACING, facing) : state;
    }

    private static void put(ServerLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        PLACED.add(pos);
    }

    private static void tall(ServerLevel level, int x, int y, int z, BlockState lower) {
        put(level, x, y, z, lower.setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.LOWER));
        put(level, x, y + 1, z, lower.setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void bed(ServerLevel level, int x, int z, BlockState bed) {
        put(level, x, Y, z, bed.setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        put(level, x, Y, z + 1, bed.setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT));
    }

    private static void fill(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    /** Lets joining pieces and decorations look at their finished neighbours, like real placement does. */
    private static void settle(ServerLevel level) {
        for (BlockPos pos : PLACED) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof ConnectingBlock || block instanceof ConnectedSeatBlock || block instanceof DecorBlock
                    || block instanceof net.minecraft.world.level.block.IronBarsBlock) {
                level.setBlock(pos, Block.updateFromNeighbourShapes(state, level, pos), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * A dollhouse room 11 wide and 8 deep with its back wall on the north and a side wall on the west, both 5 high,
     * a skirting board, a window in the back wall and a garden around it. Returns nothing; furniture goes in after.
     */
    private static void room(ServerLevel level, int x0, int z0, BlockState floor, BlockState wall, BlockState trim, int windowX) {
        fill(level, x0 - 4, Y - 1, z0 - 5, x0 + 14, Y - 1, z0 + 12, Blocks.GRASS_BLOCK.defaultBlockState());
        fill(level, x0, Y - 1, z0, x0 + 10, Y - 1, z0 + 7, floor);
        fill(level, x0, Y, z0 - 1, x0 + 10, Y + 4, z0 - 1, wall);
        fill(level, x0 - 1, Y, z0 - 1, x0 - 1, Y + 4, z0 + 7, wall);
        fill(level, x0 - 1, Y + 5, z0 - 1, x0 + 10, Y + 5, z0 - 1, trim);
        fill(level, x0 - 1, Y + 5, z0 - 1, x0 - 1, Y + 5, z0 + 7, trim);
        fill(level, x0 - 1, Y - 1, z0 - 1, x0 + 10, Y - 1, z0 - 1, trim);
        fill(level, x0 - 1, Y - 1, z0 - 1, x0 - 1, Y - 1, z0 + 7, trim);
        if (windowX >= 0) {
            for (int x = x0 + windowX; x <= x0 + windowX + 1; x++) {
                for (int y = Y + 2; y <= Y + 3; y++) {
                    put(level, x, y, z0 - 1, Blocks.GLASS_PANE.defaultBlockState());
                }
            }
        }
        // a few flowers around the house
        BlockState[] flowers = {Blocks.PINK_TULIP.defaultBlockState(), Blocks.ALLIUM.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(),
                Blocks.LILY_OF_THE_VALLEY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState()};
        int i = 0;
        for (int x = x0 - 4; x <= x0 + 14; x++) {
            for (int z = z0 - 5; z <= z0 + 12; z++) {
                boolean inside = x >= x0 - 1 && x <= x0 + 10 && z >= z0 - 1 && z <= z0 + 7;
                if (!inside && Math.floorMod(x * 7 + z * 13, 5) == 0) {
                    BlockState flower = flowers[i++ % flowers.length];
                    level.setBlock(new BlockPos(x, Y, z), flower, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    /** The default room camera: south-east of the room, looking north-west into it. */
    private static void roomShot(String name, int x0, int z0, long time) {
        SHOTS.add(new Shot(name, x0 + 8.1, Y + 3.3, z0 + 7.0, 142, 24, time, mc -> {
        }));
    }

    // ------------------------------------------------------------------ scenes

    private static void build(ServerLevel level) {
        level.setWeatherParameters(6000, 0, false, false);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, level.getServer());

        princessBedroom(level, 0, 0);
        angelcoreLivingRoom(level, 40, 0);
        pastelKitchen(level, 80, 0);
        lavenderDreams(level, 120, 0);
        strawberryCottage(level, 160, 0);
        colours(level, 200, 0);
        joining(level, 240, 0);
        settle(level);

        // close-ups and catalog views
        SHOTS.add(new Shot("open-storage", 4.4, Y + 1.9, 5.0, 160, 14, 6000, mc -> {
        }));
        SHOTS.add(new Shot("decor", 40 + 5.5, Y + 2.0, 5.4, 180, 30, 6000, mc -> {
        }));
        SHOTS.add(new Shot("night", 7.6, Y + 2.6, 5.6, 150, 20, 18000, mc -> {
        }));
        SHOTS.add(new Shot("catalog", 7.5, Y + 2.2, 5.5, 150, 15, 6000, mc -> {
            mc.options.guiScale().set(4);
            mc.resizeDisplay();
            CatalogScreen screen = new CatalogScreen();
            mc.setScreen(screen);
            screen.previewSelect(1, ModItems.furniture(FurnitureType.CANOPY_BED, FurnitureColor.BABY_PINK));
        }));
        SHOTS.add(new Shot("catalog-kitchen", 80 + 7.5, Y + 2.2, 5.5, 150, 15, 6000, mc -> {
            mc.options.guiScale().set(4);
            mc.resizeDisplay();
            CatalogScreen screen = new CatalogScreen();
            mc.setScreen(screen);
            screen.previewSelect(3, ModItems.furniture(FurnitureType.STOVE, FurnitureColor.MINT));
        }));
        SHOTS.add(new Shot("catalog-sets", 40 + 7.5, Y + 2.2, 5.5, 150, 15, 6000, mc -> {
            mc.options.guiScale().set(4);
            mc.resizeDisplay();
            CatalogScreen screen = new CatalogScreen();
            mc.setScreen(screen);
            screen.previewSet(0);
        }));
    }

    /** Princess Bedroom: the hero shot. */
    private static void princessBedroom(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.CHERRY_PLANKS.defaultBlockState(), Blocks.PINK_WOOL.defaultBlockState(),
                Blocks.WHITE_WOOL.defaultBlockState(), 4);
        int y = Y;
        bed(level, x0 + 4, z0, f(FurnitureType.CANOPY_BED, FurnitureColor.BABY_PINK, Direction.NORTH));
        put(level, x0 + 4, y + 1, z0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.WHITE, S).setValue(PillowBlock.PILLOWS, 3));
        put(level, x0 + 3, y, z0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE, S));
        put(level, x0 + 3, y + 1, z0, f(FurnitureType.NIGHT_LAMP, FurnitureColor.BABY_PINK, S).setValue(LampBlock.LIT, true));
        put(level, x0 + 5, y, z0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE, S));
        put(level, x0 + 5, y + 1, z0, d(DecorType.PASTEL_CANDLES, S).setValue(LampBlock.LIT, true));
        tall(level, x0 + 4, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.BABY_PINK, S).setValue(StorageBlock.OPEN, true));
        tall(level, x0 + 5, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.BABY_PINK, S).setValue(StorageBlock.OPEN, true));
        tall(level, x0, y, z0, f(FurnitureType.WARDROBE, FurnitureColor.BABY_PINK, S));
        tall(level, x0 + 1, y, z0, f(FurnitureType.WARDROBE, FurnitureColor.BABY_PINK, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 7, y, z0, f(FurnitureType.VANITY_TABLE, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 7, y, z0 + 1, f(FurnitureType.DINING_CHAIR, FurnitureColor.WHITE, Direction.NORTH));
        put(level, x0 + 8, y, z0, f(FurnitureType.DRESSER, FurnitureColor.WHITE, S));
        put(level, x0 + 8, y + 1, z0, f(FurnitureType.JEWELRY_BOX, FurnitureColor.BABY_PINK, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 9, y, z0, f(FurnitureType.DRESSER, FurnitureColor.WHITE, S));
        put(level, x0 + 9, y + 1, z0, d(DecorType.PERFUME_BOTTLES, S));
        tall(level, x0 + 10, y, z0, f(FurnitureType.STANDING_MIRROR, FurnitureColor.WHITE, Direction.WEST));
        put(level, x0, y, z0 + 3, f(FurnitureType.ARMCHAIR, FurnitureColor.WHITE, Direction.EAST));
        put(level, x0, y, z0 + 4, d(DecorType.POTTED_ROSE_BUSH, Direction.EAST));
        put(level, x0, y, z0 + 2, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE, Direction.EAST));
        put(level, x0, y + 1, z0 + 2, d(DecorType.HEART_PHOTO_FRAME, Direction.EAST));
        for (int x = x0 + 3; x <= x0 + 6; x++) {
            for (int z = z0 + 3; z <= z0 + 5; z++) {
                put(level, x, y, z, f(FurnitureType.RUG, FurnitureColor.BABY_PINK, S));
            }
        }
        put(level, x0 + 8, y, z0 + 3, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.BABY_PINK, S).setValue(PillowBlock.PILLOWS, 2));
        put(level, x0 + 9, y, z0 + 3, d(DecorType.TEDDY_BEAR, Direction.WEST));
        put(level, x0 + 8, y, z0 + 5, d(DecorType.RIBBON_GIFT_BOX, S));
        roomShot("princess-bedroom", x0, z0, 6000);
    }

    /** Angelcore Room: a white and baby blue living room. */
    private static void angelcoreLivingRoom(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
                Blocks.WHITE_CONCRETE.defaultBlockState(), 4);
        int y = Y;
        for (int x = x0 + 3; x <= x0 + 6; x++) {
            put(level, x, y, z0, f(FurnitureType.SOFA, FurnitureColor.BABY_BLUE, S));
        }
        put(level, x0 + 4, y + 1, z0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.WHITE, S));
        put(level, x0 + 6, y + 1, z0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.BABY_PINK, S));
        tall(level, x0 + 4, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.WHITE, S).setValue(StorageBlock.OPEN, true));
        tall(level, x0 + 5, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.WHITE, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 4, y, z0 + 2, f(FurnitureType.COFFEE_TABLE, FurnitureColor.WHITE, S));
        put(level, x0 + 5, y, z0 + 2, f(FurnitureType.COFFEE_TABLE, FurnitureColor.WHITE, S));
        put(level, x0 + 4, y + 1, z0 + 2, f(FurnitureType.TEA_SET, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 5, y + 1, z0 + 2, d(DecorType.ROSE_VASE, S));
        put(level, x0 + 2, y, z0 + 2, f(FurnitureType.ARMCHAIR, FurnitureColor.BABY_BLUE, Direction.EAST));
        put(level, x0 + 7, y, z0 + 2, f(FurnitureType.ARMCHAIR, FurnitureColor.LAVENDER, Direction.WEST));
        for (int x = x0 + 3; x <= x0 + 6; x++) {
            for (int z = z0 + 1; z <= z0 + 3; z++) {
                if (z != z0 + 2 || x == x0 + 3 || x == x0 + 6) {
                    put(level, x, y, z, f(FurnitureType.RUG, FurnitureColor.BABY_BLUE, S));
                }
            }
        }
        put(level, x0, y, z0, f(FurnitureType.BOOKSHELF, FurnitureColor.WHITE, S));
        put(level, x0 + 1, y, z0, f(FurnitureType.BOOKSHELF, FurnitureColor.WHITE, S));
        put(level, x0, y + 1, z0, d(DecorType.BOOK_STACK, S).setValue(BookStackBlock.BOOKS, 3));
        put(level, x0 + 1, y + 1, z0, d(DecorType.ANGEL_FIGURINE, S));
        put(level, x0 + 8, y, z0, f(FurnitureType.DISPLAY_CABINET, FurnitureColor.WHITE, S));
        put(level, x0 + 9, y, z0, d(DecorType.POTTED_MONSTERA, S));
        put(level, x0 + 10, y, z0, f(FurnitureType.NIGHT_LAMP, FurnitureColor.BABY_BLUE, S).setValue(LampBlock.LIT, true));
        put(level, x0, y, z0 + 3, d(DecorType.POTTED_CHERRY_BLOSSOM, Direction.EAST));
        put(level, x0, y, z0 + 5, d(DecorType.BUNNY_PLUSH, Direction.EAST));
        put(level, x0 + 8, y, z0 + 4, d(DecorType.PASTEL_CANDLES, S).setValue(LampBlock.LIT, true));
        if (level.getBlockEntity(new BlockPos(x0 + 8, y, z0)) instanceof DisplayCabinetBlockEntity cabinet) {
            cabinet.setItem(0, new ItemStack(ModItems.decor(DecorType.TEDDY_BEAR)));
            cabinet.setItem(1, new ItemStack(Items.DIAMOND));
            cabinet.setItem(2, new ItemStack(ModItems.decor(DecorType.PERFUME_BOTTLES)));
            cabinet.setItem(3, new ItemStack(Items.CAKE));
        }
        roomShot("angelcore-living-room", x0, z0, 6000);
    }

    /** Pastel Kitchen. */
    private static void pastelKitchen(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
                Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), 4);
        for (int x = x0; x <= x0 + 10; x++) {
            for (int z = z0; z <= z0 + 7; z++) {
                level.setBlock(new BlockPos(x, Y - 1, z), ((x + z) & 1) == 0 ? Blocks.WHITE_CONCRETE.defaultBlockState()
                        : Blocks.PINK_WOOL.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        int y = Y;
        tall(level, x0, y, z0, f(FurnitureType.REFRIGERATOR, FurnitureColor.MINT, S));
        tall(level, x0 + 1, y, z0, f(FurnitureType.REFRIGERATOR, FurnitureColor.MINT, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 2, y, z0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.CREAM, S));
        put(level, x0 + 2, y + 1, z0, d(DecorType.CUPCAKE_STAND, S));
        put(level, x0 + 3, y, z0, f(FurnitureType.KITCHEN_CABINET, FurnitureColor.MINT, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 4, y, z0, f(FurnitureType.KITCHEN_SINK, FurnitureColor.WHITE, S).setValue(SinkBlock.RUNNING, true));
        put(level, x0 + 5, y, z0, f(FurnitureType.KITCHEN_SINK, FurnitureColor.WHITE, S));
        tall(level, x0 + 4, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.MINT, S).setValue(StorageBlock.OPEN, true));
        tall(level, x0 + 5, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.MINT, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 6, y, z0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.CREAM, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 6, y + 1, z0, d(DecorType.STRAWBERRY_BASKET, S));
        put(level, x0 + 7, y, z0, f(FurnitureType.STOVE, FurnitureColor.BABY_BLUE, S).setValue(StoveBlock.LIT, true));
        put(level, x0 + 8, y, z0, f(FurnitureType.KITCHEN_CABINET, FurnitureColor.MINT, S));
        put(level, x0 + 8, y + 1, z0, f(FurnitureType.TEA_SET, FurnitureColor.MINT, S));
        put(level, x0 + 9, y, z0, f(FurnitureType.DISPLAY_CABINET, FurnitureColor.BABY_BLUE, S));
        put(level, x0, y, z0 + 4, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.CREAM, Direction.EAST));
        put(level, x0, y + 1, z0 + 4, d(DecorType.TULIP_VASE, Direction.EAST));
        put(level, x0, y, z0 + 5, f(FurnitureType.KITCHEN_CABINET, FurnitureColor.MINT, Direction.EAST));
        put(level, x0, y + 1, z0 + 5, d(DecorType.RIBBON_GIFT_BOX, Direction.EAST));
        for (int x = x0 + 4; x <= x0 + 6; x++) {
            for (int z = z0 + 3; z <= z0 + 4; z++) {
                put(level, x, y, z, f(FurnitureType.DINING_TABLE, FurnitureColor.LAVENDER, S));
            }
        }
        put(level, x0 + 4, y + 1, z0 + 3, f(FurnitureType.TEA_SET, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 5, y + 1, z0 + 4, d(DecorType.CUPCAKE_STAND, S));
        put(level, x0 + 6, y + 1, z0 + 3, d(DecorType.TULIP_VASE, S));
        for (int x = x0 + 4; x <= x0 + 6; x++) {
            put(level, x, y, z0 + 2, f(FurnitureType.DINING_CHAIR, x % 2 == 0 ? FurnitureColor.BABY_PINK : FurnitureColor.MINT, S));
            put(level, x, y, z0 + 5, f(FurnitureType.DINING_CHAIR, x % 2 == 0 ? FurnitureColor.MINT : FurnitureColor.BABY_PINK, Direction.NORTH));
        }
        put(level, x0 + 3, y, z0 + 3, f(FurnitureType.DINING_CHAIR, FurnitureColor.BABY_PINK, Direction.EAST));
        put(level, x0 + 7, y, z0 + 4, f(FurnitureType.DINING_CHAIR, FurnitureColor.MINT, Direction.WEST));
        roomShot("pastel-kitchen", x0, z0, 6000);
    }

    /** Lavender Dreams: a lavender bedroom with a bathroom corner. */
    private static void lavenderDreams(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.PURPUR_BLOCK.defaultBlockState(),
                Blocks.WHITE_WOOL.defaultBlockState(), 2);
        fill(level, x0 + 6, Y - 1, z0, x0 + 10, Y - 1, z0 + 7, Blocks.CALCITE.defaultBlockState());
        fill(level, x0 + 6, Y, z0 - 1, x0 + 10, Y + 4, z0 - 1, Blocks.WHITE_WOOL.defaultBlockState());
        int y = Y;
        bed(level, x0 + 2, z0, f(FurnitureType.CANOPY_BED, FurnitureColor.LAVENDER, Direction.NORTH));
        put(level, x0 + 2, y + 1, z0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.LAVENDER, S).setValue(PillowBlock.PILLOWS, 2));
        put(level, x0 + 1, y, z0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.LAVENDER, S));
        put(level, x0 + 1, y + 1, z0, f(FurnitureType.NIGHT_LAMP, FurnitureColor.LAVENDER, S).setValue(LampBlock.LIT, true));
        put(level, x0 + 3, y, z0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.LAVENDER, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 3, y + 1, z0, d(DecorType.HEART_MUSIC_BOX, S));
        tall(level, x0 + 2, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.LAVENDER, S));
        tall(level, x0 + 3, y + 2, z0, f(FurnitureType.CURTAINS, FurnitureColor.LAVENDER, S));
        tall(level, x0 + 4, y, z0, f(FurnitureType.WARDROBE, FurnitureColor.LAVENDER, S).setValue(StorageBlock.OPEN, true));
        put(level, x0, y, z0 + 3, f(FurnitureType.ARMCHAIR, FurnitureColor.LAVENDER, Direction.EAST));
        put(level, x0, y, z0 + 4, d(DecorType.POTTED_LAVENDER, Direction.EAST));
        put(level, x0, y, z0 + 2, d(DecorType.BOOK_STACK, Direction.EAST).setValue(BookStackBlock.BOOKS, 2));
        for (int x = x0 + 1; x <= x0 + 3; x++) {
            for (int z = z0 + 3; z <= z0 + 4; z++) {
                put(level, x, y, z, f(FurnitureType.RUG, FurnitureColor.LAVENDER, S));
            }
        }
        // bathroom corner
        BlockState tub = f(FurnitureType.BATHTUB, FurnitureColor.LAVENDER, Direction.WEST).setValue(BathtubBlock.FILLED, true);
        put(level, x0 + 9, y, z0 + 1, tub.setValue(BathtubBlock.PART, BedPart.HEAD));
        put(level, x0 + 10, y, z0 + 1, tub.setValue(BathtubBlock.PART, BedPart.FOOT));
        put(level, x0 + 7, y, z0, f(FurnitureType.BATHROOM_SINK, FurnitureColor.LAVENDER, S).setValue(SinkBlock.RUNNING, true));
        put(level, x0 + 7, y + 1, z0, f(FurnitureType.WALL_MIRROR, FurnitureColor.LAVENDER, S));
        put(level, x0 + 8, y + 1, z0, f(FurnitureType.TOWEL_RACK, FurnitureColor.LAVENDER, S));
        put(level, x0 + 9, y + 2, z0, f(FurnitureType.BATHROOM_CABINET, FurnitureColor.LAVENDER, S).setValue(StorageBlock.OPEN, true));
        put(level, x0 + 10, y, z0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.WHITE, S));
        put(level, x0 + 10, y + 1, z0, f(FurnitureType.SOAP_DISPENSER, FurnitureColor.LAVENDER, S));
        put(level, x0 + 6, y, z0, d(DecorType.POTTED_LAVENDER, S));
        roomShot("lavender-dreams", x0, z0, 6000);
    }

    /** Strawberry Cottage: the garden. */
    private static void strawberryCottage(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.AIR.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), -1);
        fill(level, x0 - 1, Y, z0 - 1, x0 + 10, Y + 5, z0 + 7, Blocks.AIR.defaultBlockState());
        // a cherry tree behind the fence
        fill(level, x0 + 8, Y, z0 - 3, x0 + 8, Y + 4, z0 - 3, Blocks.CHERRY_LOG.defaultBlockState());
        fill(level, x0 + 6, Y + 4, z0 - 5, x0 + 10, Y + 6, z0 - 1, Blocks.CHERRY_LEAVES.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        fill(level, x0 + 7, Y + 7, z0 - 4, x0 + 9, Y + 7, z0 - 2, Blocks.CHERRY_LEAVES.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        int y = Y;
        for (int x = x0 - 1; x <= x0 + 10; x++) {
            put(level, x, y, z0 - 1, f(FurnitureType.PICKET_FENCE, FurnitureColor.WHITE, S));
        }
        for (int z = z0; z <= z0 + 7; z++) {
            put(level, x0 - 1, y, z, f(FurnitureType.PICKET_FENCE, FurnitureColor.WHITE, S));
        }
        for (int x = x0 + 1; x <= x0 + 3; x++) {
            put(level, x, y, z0, f(FurnitureType.GARDEN_BENCH, FurnitureColor.CREAM, S));
        }
        tall(level, x0 + 5, y, z0, f(FurnitureType.SWING_SEAT, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 7, y, z0, f(FurnitureType.BIRDHOUSE, FurnitureColor.CREAM, S));
        put(level, x0 + 8, y, z0 + 2, f(FurnitureType.OUTDOOR_TABLE, FurnitureColor.CREAM, S));
        put(level, x0 + 8, y + 1, z0 + 2, f(FurnitureType.FLOWER_POT, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 7, y, z0 + 2, f(FurnitureType.DINING_CHAIR, FurnitureColor.BABY_PINK, Direction.EAST));
        put(level, x0 + 9, y, z0 + 2, f(FurnitureType.DINING_CHAIR, FurnitureColor.BABY_PINK, Direction.WEST));
        for (int x = x0 + 2; x <= x0 + 4; x++) {
            for (int z = z0 + 3; z <= z0 + 5; z++) {
                put(level, x, y, z, f(FurnitureType.PICNIC_BLANKET, FurnitureColor.BABY_PINK, S));
            }
        }
        put(level, x0 + 5, y, z0 + 4, d(DecorType.STRAWBERRY_BASKET, S));
        put(level, x0 + 1, y, z0 + 4, f(FurnitureType.TEA_SET, FurnitureColor.BABY_PINK, Direction.EAST));
        put(level, x0, y, z0 + 2, f(FurnitureType.FLOWER_POT, FurnitureColor.MINT, Direction.EAST));
        put(level, x0, y, z0 + 6, f(FurnitureType.FLOWER_POT, FurnitureColor.LAVENDER, Direction.EAST));
        put(level, x0 + 9, y, z0, f(FurnitureType.FLOWER_POT, FurnitureColor.BABY_BLUE, S));
        roomShot("strawberry-cottage", x0, z0, 6000);
    }

    /** Every colourway of the signature pieces, on a white stage. */
    private static void colours(ServerLevel level, int x0, int z0) {
        fill(level, x0 - 3, Y - 1, z0 - 3, x0 + 16, Y - 1, z0 + 16, Blocks.WHITE_CONCRETE.defaultBlockState());
        FurnitureColor[] colors = FurnitureColor.values();
        FurnitureType[] row = {FurnitureType.SOFA, FurnitureType.ARMCHAIR, FurnitureType.DRESSER, FurnitureType.STOVE, FurnitureType.VANITY_TABLE};
        for (int i = 0; i < colors.length; i++) {
            int x = x0 + i * 2;
            tall(level, x, Y, z0, f(FurnitureType.WARDROBE, colors[i], S));
            for (int j = 0; j < row.length; j++) {
                put(level, x, Y, z0 + 2 + j * 2, f(row[j], colors[i], S));
            }
        }
        SHOTS.add(new Shot("colours", x0 + 5, Y + 4.4, z0 + 12.2, 180, 29, 6000, mc -> {
        }));
    }

    /** Sofas, tables, rugs and blankets joining up. */
    private static void joining(ServerLevel level, int x0, int z0) {
        room(level, x0, z0, Blocks.CHERRY_PLANKS.defaultBlockState(), Blocks.PINK_WOOL.defaultBlockState(),
                Blocks.WHITE_WOOL.defaultBlockState(), -1);
        int y = Y;
        for (int x = x0; x <= x0 + 4; x++) {
            put(level, x, y, z0, f(FurnitureType.SOFA, FurnitureColor.BABY_PINK, S));
        }
        for (int x = x0 + 6; x <= x0 + 8; x++) {
            put(level, x, y, z0, f(FurnitureType.GARDEN_BENCH, FurnitureColor.WHITE, S));
        }
        for (int x = x0; x <= x0 + 4; x++) {
            for (int z = z0 + 2; z <= z0 + 5; z++) {
                put(level, x, y, z, f(FurnitureType.RUG, FurnitureColor.MINT, S));
            }
        }
        for (int x = x0 + 6; x <= x0 + 9; x++) {
            for (int z = z0 + 2; z <= z0 + 4; z++) {
                put(level, x, y, z, f(FurnitureType.DINING_TABLE, FurnitureColor.WHITE, S));
            }
        }
        put(level, x0 + 7, y + 1, z0 + 3, f(FurnitureType.TEA_SET, FurnitureColor.BABY_PINK, S));
        put(level, x0 + 8, y + 1, z0 + 3, d(DecorType.CUPCAKE_STAND, S));
        roomShot("joining", x0, z0, 6000);
    }
}
