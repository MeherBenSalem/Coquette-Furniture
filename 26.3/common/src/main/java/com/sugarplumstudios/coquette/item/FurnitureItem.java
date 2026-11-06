package com.sugarplumstudios.coquette.item;

import com.sugarplumstudios.coquette.block.BathtubBlock;
import com.sugarplumstudios.coquette.block.Feature;
import com.sugarplumstudios.coquette.catalog.FurnitureSet;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A furniture block item whose tooltip lists what the piece does and which sets it belongs to. */
public class FurnitureItem extends BlockItem {
    private final List<Feature> features;
    private final int storageSlots;
    private final boolean dyeable;

    public FurnitureItem(Block block, Properties properties, List<Feature> features, int storageSlots, boolean dyeable) {
        super(block, properties);
        this.features = features;
        this.storageSlots = storageSlots;
        this.dyeable = dyeable;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (getBlock() instanceof BedBlock || getBlock() instanceof BathtubBlock) {
            // like vanilla beds: don't wake the neighbours until the other half exists
            return context.getLevel().setBlock(context.getClickedPos(), state,
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE | Block.UPDATE_KNOWN_SHAPE);
        }
        return super.placeBlock(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (CoquetteConfig.CLIENT.featureTooltips.get()) {
            for (Feature feature : features) {
                tooltip.accept(feature.describe(storageSlots));
            }
            if (dyeable) {
                tooltip.accept(Feature.DYE.describe(0));
            }
        }
        if (CoquetteConfig.CLIENT.setTooltips.get()) {
            for (FurnitureSet set : FurnitureSet.setsContaining(this)) {
                tooltip.accept(Component.translatable("tooltip.coquettefurniture.set", Component.translatable(set.translationKey()))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(set.color())).withItalic(true)));
            }
        }
    }
}
