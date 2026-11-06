package com.sugarplumstudios.coquette.gametest;

import com.sugarplumstudios.coquette.CoquetteFurniture;
import com.sugarplumstudios.coquette.block.BathtubBlock;
import com.sugarplumstudios.coquette.block.CandleDecorBlock;
import com.sugarplumstudios.coquette.block.ConnectedSeatBlock;
import com.sugarplumstudios.coquette.block.ConnectingBlock;
import com.sugarplumstudios.coquette.block.DecorBlock;
import com.sugarplumstudios.coquette.block.DecorSurface;
import com.sugarplumstudios.coquette.block.DecorType;
import com.sugarplumstudios.coquette.block.FurnitureBlock;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.FurnitureType;
import com.sugarplumstudios.coquette.block.LampBlock;
import com.sugarplumstudios.coquette.block.PillowBlock;
import com.sugarplumstudios.coquette.block.Recolor;
import com.sugarplumstudios.coquette.block.SinkBlock;
import com.sugarplumstudios.coquette.block.StorageBlock;
import com.sugarplumstudios.coquette.block.TallFurnitureBlock;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.block.entity.FurnitureStorageBlockEntity;
import com.sugarplumstudios.coquette.block.entity.StoveBlockEntity;
import com.sugarplumstudios.coquette.entity.SeatEntity;
import com.sugarplumstudios.coquette.registry.ModBlocks;
import com.sugarplumstudios.coquette.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(CoquetteFurniture.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CoquetteGameTests {
    private static final String TEMPLATE = "empty";

    private CoquetteGameTests() {
    }

    private static InteractionResult use(GameTestHelper helper, BlockPos relative, Player player) {
        BlockPos pos = helper.absolutePos(relative);
        BlockState state = helper.getLevel().getBlockState(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
        // as ServerPlayerGameMode#useItemOn does: the held item first, then the empty-handed interaction
        ItemInteractionResult withItem = state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        if (withItem == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            return state.useWithoutItem(helper.getLevel(), player, hit);
        }
        return withItem.result();
    }

    private static Player playerNear(GameTestHelper helper, BlockPos relative) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos pos = helper.absolutePos(relative);
        player.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() - 0.6D);
        return player;
    }

    /** Re-reads every neighbour like a real placement would, for blocks set directly by the test. */
    private static void settle(GameTestHelper helper, BlockPos... relative) {
        ServerLevel level = helper.getLevel();
        for (BlockPos rel : relative) {
            BlockPos pos = helper.absolutePos(rel);
            level.setBlock(pos, Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos), Block.UPDATE_ALL);
        }
    }

    private static boolean holds(Player player, net.minecraft.world.item.Item item) {
        if (player.getMainHandItem().is(item)) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }

    @GameTest(template = TEMPLATE)
    public static void everyPieceHasRecipesLootAndItem(GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();
        for (FurnitureType type : FurnitureType.values()) {
            for (FurnitureColor color : FurnitureColor.values()) {
                String name = type.id() + "/" + color.id();
                helper.assertTrue(recipes.byKey(CoquetteFurniture.id(name)).isPresent(), "Missing recipe " + name);
                helper.assertTrue(recipes.byKey(CoquetteFurniture.id(name + "_from_dyeing")).isPresent(), "Missing dye recipe " + name);
                Block block = ModBlocks.furniture(type, color);
                helper.assertTrue(block.asItem() == ModItems.furniture(type, color), "Item mismatch for " + name);
                LootTable loot = helper.getLevel().getServer().reloadableRegistries().getLootTable(block.getLootTable());
                helper.assertTrue(loot != LootTable.EMPTY, "Missing loot table for " + name);
            }
        }
        for (DecorType decor : DecorType.values()) {
            helper.assertTrue(recipes.byKey(CoquetteFurniture.id("decor/" + decor.id())).isPresent(), "Missing recipe " + decor.id());
            LootTable loot = helper.getLevel().getServer().reloadableRegistries().getLootTable(ModBlocks.decor(decor).getLootTable());
            helper.assertTrue(loot != LootTable.EMPTY, "Missing loot table for " + decor.id());
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void chairSeatsPlayer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.furniture(FurnitureType.DINING_CHAIR, FurnitureColor.MINT).defaultBlockState());
        Player player = playerNear(helper, pos);
        use(helper, pos, player);
        helper.assertTrue(player.getVehicle() instanceof SeatEntity, "Player should be sitting on the chair");
        double seatTop = helper.absolutePos(pos).getY() + 10.5D / 16.0D;
        helper.assertTrue(Math.abs(player.getVehicle().getY() - seatTop) < 0.01D, "Seat should be at cushion height");
        player.stopRiding();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void sofasJoinIntoOnePiece(GameTestHelper helper) {
        BlockState sofa = ModBlocks.furniture(FurnitureType.SOFA, FurnitureColor.BABY_PINK).defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        BlockPos a = new BlockPos(1, 1, 2);
        BlockPos b = new BlockPos(2, 1, 2);
        BlockPos c = new BlockPos(3, 1, 2);
        helper.setBlock(a, sofa);
        helper.setBlock(b, sofa);
        helper.setBlock(c, sofa);
        settle(helper, a, b, c);
        helper.assertBlockProperty(a, ConnectedSeatBlock.SHAPE, ConnectedSeatBlock.SeatShape.LEFT);
        helper.assertBlockProperty(b, ConnectedSeatBlock.SHAPE, ConnectedSeatBlock.SeatShape.MIDDLE);
        helper.assertBlockProperty(c, ConnectedSeatBlock.SHAPE, ConnectedSeatBlock.SeatShape.RIGHT);
        helper.setBlock(b, Blocks.AIR);
        helper.assertBlockProperty(a, ConnectedSeatBlock.SHAPE, ConnectedSeatBlock.SeatShape.SINGLE);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tablesAndRugsJoin(GameTestHelper helper) {
        Block table = ModBlocks.furniture(FurnitureType.DINING_TABLE, FurnitureColor.CREAM);
        Block rug = ModBlocks.furniture(FurnitureType.RUG, FurnitureColor.LAVENDER);
        BlockPos t1 = new BlockPos(1, 1, 1);
        BlockPos t2 = new BlockPos(2, 1, 1);
        BlockPos r1 = new BlockPos(1, 1, 3);
        BlockPos r2 = new BlockPos(1, 1, 4);
        helper.setBlock(t1, table);
        helper.setBlock(t2, table);
        helper.setBlock(r1.below(), Blocks.STONE);
        helper.setBlock(r2.below(), Blocks.STONE);
        helper.setBlock(r1, rug);
        helper.setBlock(r2, rug);
        settle(helper, t1, t2, r1, r2);
        helper.assertBlockProperty(t1, ConnectingBlock.EAST, true);
        helper.assertBlockProperty(t2, ConnectingBlock.WEST, true);
        helper.assertBlockProperty(t1, ConnectingBlock.WEST, false);
        helper.assertBlockProperty(r1, ConnectingBlock.SOUTH, true);
        helper.assertBlockProperty(r2, ConnectingBlock.NORTH, true);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void decorSinksOntoSurfaces(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Object[][] cases = {
                {ModBlocks.furniture(FurnitureType.COFFEE_TABLE, FurnitureColor.WHITE).defaultBlockState(), 4},
                {ModBlocks.furniture(FurnitureType.BEDSIDE_TABLE, FurnitureColor.WHITE).defaultBlockState(), 1},
                {ModBlocks.furniture(FurnitureType.DRESSER, FurnitureColor.WHITE).defaultBlockState(), 0},
                {ModBlocks.furniture(FurnitureType.SOFA, FurnitureColor.WHITE).defaultBlockState(), 4},
                {Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM), 4},
                {Blocks.STONE.defaultBlockState(), 0}};
        for (int i = 0; i < cases.length; i++) {
            BlockPos below = new BlockPos(i % 5, 1, i / 5 * 2 + 1);
            helper.setBlock(below, (BlockState) cases[i][0]);
            int sink = DecorSurface.sinkFor(level, helper.absolutePos(below.above()));
            helper.assertTrue(sink == (int) cases[i][1], "Sink above " + cases[i][0] + " was " + sink);
        }
        BlockPos lamp = new BlockPos(0, 2, 1);
        helper.setBlock(lamp, ModBlocks.furniture(FurnitureType.NIGHT_LAMP, FurnitureColor.BABY_PINK).defaultBlockState());
        settle(helper, lamp);
        helper.assertBlockProperty(lamp, DecorBlock.SINK, 4);
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
        helper.assertBlockProperty(lamp, DecorBlock.SINK, 0);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pillowsStackAndSeat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.furniture(FurnitureType.DECORATIVE_PILLOW, FurnitureColor.LAVENDER).defaultBlockState()
                .setValue(PillowBlock.PILLOWS, 3));
        Player player = playerNear(helper, pos);
        use(helper, pos, player);
        helper.assertTrue(player.getVehicle() instanceof SeatEntity, "Player should sit on the pillows");
        player.stopRiding();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void sinkGivesWater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.furniture(FurnitureType.KITCHEN_SINK, FurnitureColor.WHITE).defaultBlockState());
        Player player = playerNear(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        use(helper, pos, player);
        helper.assertTrue(holds(player, Items.WATER_BUCKET), "Sink should fill the bucket");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        use(helper, pos, player);
        boolean bottle = player.getInventory().items.stream().anyMatch(CoquetteGameTests::isWaterBottle)
                || isWaterBottle(player.getMainHandItem());
        helper.assertTrue(bottle, "Sink should fill the bottle");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, pos, player);
        helper.assertBlockProperty(pos, SinkBlock.RUNNING, true);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void bathtubFillsDrainsAndSeats(GameTestHelper helper) {
        Block tub = ModBlocks.furniture(FurnitureType.BATHTUB, FurnitureColor.BABY_BLUE);
        BlockPos foot = new BlockPos(2, 1, 3);
        BlockPos head = new BlockPos(2, 1, 2);
        helper.setBlock(foot, tub.defaultBlockState().setValue(BathtubBlock.FACING, Direction.NORTH).setValue(BathtubBlock.PART, BedPart.FOOT));
        helper.setBlock(head, tub.defaultBlockState().setValue(BathtubBlock.FACING, Direction.NORTH).setValue(BathtubBlock.PART, BedPart.HEAD));
        Player player = playerNear(helper, foot);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        use(helper, foot, player);
        helper.assertBlockProperty(foot, BathtubBlock.FILLED, true);
        helper.assertBlockProperty(head, BathtubBlock.FILLED, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, foot, player);
        helper.assertTrue(player.getVehicle() instanceof SeatEntity, "Player should be in the bath");
        player.stopRiding();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        use(helper, head, player);
        helper.assertBlockProperty(foot, BathtubBlock.FILLED, false);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void recolorKeepsContentsAndBothHalves(GameTestHelper helper) {
        Block pink = ModBlocks.furniture(FurnitureType.WARDROBE, FurnitureColor.BABY_PINK);
        Block mint = ModBlocks.furniture(FurnitureType.WARDROBE, FurnitureColor.MINT);
        BlockPos lower = new BlockPos(2, 1, 2);
        BlockPos upper = lower.above();
        helper.setBlock(lower, pink.defaultBlockState().setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(upper, pink.defaultBlockState().setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.UPPER));
        FurnitureStorageBlockEntity storage = (FurnitureStorageBlockEntity) helper.getBlockEntity(lower);
        helper.assertTrue(storage.getContainerSize() == 54, "Wardrobe should hold 54 stacks");
        storage.setItem(7, new ItemStack(Items.DIAMOND, 5));
        helper.assertTrue(Recolor.tryRecolor(helper.getLevel(), helper.absolutePos(upper), FurnitureColor.MINT), "Recolour failed");
        helper.assertBlockPresent(mint, lower);
        helper.assertBlockPresent(mint, upper);
        FurnitureStorageBlockEntity moved = (FurnitureStorageBlockEntity) helper.getBlockEntity(lower);
        helper.assertTrue(moved.getItem(7).is(Items.DIAMOND) && moved.getItem(7).getCount() == 5, "Contents should survive the recolour");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(helper.absolutePos(lower)).inflate(2)).isEmpty(), "Nothing should spill");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void storageSizesAndOpenModel(GameTestHelper helper) {
        Object[][] cases = {{FurnitureType.DRESSER, 27}, {FurnitureType.BEDSIDE_TABLE, 9}, {FurnitureType.KITCHEN_CABINET, 18},
                {FurnitureType.JEWELRY_BOX, 5}, {FurnitureType.VANITY_TABLE, 9}, {FurnitureType.BATHROOM_CABINET, 9}};
        for (int i = 0; i < cases.length; i++) {
            BlockPos pos = new BlockPos(i % 5, 1, i / 5 * 2);
            helper.setBlock(pos, ModBlocks.furniture((FurnitureType) cases[i][0], FurnitureColor.CREAM));
            FurnitureStorageBlockEntity storage = (FurnitureStorageBlockEntity) helper.getBlockEntity(pos);
            helper.assertTrue(storage.getContainerSize() == (int) cases[i][1], cases[i][0] + " has " + storage.getContainerSize() + " slots");
        }
        BlockPos dresser = new BlockPos(0, 1, 0);
        Block block = ModBlocks.furniture(FurnitureType.DRESSER, FurnitureColor.CREAM);
        ((StorageBlock) block).setOpen(helper.getLevel(), helper.absolutePos(dresser), helper.getLevel().getBlockState(helper.absolutePos(dresser)), true);
        helper.assertBlockProperty(dresser, StorageBlock.OPEN, true);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void canopyBedIsARealBed(GameTestHelper helper) {
        Block bed = ModBlocks.furniture(FurnitureType.CANOPY_BED, FurnitureColor.LAVENDER);
        BlockPos foot = new BlockPos(2, 1, 3);
        BlockPos head = new BlockPos(2, 1, 2);
        BlockState footState = bed.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.BedBlock.PART, BedPart.FOOT);
        helper.setBlock(foot, footState);
        helper.setBlock(head, footState.setValue(net.minecraft.world.level.block.BedBlock.PART, BedPart.HEAD));
        Player player = playerNear(helper, foot);
        BlockPos absHead = helper.absolutePos(head);
        helper.assertTrue(helper.getLevel().getBlockState(absHead).isBed(helper.getLevel(), absHead, player), "Canopy bed should be a bed");
        helper.assertTrue(helper.getLevel().getBlockEntity(absHead) == null, "Canopy bed renders as a baked model, no block entity");
        List<ItemStack> headDrops = Block.getDrops(helper.getLevel().getBlockState(absHead), helper.getLevel(), absHead, null);
        List<ItemStack> footDrops = Block.getDrops(footState, helper.getLevel(), helper.absolutePos(foot), null);
        helper.assertTrue(headDrops.size() == 1 && footDrops.isEmpty(), "Only the head drops the bed");
        helper.assertTrue(DecorSurface.sinkFor(helper.getLevel(), absHead.above()) == 4, "Pillows should sink onto the mattress");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void stoveCooksLikeASmoker(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.furniture(FurnitureType.STOVE, FurnitureColor.MINT));
        StoveBlockEntity stove = (StoveBlockEntity) helper.getBlockEntity(pos);
        stove.setItem(0, new ItemStack(Items.BEEF));
        stove.setItem(1, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> helper.assertTrue(stove.getItem(2).is(Items.COOKED_BEEF), "Beef not cooked yet"));
    }

    @GameTest(template = TEMPLATE)
    public static void lampsCandlesAndCurtainsToggle(GameTestHelper helper) {
        BlockPos lamp = new BlockPos(1, 1, 1);
        BlockPos candles = new BlockPos(3, 1, 1);
        BlockPos curtain = new BlockPos(2, 1, 3);
        helper.setBlock(lamp, ModBlocks.furniture(FurnitureType.NIGHT_LAMP, FurnitureColor.MINT));
        helper.setBlock(candles, ModBlocks.decor(DecorType.PASTEL_CANDLES));
        Block curtains = ModBlocks.furniture(FurnitureType.CURTAINS, FurnitureColor.WHITE);
        helper.setBlock(curtain, curtains.defaultBlockState().setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(curtain.above(), curtains.defaultBlockState().setValue(TallFurnitureBlock.HALF, DoubleBlockHalf.UPPER));
        Player player = playerNear(helper, lamp);
        use(helper, lamp, player);
        helper.assertBlockProperty(lamp, LampBlock.LIT, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        use(helper, candles, player);
        helper.assertBlockProperty(candles, LampBlock.LIT, true);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(candles)).getLightEmission() == 12, "Lit candles glow");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, curtain.above(), player);
        helper.assertBlockProperty(curtain, StorageBlock.OPEN, true);
        helper.assertBlockProperty(curtain.above(), StorageBlock.OPEN, true);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(candles)).getBlock() instanceof CandleDecorBlock, "Candles");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void displayCabinetShowsAndReturnsItems(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.furniture(FurnitureType.DISPLAY_CABINET, FurnitureColor.BABY_BLUE).defaultBlockState()
                .setValue(FurnitureBlock.FACING, Direction.NORTH));
        Player player = playerNear(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.decor(DecorType.TEDDY_BEAR)));
        use(helper, pos, player);
        DisplayCabinetBlockEntity cabinet = (DisplayCabinetBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(cabinet.lastFilledSlot() >= 0, "Teddy should be on display");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, pos, player);
        helper.assertTrue(cabinet.lastFilledSlot() < 0, "Teddy should be taken back");
        helper.succeed();
    }
}
