package com.sugarplumstudios.coquette.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Draws the catalog's large preview of the selected piece: its item model rendered in 3D into its own texture
 * (a GUI "picture in picture", since 1.21.6 GUI items are otherwise drawn from a 16px atlas), slowly turning.
 * Each loader registers this renderer for {@link State}.
 */
public class BlockPreviewRenderer extends PictureInPictureRenderer<BlockPreviewRenderer.State> {
    /** One preview: the item, its turn in degrees and the screen box it fills. */
    public record State(ItemStackRenderState item, float yaw, int x0, int y0, int x1, int y1, float scale,
                        @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        @Override
        public @Nullable ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
        }
    }

    /** Builds the state for an item shown in a box of the given size, turned by {@code yaw} degrees. */
    public static State state(ItemStack stack, float yaw, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissor) {
        Minecraft mc = Minecraft.getInstance();
        ItemStackRenderState item = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(item, stack, ItemDisplayContext.NONE, mc.level, mc.player, 0);
        // a block fills about 60% of the box, like the 2.5x scaled inventory icon used to
        return new State(item, yaw, x0, y0, x1, y1, Math.min(x1 - x0, y1 - y0) * 0.6F, scissor);
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected void renderToTexture(State state, PoseStack pose, SubmitNodeCollector collector) {
        // the texture's y axis points down; turn the model the right way up, then tilt it towards the viewer
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.rotate(Axis.XP.rotationDegrees(30.0F));
        pose.rotate(Axis.YP.rotationDegrees(225.0F + state.yaw()));
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        state.item().submit(pose, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "coquettefurniture_block_preview";
    }
}
