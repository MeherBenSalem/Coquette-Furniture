package com.sugarplumstudios.coquette.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.coquette.block.FurnitureBlock;
import com.sugarplumstudios.coquette.block.ShapeUtil;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the four items on show inside a display cabinet, two on each glass shelf. */
public class DisplayCabinetRenderer implements BlockEntityRenderer<DisplayCabinetBlockEntity> {
    /** North-frame slot positions in pixels {x, y, z}; slot 0/1 on the floor, 2/3 on the middle shelf. */
    private static final double[][] SLOTS = {{10.5, 2, 8.5}, {5.5, 2, 8.5}, {10.5, 8.25, 8.5}, {5.5, 8.25, 8.5}};

    public DisplayCabinetRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DisplayCabinetBlockEntity cabinet, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        if (cabinet.getLevel() == null) {
            return;
        }
        Direction facing = cabinet.getBlockState().getValue(FurnitureBlock.FACING);
        float spin = CoquetteConfig.CLIENT.displayCabinetSpin.get()
                ? (cabinet.getLevel().getGameTime() + partialTick) * 1.5F % 360.0F : 0.0F;
        for (int i = 0; i < DisplayCabinetBlockEntity.SLOTS; i++) {
            ItemStack stack = cabinet.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            double[] at = ShapeUtil.toWorld(facing, SLOTS[i][0] / 16.0D, SLOTS[i][2] / 16.0D);
            boolean block = stack.getItem() instanceof BlockItem;
            pose.pushPose();
            // items are drawn centred on the pose, so lift each by half its height to stand on the shelf
            pose.translate(at[0], SLOTS[i][1] / 16.0D + (block ? 0.14D : 0.165D), at[1]);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot() + spin + i * 40.0F));
            float scale = block ? 0.55F : 0.32F;
            pose.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffers, cabinet.getLevel(), (int) cabinet.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
