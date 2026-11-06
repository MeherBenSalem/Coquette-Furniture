package com.sugarplumstudios.coquette.dev;

import com.sugarplumstudios.coquette.CoquetteFurniture;
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
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.client.screen.CatalogScreen;
import com.sugarplumstudios.coquette.registry.ModBlocks;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Development only (stripped from the release jar). Launch the client with
 * {@code ./gradlew :forge:runClient -Pshowcase} (or {@code :fabric:runClient}) and it builds a flat creative world,
 * furnishes a few rooms, screenshots each one plus the catalog, then quits.
 */
public final class ShowcaseHarness {
    public static final boolean ENABLED = Boolean.getBoolean("coquettefurniture.showcase");
    private static final int Y = -60;

    private record Shot(String name, double x, double y, double z, float yaw, float pitch) {
    }

    private static boolean worldRequested;
    private static boolean built;
    private static int tick;
    private static final List<Shot> SHOTS = new ArrayList<>();
    private static int shotIndex;
    private static int catalogStage;

    private ShowcaseHarness() {
    }

    private static void createWorld(Minecraft mc) {
        worldRequested = true;
        mc.getWindow().setWindowed(1280, 720);
        mc.options.hideGui = true;
        LevelSettings settings = new LevelSettings("Coquette Showcase", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("coquette_showcase_" + System.currentTimeMillis(), settings,
                new WorldOptions(1L, false, false),
                access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                mc.screen);
    }

    /** Called by every loader at the end of each client tick. */
    public static void onTick() {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // on a fresh run folder the first menu is the accessibility onboarding screen, not the title screen
        if (!worldRequested && mc.level == null && mc.screen != null && mc.getOverlay() == null) {
            mc.options.onboardAccessibility = false;
            mc.options.save();
            createWorld(mc);
            return;
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
            return;
        }
        tick++;
        if (tick == 40 && !built) {
            built = true;
            mc.getSingleplayerServer().execute(() -> build(mc.getSingleplayerServer().overworld()));
            mc.player.getAbilities().flying = true;
            return;
        }
        if (tick < 120) {
            return;
        }
        int phase = tick - 120;
        if (shotIndex < SHOTS.size()) {
            Shot shot = SHOTS.get(shotIndex);
            if (phase % 70 == 0) {
                mc.player.getAbilities().flying = true;
                mc.player.moveTo(shot.x, shot.y, shot.z, shot.yaw, shot.pitch);
                mc.player.setDeltaMovement(0, 0, 0);
            } else if (phase % 70 == 69) {
                Screenshot.grab(mc.gameDirectory, "coquette_" + shot.name + ".png", mc.getMainRenderTarget(), msg -> {
                });
                shotIndex++;
            }
            return;
        }
        catalogStage++;
        if (catalogStage == 1) {
            GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 2, 2);
            mc.setScreen(new CatalogScreen());
        } else if (catalogStage == 10 && mc.screen instanceof CatalogScreen catalog) {
            catalog.previewSelect(1, ModItems.furniture(FurnitureType.VANITY_TABLE, FurnitureColor.BABY_PINK));
        } else if (catalogStage == 30) {
            Screenshot.grab(mc.gameDirectory, "coquette_catalog.png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (catalogStage == 35 && mc.screen instanceof CatalogScreen catalog) {
            catalog.previewSet(0);
        } else if (catalogStage == 55) {
            Screenshot.grab(mc.gameDirectory, "coquette_catalog_sets.png", mc.getMainRenderTarget(), msg -> {
            });
        } else if (catalogStage == 70) {
            mc.stop();
        }
    }

    // ------------------------------------------------------------------ scenes

    private static BlockState f(FurnitureType type, FurnitureColor color, Direction facing) {
        BlockState state = ModBlocks.furniture(type, color).defaultBlockState();
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.setValue(HorizontalDirectionalBlock.FACING, facing) : state;
    }

    private static BlockState d(DecorType decor, Direction facing) {
        BlockState state = ModBlocks.decor(decor).defaultBlockState();
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.setValue(HorizontalDirectionalBlock.FACING, facing) : state;
    }

    private static final List<BlockPos> PLACED = new ArrayList<>();

    private static void put(ServerLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        PLACED.add(pos);
    }

    /** Lets joining pieces and decorations look at their finished neighbours, like real placement does. */
    private static void settle(ServerLevel level) {
        for (BlockPos pos : PLACED) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof ConnectingBlock || block instanceof ConnectedSeatBlock || block instanceof DecorBlock) {
                level.setBlock(pos, Block.updateFromNeighbourShapes(state, level, pos), Block.UPDATE_ALL);
            }
        }
    }

    private static void tall(ServerLevel level, int x, int y, int z, BlockState lower) {
        put(level, x, y, z, lower.setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.LOWER));
        put(level, x, y + 1, z, lower.setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void room(ServerLevel level, int x0, int z0, BlockState floor, BlockState wall) {
        for (int x = x0; x < x0 + 9; x++) {
            for (int z = z0; z < z0 + 7; z++) {
                level.setBlock(new BlockPos(x, Y - 1, z), floor, Block.UPDATE_ALL);
            }
            for (int y = Y; y < Y + 4; y++) {
                level.setBlock(new BlockPos(x, y, z0 - 1), wall, Block.UPDATE_ALL);
            }
        }
        for (int z = z0 - 1; z < z0 + 7; z++) {
            for (int y = Y; y < Y + 4; y++) {
                level.setBlock(new BlockPos(x0 - 1, y, z), wall, Block.UPDATE_ALL);
            }
        }
    }

    private static void build(ServerLevel level) {
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
        Direction s = Direction.SOUTH;

        // bedroom (walls on the north and west, camera to the south-east looking north-west)
        room(level, 0, 0, Blocks.BIRCH_PLANKS.defaultBlockState(), Blocks.PINK_TERRACOTTA.defaultBlockState());
        BlockState bed = f(FurnitureType.CANOPY_BED, FurnitureColor.BABY_PINK, Direction.NORTH);
        put(level, 3, Y, 0, bed.setValue(BedBlock.PART, BedPart.HEAD));
        put(level, 3, Y, 1, bed.setValue(BedBlock.PART, BedPart.FOOT));
        put(level, 3, Y + 1, 0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.WHITE, s).setValue(PillowBlock.PILLOWS, 2));
        put(level, 2, Y, 0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE, s));
        put(level, 2, Y + 1, 0, f(FurnitureType.NIGHT_LAMP, FurnitureColor.BABY_PINK, s));
        put(level, 4, Y, 0, f(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE, s).setValue(StorageBlock.OPEN, true));
        put(level, 4, Y + 1, 0, d(DecorType.PASTEL_CANDLES, s).setValue(LampBlock.LIT, true));
        tall(level, 0, Y, 0, f(FurnitureType.WARDROBE, FurnitureColor.BABY_PINK, s));
        tall(level, 1, Y, 0, f(FurnitureType.WARDROBE, FurnitureColor.BABY_PINK, s).setValue(StorageBlock.OPEN, true));
        put(level, 6, Y, 0, f(FurnitureType.VANITY_TABLE, FurnitureColor.BABY_PINK, s));
        put(level, 7, Y, 0, f(FurnitureType.DRESSER, FurnitureColor.WHITE, s));
        put(level, 7, Y + 1, 0, f(FurnitureType.JEWELRY_BOX, FurnitureColor.MINT, s).setValue(StorageBlock.OPEN, true));
        tall(level, 8, Y, 0, f(FurnitureType.STANDING_MIRROR, FurnitureColor.WHITE, s));
        tall(level, 5, Y + 1, 0, f(FurnitureType.CURTAINS, FurnitureColor.BABY_PINK, s));
        for (int x = 2; x <= 4; x++) {
            for (int z = 3; z <= 4; z++) {
                put(level, x, Y, z, f(FurnitureType.RUG, FurnitureColor.BABY_PINK, s));
            }
        }
        put(level, 6, Y, 3, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.LAVENDER, s).setValue(PillowBlock.PILLOWS, 3));
        put(level, 7, Y, 3, d(DecorType.TEDDY_BEAR, s));
        put(level, 8, Y, 3, d(DecorType.BUNNY_PLUSH, s));
        put(level, 0, Y, 3, d(DecorType.POTTED_ROSE_BUSH, s));
        SHOTS.add(new Shot("bedroom", 9.5, Y + 2.2, 8.5, 135, 20));

        // living room
        int lx = 14;
        room(level, lx, 0, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState());
        for (int x = lx + 2; x <= lx + 4; x++) {
            put(level, x, Y, 0, f(FurnitureType.SOFA, FurnitureColor.BABY_BLUE, s));
        }
        put(level, lx + 3, Y + 1, 0, f(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.WHITE, s));
        put(level, lx + 6, Y, 1, f(FurnitureType.ARMCHAIR, FurnitureColor.LAVENDER, Direction.WEST));
        put(level, lx + 3, Y, 2, f(FurnitureType.COFFEE_TABLE, FurnitureColor.WHITE, s));
        put(level, lx + 3, Y + 1, 2, f(FurnitureType.TEA_SET, FurnitureColor.BABY_PINK, s));
        put(level, lx, Y, 0, f(FurnitureType.BOOKSHELF, FurnitureColor.WHITE, s));
        put(level, lx, Y + 1, 0, d(DecorType.ROSE_VASE, s));
        put(level, lx + 7, Y, 0, f(FurnitureType.DISPLAY_CABINET, FurnitureColor.MINT, s));
        put(level, lx + 8, Y, 0, d(DecorType.POTTED_MONSTERA, s));
        put(level, lx + 1, Y, 0, d(DecorType.POTTED_CHERRY_BLOSSOM, s));
        put(level, lx + 8, Y, 3, d(DecorType.POTTED_LAVENDER, s));
        for (int x = lx + 2; x <= lx + 4; x++) {
            for (int z = 2; z <= 3; z++) {
                if (!(x == lx + 3 && z == 2)) {
                    put(level, x, Y, z, f(FurnitureType.RUG, FurnitureColor.BABY_BLUE, s));
                }
            }
        }
        tall(level, lx + 5, Y + 1, 0, f(FurnitureType.CURTAINS, FurnitureColor.WHITE, s).setValue(StorageBlock.OPEN, true));
        if (level.getBlockEntity(new BlockPos(lx + 7, Y, 0)) instanceof DisplayCabinetBlockEntity cabinet) {
            cabinet.setItem(0, new ItemStack(ModItems.decor(DecorType.TEDDY_BEAR)));
            cabinet.setItem(1, new ItemStack(Items.DIAMOND));
            cabinet.setItem(2, new ItemStack(ModItems.decor(DecorType.PERFUME_BOTTLES)));
            cabinet.setItem(3, new ItemStack(Items.CAKE));
        }
        SHOTS.add(new Shot("living_room", lx + 9.5, Y + 2.2, 8.5, 135, 20));

        // kitchen
        int kx = 28;
        room(level, kx, 0, Blocks.SMOOTH_QUARTZ.defaultBlockState(), Blocks.LIME_TERRACOTTA.defaultBlockState());
        tall(level, kx, Y, 0, f(FurnitureType.REFRIGERATOR, FurnitureColor.MINT, s));
        tall(level, kx + 1, Y, 0, f(FurnitureType.REFRIGERATOR, FurnitureColor.MINT, s).setValue(StorageBlock.OPEN, true));
        put(level, kx + 2, Y, 0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.CREAM, s));
        put(level, kx + 2, Y + 1, 0, d(DecorType.CUPCAKE_STAND, s));
        put(level, kx + 3, Y, 0, f(FurnitureType.KITCHEN_SINK, FurnitureColor.WHITE, s).setValue(SinkBlock.RUNNING, true));
        put(level, kx + 4, Y, 0, f(FurnitureType.STOVE, FurnitureColor.BABY_BLUE, s).setValue(StoveBlock.LIT, true));
        put(level, kx + 5, Y, 0, f(FurnitureType.KITCHEN_CABINET, FurnitureColor.MINT, s).setValue(StorageBlock.OPEN, true));
        put(level, kx + 6, Y, 0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.CREAM, s).setValue(StorageBlock.OPEN, true));
        put(level, kx + 6, Y + 1, 0, d(DecorType.STRAWBERRY_BASKET, s));
        put(level, kx + 3, Y, 3, f(FurnitureType.DINING_TABLE, FurnitureColor.LAVENDER, s));
        put(level, kx + 4, Y, 3, f(FurnitureType.DINING_TABLE, FurnitureColor.LAVENDER, s));
        put(level, kx + 3, Y + 1, 3, f(FurnitureType.TEA_SET, FurnitureColor.MINT, s));
        put(level, kx + 4, Y + 1, 3, d(DecorType.TULIP_VASE, s));
        put(level, kx + 3, Y, 4, f(FurnitureType.DINING_CHAIR, FurnitureColor.BABY_PINK, Direction.NORTH));
        put(level, kx + 4, Y, 4, f(FurnitureType.DINING_CHAIR, FurnitureColor.MINT, Direction.NORTH));
        put(level, kx + 3, Y, 2, f(FurnitureType.DINING_CHAIR, FurnitureColor.BABY_PINK, s));
        put(level, kx + 2, Y, 3, f(FurnitureType.DINING_CHAIR, FurnitureColor.MINT, Direction.EAST));
        SHOTS.add(new Shot("kitchen", kx + 9.5, Y + 2.2, 8.5, 135, 20));

        // bathroom
        int bx = 42;
        room(level, bx, 0, Blocks.WHITE_CONCRETE.defaultBlockState(), Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState());
        BlockState tub = f(FurnitureType.BATHTUB, FurnitureColor.BABY_BLUE, Direction.WEST).setValue(BathtubBlock.FILLED, true);
        put(level, bx, Y, 1, tub.setValue(BathtubBlock.PART, BedPart.HEAD));
        put(level, bx + 1, Y, 1, tub.setValue(BathtubBlock.PART, BedPart.FOOT));
        put(level, bx + 3, Y, 0, f(FurnitureType.BATHROOM_SINK, FurnitureColor.WHITE, s).setValue(SinkBlock.RUNNING, true));
        put(level, bx + 3, Y + 1, 0, f(FurnitureType.WALL_MIRROR, FurnitureColor.BABY_PINK, s));
        put(level, bx + 4, Y, 0, f(FurnitureType.BATHROOM_SINK, FurnitureColor.LAVENDER, s));
        put(level, bx + 4, Y + 1, 0, f(FurnitureType.BATHROOM_CABINET, FurnitureColor.MINT, s).setValue(StorageBlock.OPEN, true));
        put(level, bx + 5, Y + 1, 0, f(FurnitureType.TOWEL_RACK, FurnitureColor.LAVENDER, s));
        put(level, bx + 6, Y, 0, f(FurnitureType.KITCHEN_COUNTER, FurnitureColor.WHITE, s));
        put(level, bx + 6, Y + 1, 0, f(FurnitureType.SOAP_DISPENSER, FurnitureColor.BABY_PINK, s));
        put(level, bx + 7, Y + 1, 0, f(FurnitureType.BATHROOM_CABINET, FurnitureColor.WHITE, s));
        put(level, bx + 7, Y, 0, d(DecorType.ANGEL_FIGURINE, s));
        SHOTS.add(new Shot("bathroom", bx + 8.5, Y + 2.2, 7.5, 135, 20));

        // garden
        int gx = 56;
        for (int x = gx - 1; x < gx + 10; x++) {
            for (int z = -1; z < 8; z++) {
                level.setBlock(new BlockPos(x, Y - 1, z), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            }
            put(level, x, Y, -1, f(FurnitureType.PICKET_FENCE, FurnitureColor.WHITE, s));
        }
        for (int x = gx; x <= gx + 2; x++) {
            put(level, x, Y, 0, f(FurnitureType.GARDEN_BENCH, FurnitureColor.WHITE, s));
        }
        tall(level, gx + 4, Y, 0, f(FurnitureType.SWING_SEAT, FurnitureColor.CREAM, s));
        put(level, gx + 6, Y, 0, f(FurnitureType.BIRDHOUSE, FurnitureColor.BABY_BLUE, s));
        put(level, gx + 7, Y, 1, f(FurnitureType.OUTDOOR_TABLE, FurnitureColor.MINT, s));
        put(level, gx + 7, Y + 1, 1, f(FurnitureType.FLOWER_POT, FurnitureColor.BABY_PINK, s));
        for (int x = gx; x <= gx + 2; x++) {
            for (int z = 3; z <= 4; z++) {
                put(level, x, Y, z, f(FurnitureType.PICNIC_BLANKET, FurnitureColor.BABY_PINK, s));
            }
        }
        put(level, gx + 1, Y + 1, 3, d(DecorType.STRAWBERRY_BASKET, s));
        put(level, gx + 2, Y + 1, 4, d(DecorType.RIBBON_GIFT_BOX, s));
        SHOTS.add(new Shot("garden", gx + 9.5, Y + 2.2, 8.5, 135, 20));

        // every colourway of a few pieces
        int cz = 14;
        FurnitureColor[] colors = FurnitureColor.values();
        FurnitureType[] lineup = {FurnitureType.SOFA, FurnitureType.ARMCHAIR, FurnitureType.DRESSER, FurnitureType.STOVE};
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < lineup.length; j++) {
                put(level, i * 2, Y, cz + j * 2, f(lineup[j], colors[i], s));
            }
            tall(level, i * 2, Y, cz + 8, f(FurnitureType.REFRIGERATOR, colors[i], s));
        }
        SHOTS.add(new Shot("colours", 5, Y + 6, cz + 15, 180, 35));
        SHOTS.add(new Shot("bed_closeup", 6.2, Y + 1.6, 4.6, 145, 18));
        SHOTS.add(new Shot("vanity_closeup", 8.6, Y + 1.2, 2.9, 128, 12));
        SHOTS.add(new Shot("wardrobes_closeup", 2.6, Y + 1.4, 4.2, 165, 12));
        settle(level);
    }
}
