package com.sugarplumstudios.coquette.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FenceBlock;

/**
 * A painted picket fence. It is a real fence: it joins other wooden fences and
 * gates, holds leads and blocks mobs (it is in the vanilla fence tags).
 */
public class PicketFenceBlock extends FenceBlock {
    private final FurnitureColor color;

    public PicketFenceBlock(FurnitureColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    public MapCodec<FenceBlock> codec() {
        return simpleCodec(properties -> new PicketFenceBlock(color, properties));
    }

    public FurnitureColor furnitureColor() {
        return color;
    }
}
