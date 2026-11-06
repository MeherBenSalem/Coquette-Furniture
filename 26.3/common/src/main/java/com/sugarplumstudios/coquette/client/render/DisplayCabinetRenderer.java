package com.sugarplumstudios.coquette.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sugarplumstudios.coquette.block.FurnitureBlock;
import com.sugarplumstudios.coquette.block.ShapeUtil;
import com.sugarplumstudios.coquette.block.entity.DisplayCabinetBlockEntity;
import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws the four items on show inside a display cabinet, two on each glass shelf. */
public class DisplayCabinetRenderer implements BlockEntityRenderer<DisplayCabinetBlockEntity, DisplayCabinetRenderer.State> {
    /** North-frame slot positions in pixels {x, y, z}; slot 0/1 on the floor, 2/3 on the middle shelf. */
    private static final double[][] SLOTS = {{10.5, 2, 8.5}, {5.5, 2, 8.5}, {10.5, 8.25, 8.5}, {5.5, 8.25, 8.5}};

    private final ItemModelResolver itemModelResolver;

    public DisplayCabinetRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    /** What the renderer needs from a cabinet, captured once per frame. */
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float spin;
        final ItemStackRenderState[] items = new ItemStackRenderState[DisplayCabinetBlockEntity.SLOTS];
        final boolean[] blocks = new boolean[DisplayCabinetBlockEntity.SLOTS];

        State() {
            for (int i = 0; i < items.length; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DisplayCabinetBlockEntity cabinet, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(cabinet, state, partialTicks, cameraPosition, breakProgress);
        state.facing = cabinet.getBlockState().getValue(FurnitureBlock.FACING);
        state.spin = cabinet.getLevel() != null && CoquetteConfig.CLIENT.displayCabinetSpin.get()
                ? (cabinet.getLevel().getGameTime() + partialTicks) * 1.5F % 360.0F : 0.0F;
        int seed = (int) cabinet.getBlockPos().asLong();
        for (int i = 0; i < DisplayCabinetBlockEntity.SLOTS; i++) {
            ItemStack stack = cabinet.getItem(i);
            state.blocks[i] = stack.getItem() instanceof BlockItem;
            itemModelResolver.updateForTopItem(state.items[i], stack, ItemDisplayContext.FIXED, cabinet.getLevel(), null, seed + i);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < DisplayCabinetBlockEntity.SLOTS; i++) {
            ItemStackRenderState item = state.items[i];
            if (item.isEmpty()) {
                continue;
            }
            double[] at = ShapeUtil.toWorld(state.facing, SLOTS[i][0] / 16.0D, SLOTS[i][2] / 16.0D);
            boolean block = state.blocks[i];
            pose.pushPose();
            // items are drawn centred on the pose, so lift each by half its height to stand on the shelf
            pose.translate(at[0], SLOTS[i][1] / 16.0D + (block ? 0.14D : 0.165D), at[1]);
            pose.rotate(Axis.YP.rotationDegrees(180.0F - state.facing.toYRot() + state.spin + i * 40.0F));
            float scale = block ? 0.55F : 0.32F;
            pose.scale(scale, scale, scale);
            item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
