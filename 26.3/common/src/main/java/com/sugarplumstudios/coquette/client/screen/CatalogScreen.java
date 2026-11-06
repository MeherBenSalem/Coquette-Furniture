package com.sugarplumstudios.coquette.client.screen;

import com.sugarplumstudios.coquette.block.Feature;
import com.sugarplumstudios.coquette.block.FurnitureColor;
import com.sugarplumstudios.coquette.block.Room;
import com.sugarplumstudios.coquette.catalog.CatalogEntry;
import com.sugarplumstudios.coquette.catalog.FurnitureSet;
import com.sugarplumstudios.coquette.client.CoquetteClient;
import com.sugarplumstudios.coquette.client.render.BlockPreviewRenderer;
import com.sugarplumstudios.coquette.network.CatalogTakePacket;
import com.sugarplumstudios.coquette.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interior Designer Mode: browse every piece by room or by themed set, filter by
 * colour or name, see what each piece does and how to craft it, and (in creative)
 * have it delivered straight to your inventory.
 */
public class CatalogScreen extends Screen {
    private static final int PANEL = 0xFFFFF4F8;
    private static final int PANEL_SHADE = 0xFFFDE6EF;
    private static final int BORDER = 0xFFF2A7C3;
    private static final int BORDER_DARK = 0xFFD97CA0;
    private static final int TAB = 0xFFFDE3EE;
    private static final int TAB_SELECTED = 0xFFF7B8CF;
    private static final int TEXT = 0xFF7A3E5B;
    private static final int TEXT_SOFT = 0xFFA86F8B;
    private static final int SLOT = 0xFFFFFFFF;
    private static final int SLOT_EDGE = 0xFFF3CFDD;
    private static final int SLOT_SELECTED = 0xFFF9C9DB;
    private static final int CELL = 18;
    private static final int SIDEBAR = 86;
    private static final int DETAIL = 128;
    private static final int GRID_TOP = 58;

    private enum Tab {
        ALL("room.coquettefurniture.all", null),
        BEDROOM(Room.BEDROOM.translationKey(), Room.BEDROOM),
        LIVING_ROOM(Room.LIVING_ROOM.translationKey(), Room.LIVING_ROOM),
        KITCHEN(Room.KITCHEN.translationKey(), Room.KITCHEN),
        BATHROOM(Room.BATHROOM.translationKey(), Room.BATHROOM),
        GARDEN(Room.GARDEN.translationKey(), Room.GARDEN),
        DECOR(Room.DECOR.translationKey(), Room.DECOR),
        SETS("room.coquettefurniture.sets", null);

        final String key;
        @Nullable
        final Room room;

        Tab(String key, @Nullable Room room) {
            this.key = key;
            this.room = room;
        }
    }

    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int columns;
    private int rows;

    private Tab tab = Tab.ALL;
    @Nullable
    private FurnitureColor colorFilter;
    private String query = "";
    private int scroll;
    private List<CatalogEntry> shown = List.of();
    @Nullable
    private CatalogEntry selected;
    @Nullable
    private FurnitureSet selectedSet;
    @Nullable
    private RecipeDisplay recipe;
    private ItemStack hovered = ItemStack.EMPTY;
    @Nullable
    private Component hoveredLabel;

    /** Set by the dev showcase: the cursor sits over the grid under Xvfb, and its tooltip would hide the detail panel. */
    private boolean showcase;

    private EditBox search;
    private Button takeOne;
    private Button takeStack;
    private Button takeSet;

    public CatalogScreen() {
        super(Component.translatable("gui.coquettefurniture.catalog.title"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(392, width - 8);
        panelHeight = Math.min(240, height - 8);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int gridWidth = panelWidth - SIDEBAR - DETAIL - 18;
        columns = Math.max(3, gridWidth / CELL);
        rows = Math.max(3, (panelHeight - GRID_TOP - 6) / CELL);

        search = new EditBox(font, left + SIDEBAR + 5, top + 25, columns * CELL - 2, 13,
                Component.translatable("gui.coquettefurniture.catalog.search"));
        search.setHint(Component.translatable("gui.coquettefurniture.catalog.search").withStyle(s -> s.withColor(TEXT_SOFT)));
        search.setMaxLength(40);
        search.setValue(query);
        search.setResponder(value -> {
            query = value.toLowerCase(Locale.ROOT);
            scroll = 0;
            refilter();
        });
        addRenderableWidget(search);

        int dx = left + panelWidth - DETAIL + 4;
        int by = top + panelHeight - 24;
        takeOne = addRenderableWidget(Button.builder(Component.translatable("gui.coquettefurniture.catalog.take"),
                b -> take(false)).bounds(dx, by, 59, 18).build());
        takeStack = addRenderableWidget(Button.builder(Component.translatable("gui.coquettefurniture.catalog.take_stack"),
                b -> take(true)).bounds(dx + 61, by, 59, 18).build());
        takeSet = addRenderableWidget(Button.builder(Component.translatable("gui.coquettefurniture.catalog.take_set"),
                b -> takeWholeSet()).bounds(dx, by, DETAIL - 8, 18).build());
        refilter();
        updateButtons();
    }

    private boolean creative() {
        return minecraft != null && minecraft.player != null && minecraft.player.isCreative();
    }

    private void updateButtons() {
        boolean sets = tab == Tab.SETS;
        takeOne.visible = takeStack.visible = !sets && selected != null && creative();
        takeSet.visible = sets && selectedSet != null && creative();
        search.visible = !sets;
    }

    private void refilter() {
        List<CatalogEntry> out = new ArrayList<>();
        for (CatalogEntry entry : CatalogEntry.all()) {
            if (tab.room != null && entry.room() != tab.room) {
                continue;
            }
            if (colorFilter != null && entry.color() != null && entry.color() != colorFilter) {
                continue;
            }
            if (colorFilter != null && entry.color() == null && tab != Tab.DECOR) {
                continue;
            }
            if (!query.isEmpty() && !new ItemStack(entry.item()).getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            out.add(entry);
        }
        shown = out;
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private int maxScroll() {
        int totalRows = (shown.size() + columns - 1) / columns;
        return Math.max(0, totalRows - rows);
    }

    private void select(CatalogEntry entry) {
        selected = entry;
        recipe = findRecipe(entry.item());
        updateButtons();
    }

    /**
     * The piece's crafting recipe as the client knows it. Since 1.21.2 clients only receive the recipes in their
     * recipe book (the mod unlocks all of its recipes on join unless that is turned off in the config).
     */
    @Nullable
    private RecipeDisplay findRecipe(Item item) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return null;
        }
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        RecipeDisplay best = null;
        for (RecipeCollection collection : minecraft.player.getRecipeBook().getCollections()) {
            for (RecipeDisplayEntry entry : collection.getRecipes()) {
                RecipeDisplay display = entry.display();
                if (!(display instanceof ShapedCraftingRecipeDisplay) && !(display instanceof ShapelessCraftingRecipeDisplay)) {
                    continue;
                }
                if (display.result().resolveForFirstStack(context).is(item)
                        && (best == null || display instanceof ShapedCraftingRecipeDisplay && !(best instanceof ShapedCraftingRecipeDisplay))) {
                    best = display;
                }
            }
        }
        return best;
    }

    private void take(boolean stack) {
        if (selected != null) {
            Identifier id = BuiltInRegistries.ITEM.getKey(selected.item());
            Services.PLATFORM.sendToServer(new CatalogTakePacket(List.of(id), stack));
        }
    }

    private void takeWholeSet() {
        if (selectedSet != null) {
            List<Identifier> ids = new ArrayList<>();
            for (Item item : selectedSet.pieces()) {
                ids.add(BuiltInRegistries.ITEM.getKey(item));
            }
            Services.PLATFORM.sendToServer(new CatalogTakePacket(ids, false));
        }
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        hovered = ItemStack.EMPTY;
        hoveredLabel = null;
        drawFrame(g);
        drawTabs(g, mouseX, mouseY);
        if (tab == Tab.SETS) {
            drawSetList(g, mouseX, mouseY);
            drawSetDetail(g, mouseX, mouseY);
        } else {
            drawSwatches(g, mouseX, mouseY);
            drawGrid(g, mouseX, mouseY);
            drawDetail(g, mouseX, mouseY);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (showcase) {
            return;
        }
        if (!hovered.isEmpty()) {
            g.setTooltipForNextFrame(font, hovered, mouseX, mouseY);
        } else if (hoveredLabel != null) {
            g.setTooltipForNextFrame(font, hoveredLabel, mouseX, mouseY);
        }
    }

    private void drawFrame(GuiGraphicsExtractor g) {
        int r = left + panelWidth;
        int b = top + panelHeight;
        g.fill(left - 2, top - 2, r + 2, b + 2, BORDER_DARK);
        g.fill(left - 1, top - 1, r + 1, b + 1, BORDER);
        g.fill(left, top, r, b, PANEL);
        for (int x = left + 3; x < r - 4; x += 8) {
            g.fill(x, top - 4, x + 5, top - 2, BORDER);
            g.fill(x + 1, top - 5, x + 4, top - 4, BORDER);
            g.fill(x, b + 2, x + 5, b + 4, BORDER);
            g.fill(x + 1, b + 4, x + 4, b + 5, BORDER);
        }
        g.fill(left, top, r, top + 19, PANEL_SHADE);
        g.fill(left, top + 19, r, top + 20, BORDER);
        Component title = Component.literal("♡ ").append(this.title).append(" ♡");
        g.text(font, title, left + (panelWidth - font.width(title)) / 2, top + 6, TEXT, false);
        g.fill(left + SIDEBAR - 1, top + 20, left + SIDEBAR, top + panelHeight, BORDER);
        int dx = left + panelWidth - DETAIL;
        g.fill(dx - 1, top + 20, dx, top + panelHeight, BORDER);
        g.fill(dx, top + 20, left + panelWidth, top + panelHeight, PANEL_SHADE);
    }

    private void drawTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = top + 24;
        for (Tab t : Tab.values()) {
            boolean hover = inside(mouseX, mouseY, left + 4, y, SIDEBAR - 9, 18);
            g.fill(left + 4, y, left + SIDEBAR - 5, y + 18, t == tab ? TAB_SELECTED : hover ? 0xFFFBD3E2 : TAB);
            if (t == tab) {
                g.fill(left + 4, y, left + 6, y + 18, BORDER_DARK);
            }
            Component label = Component.translatable(t.key);
            g.text(font, font.plainSubstrByWidth(label.getString(), SIDEBAR - 16), left + 9, y + 5, TEXT, false);
            y += 20;
        }
        int pieces = tab == Tab.SETS ? FurnitureSet.values().length : shown.size();
        String count = Component.translatable("gui.coquettefurniture.catalog.items", pieces).getString();
        if (y + 12 < top + panelHeight) {
            g.text(font, count, left + 9, top + panelHeight - 13, TEXT_SOFT, false);
        }
    }

    private void drawSwatches(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = left + SIDEBAR + 5;
        int y = top + 42;
        drawSwatch(g, x, y, 0xFFFFFFFF, colorFilter == null, true);
        if (inside(mouseX, mouseY, x, y, 12, 12)) {
            hoveredLabel = Component.translatable("color.coquettefurniture.all");
        }
        x += 14;
        for (FurnitureColor color : FurnitureColor.values()) {
            drawSwatch(g, x, y, 0xFF000000 | color.rgb(), colorFilter == color, false);
            if (inside(mouseX, mouseY, x, y, 12, 12)) {
                hoveredLabel = Component.translatable(color.translationKey());
            }
            x += 14;
        }
    }

    private void drawSwatch(GuiGraphicsExtractor g, int x, int y, int rgb, boolean active, boolean rainbow) {
        g.fill(x - 1, y - 1, x + 13, y + 13, active ? BORDER_DARK : SLOT_EDGE);
        g.fill(x, y, x + 12, y + 12, rgb);
        if (rainbow) {
            FurnitureColor[] colors = FurnitureColor.values();
            for (int i = 0; i < colors.length; i++) {
                g.fill(x + i * 2, y, x + i * 2 + 2, y + 12, 0xFF000000 | colors[i].rgb());
            }
        }
    }

    private void drawGrid(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int gx = left + SIDEBAR + 4;
        int gy = top + GRID_TOP;
        for (int i = 0; i < rows * columns; i++) {
            int index = scroll * columns + i;
            int x = gx + (i % columns) * CELL;
            int y = gy + (i / columns) * CELL;
            boolean isSelected = index < shown.size() && shown.get(index) == selected;
            g.fill(x, y, x + CELL - 1, y + CELL - 1, isSelected ? BORDER_DARK : SLOT_EDGE);
            g.fill(x + 1, y + 1, x + CELL - 2, y + CELL - 2, isSelected ? SLOT_SELECTED : SLOT);
            if (index >= shown.size()) {
                continue;
            }
            ItemStack stack = new ItemStack(shown.get(index).item());
            g.item(stack, x + 1, y + 1);
            if (inside(mouseX, mouseY, x, y, CELL - 1, CELL - 1)) {
                g.fill(x + 1, y + 1, x + CELL - 2, y + CELL - 2, 0x40FFFFFF);
                hovered = stack;
            }
        }
        int max = maxScroll();
        int barX = gx + columns * CELL + 2;
        int barHeight = rows * CELL - 1;
        g.fill(barX, gy, barX + 4, gy + barHeight, SLOT_EDGE);
        int knob = Math.max(12, barHeight / (max + 1));
        int knobY = max == 0 ? gy : gy + (barHeight - knob) * scroll / max;
        g.fill(barX, knobY, barX + 4, knobY + knob, BORDER_DARK);
    }

    private void drawDetail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int dx = left + panelWidth - DETAIL + 4;
        int width = DETAIL - 8;
        int y = top + 24;
        if (selected == null) {
            for (FormattedCharSequence line : font.split(Component.translatable("gui.coquettefurniture.catalog.select"), width)) {
                g.text(font, line, dx, y, TEXT_SOFT, false);
                y += 10;
            }
            return;
        }
        ItemStack stack = new ItemStack(selected.item());
        // the selected piece as a slowly turning 3D model, 40 pixels square
        float yaw = (Util.getMillis() % 12000L) * 360.0F / 12000.0F;
        int px = dx + width / 2 - 20;
        CoquetteClient.submitPictureInPicture(g, BlockPreviewRenderer.state(stack, yaw, px, y, px + 40, y + 40, null));
        y += 44;
        for (FormattedCharSequence line : font.split(stack.getHoverName(), width)) {
            g.text(font, line, dx + (width - font.width(line)) / 2, y, TEXT, false);
            y += 10;
        }
        Component room = Component.translatable(selected.room().translationKey());
        g.text(font, room, dx + (width - font.width(room)) / 2, y, TEXT_SOFT, false);
        y += 12;
        y = drawRecipe(g, dx, y, width, mouseX, mouseY);
        List<Component> lines = new ArrayList<>();
        for (Feature feature : selected.features()) {
            lines.add(feature.describe(selected.storageSlots()));
        }
        for (FurnitureSet set : FurnitureSet.setsContaining(selected.item())) {
            lines.add(Component.translatable("tooltip.coquettefurniture.set", Component.translatable(set.translationKey())));
        }
        int bottom = top + panelHeight - (creative() ? 28 : 4);
        for (Component line : lines) {
            for (FormattedCharSequence part : font.split(line, width)) {
                if (y + 9 > bottom) {
                    return;
                }
                g.text(font, part, dx, y, TEXT, false);
                y += 9;
            }
        }
        if (!creative() && y + 20 < bottom) {
            for (FormattedCharSequence part : font.split(Component.translatable("gui.coquettefurniture.catalog.survival"), width)) {
                g.text(font, part, dx, y + 3, TEXT_SOFT, false);
                y += 9;
            }
        }
    }

    private int drawRecipe(GuiGraphicsExtractor g, int dx, int y, int width, int mouseX, int mouseY) {
        if (recipe == null) {
            Component none = Component.translatable("gui.coquettefurniture.catalog.no_recipe");
            g.text(font, none, dx + (width - font.width(none)) / 2, y, TEXT_SOFT, false);
            return y + 12;
        }
        int gw = 3;
        int gh = 3;
        List<SlotDisplay> ingredients;
        if (recipe instanceof ShapedCraftingRecipeDisplay shaped) {
            gw = shaped.width();
            gh = shaped.height();
            ingredients = shaped.ingredients();
        } else {
            ingredients = ((ShapelessCraftingRecipeDisplay) recipe).ingredients();
        }
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        int size = 16;
        int gridX = dx + (width - 3 * size) / 2;
        long cycle = Util.getMillis() / 1000L;
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = gridX + col * size;
            int sy = y + row * size;
            g.fill(x, sy, x + size - 1, sy + size - 1, SLOT_EDGE);
            g.fill(x + 1, sy + 1, x + size - 2, sy + size - 2, SLOT);
            int index = recipe instanceof ShapedCraftingRecipeDisplay ? (col < gw && row < gh ? row * gw + col : -1) : i;
            if (index < 0 || index >= ingredients.size()) {
                continue;
            }
            List<ItemStack> options = ingredients.get(index).resolveForStacks(context);
            if (options.isEmpty()) {
                continue;
            }
            ItemStack option = options.get((int) (cycle % options.size()));
            g.pose().pushMatrix();
            g.pose().translate(x, sy);
            g.pose().scale(0.9375F, 0.9375F);
            g.item(option, 0, 0);
            g.pose().popMatrix();
            if (inside(mouseX, mouseY, x, sy, size - 1, size - 1)) {
                hovered = option;
            }
        }
        return y + 3 * size + 5;
    }

    private void drawSetList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = left + SIDEBAR + 4;
        int y = top + 24;
        int w = panelWidth - SIDEBAR - DETAIL - 8;
        for (FurnitureSet set : FurnitureSet.values()) {
            boolean active = set == selectedSet;
            boolean hover = inside(mouseX, mouseY, x, y, w, 36);
            g.fill(x, y, x + w, y + 36, active ? BORDER_DARK : SLOT_EDGE);
            g.fill(x + 1, y + 1, x + w - 1, y + 35, active ? SLOT_SELECTED : hover ? 0xFFFFF0F6 : SLOT);
            g.fill(x + 1, y + 1, x + 4, y + 35, 0xFF000000 | set.color());
            List<Item> pieces = set.pieces();
            g.item(new ItemStack(pieces.get(0)), x + 8, y + 10);
            g.text(font, Component.translatable(set.translationKey()), x + 30, y + 7, TEXT, false);
            g.text(font, Component.translatable("gui.coquettefurniture.catalog.pieces", pieces.size()), x + 30, y + 20,
                    TEXT_SOFT, false);
            y += 39;
        }
    }

    private void drawSetDetail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int dx = left + panelWidth - DETAIL + 4;
        int width = DETAIL - 8;
        int y = top + 24;
        if (selectedSet == null) {
            for (FormattedCharSequence line : font.split(Component.translatable("gui.coquettefurniture.catalog.select"), width)) {
                g.text(font, line, dx, y, TEXT_SOFT, false);
                y += 10;
            }
            return;
        }
        Component name = Component.translatable(selectedSet.translationKey());
        g.text(font, name, dx + (width - font.width(name)) / 2, y, TEXT, false);
        y += 12;
        for (FormattedCharSequence line : font.split(Component.translatable(selectedSet.translationKey() + ".desc"), width)) {
            g.text(font, line, dx, y, TEXT_SOFT, false);
            y += 9;
        }
        y += 4;
        int perRow = width / CELL;
        List<Item> pieces = selectedSet.pieces();
        for (int i = 0; i < pieces.size(); i++) {
            int x = dx + (i % perRow) * CELL;
            int sy = y + (i / perRow) * CELL;
            if (sy + CELL > top + panelHeight - 26) {
                break;
            }
            g.fill(x, sy, x + CELL - 1, sy + CELL - 1, SLOT_EDGE);
            g.fill(x + 1, sy + 1, x + CELL - 2, sy + CELL - 2, SLOT);
            ItemStack stack = new ItemStack(pieces.get(i));
            g.item(stack, x + 1, sy + 1);
            if (inside(mouseX, mouseY, x, sy, CELL - 1, CELL - 1)) {
                hovered = stack;
            }
        }
    }

    /** Opens a tab and selects a piece, as if clicked (used by the dev showcase). */
    public void previewSelect(int tabIndex, Item item) {
        showcase = true;
        tab = Tab.values()[tabIndex];
        refilter();
        for (CatalogEntry entry : shown) {
            if (entry.item() == item) {
                select(entry);
            }
        }
        updateButtons();
    }

    /** Opens the sets tab with a set selected (used by the dev showcase). */
    public void previewSet(int setIndex) {
        showcase = true;
        tab = Tab.SETS;
        selectedSet = FurnitureSet.values()[setIndex];
        updateButtons();
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int y = top + 24;
        for (Tab t : Tab.values()) {
            if (inside(mouseX, mouseY, left + 4, y, SIDEBAR - 9, 18)) {
                tab = t;
                scroll = 0;
                refilter();
                updateButtons();
                return true;
            }
            y += 20;
        }
        if (tab == Tab.SETS) {
            int x = left + SIDEBAR + 4;
            int sy = top + 24;
            int w = panelWidth - SIDEBAR - DETAIL - 8;
            for (FurnitureSet set : FurnitureSet.values()) {
                if (inside(mouseX, mouseY, x, sy, w, 36)) {
                    selectedSet = set;
                    updateButtons();
                    return true;
                }
                sy += 39;
            }
            return false;
        }
        int sx = left + SIDEBAR + 5;
        int sy = top + 42;
        if (inside(mouseX, mouseY, sx, sy, 12, 12)) {
            colorFilter = null;
            refilter();
            return true;
        }
        for (FurnitureColor color : FurnitureColor.values()) {
            sx += 14;
            if (inside(mouseX, mouseY, sx, sy, 12, 12)) {
                colorFilter = colorFilter == color ? null : color;
                refilter();
                return true;
            }
        }
        int gx = left + SIDEBAR + 4;
        int gy = top + GRID_TOP;
        if (inside(mouseX, mouseY, gx, gy, columns * CELL, rows * CELL)) {
            int col = (int) (mouseX - gx) / CELL;
            int row = (int) (mouseY - gy) / CELL;
            int index = (scroll + row) * columns + col;
            if (index < shown.size()) {
                select(shown.get(index));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab != Tab.SETS) {
            scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (search.isFocused() && !event.isEscape()) {
            return search.keyPressed(event) || search.canConsumeInput();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
