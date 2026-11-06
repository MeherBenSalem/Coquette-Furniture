package com.sugarplumstudios.coquette.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.sugarplumstudios.coquette.block.ShapeUtil.px;

/** Every piece of furniture that comes in all six colourways. */
public enum FurnitureType {
    // ------------------------------------------------------------------ bedroom
    CANOPY_BED(Room.BEDROOM, 0, c -> new CanopyBedBlock(c, fabric(c).strength(0.8F)), Feature.BED, Feature.SURFACE),
    DECORATIVE_PILLOW(Room.BEDROOM, 0, c -> new PillowBlock(fabric(c).strength(0.3F),
            px(1.5, 0, 1.5, 14.5, 4, 14.5)), Feature.SEAT, Feature.STACK, Feature.DECOR),
    BEDSIDE_TABLE(Room.BEDROOM, 9, c -> new StorageBlock(wood(c), px(1, 0, 1.5, 15, 14, 15.5), false, 1, 9,
            "container.coquettefurniture.drawers", SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundEvents.CHISELED_BOOKSHELF_INSERT),
            Feature.STORAGE, Feature.SURFACE),
    WARDROBE(Room.BEDROOM, 54, c -> new TallStorageBlock(wood(c).strength(2.5F), px(1, 0, 1.5, 15, 16, 15),
            px(1, 0, 1.25, 15, 16, 15.5), 54, "container.coquettefurniture.wardrobe",
            SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_DOOR_CLOSE), Feature.STORAGE),
    DRESSER(Room.BEDROOM, 27, c -> new StorageBlock(wood(c).strength(2.0F), px(0.5, 0, 1, 15.5, 16, 15.5), false, 0, 27,
            "container.coquettefurniture.drawers", SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundEvents.CHISELED_BOOKSHELF_INSERT),
            Feature.STORAGE, Feature.SURFACE),
    VANITY_TABLE(Room.BEDROOM, 9, c -> new VanityBlock(wood(c), px(0.5, 0, 2.5, 15.5, 12, 15.5, 3, 12, 12.75, 13, 16, 15.25), 2),
            Feature.PAMPER, Feature.STORAGE, Feature.SURFACE),
    STANDING_MIRROR(Room.BEDROOM, 0, c -> new StandingMirrorBlock(wood(c), px(1, 0, 5.5, 15, 16, 10.5),
            px(1.5, 0, 6.5, 14.5, 13, 9.5)), Feature.PAMPER),
    JEWELRY_BOX(Room.BEDROOM, 5, c -> new JewelryBoxBlock(decor(c, SoundType.WOOD), px(3.75, 0, 4.75, 12.25, 6, 11.25)),
            Feature.STORAGE, Feature.MUSIC, Feature.DECOR),
    NIGHT_LAMP(Room.BEDROOM, 0, c -> new LampBlock(decor(c, SoundType.WOOL).lightLevel(s -> s.getValue(LampBlock.LIT) ? 14 : 0),
            px(5.5, 0, 5.5, 10.5, 7, 10.5, 4, 7, 4, 12, 12.25, 12)), Feature.LIGHT, Feature.TOGGLE, Feature.DECOR),
    // ------------------------------------------------------------------ living room
    SOFA(Room.LIVING_ROOM, 0, c -> new ConnectedSeatBlock(fabric(c).strength(1.0F),
            px(0, 0, 1.5, 16, 7.5, 15.5, 0, 0, 12.5, 16, 16, 15.75), 7.5, 7.5, 4), Feature.SEAT, Feature.CONNECTS),
    ARMCHAIR(Room.LIVING_ROOM, 0, c -> new ChairBlock(fabric(c).strength(1.0F), px(1, 0, 1.25, 15, 8, 15,
            1, 0, 12.5, 15, 16, 15, 0.5, 0, 1.75, 3.25, 12, 15, 12.75, 0, 1.75, 15.5, 12, 15), 8, 7.5, 4), Feature.SEAT),
    COFFEE_TABLE(Room.LIVING_ROOM, 0, c -> new FurnitureBlock(wood(c), px(1, 0, 2, 15, 8, 14), false, 4), Feature.SURFACE),
    BOOKSHELF(Room.LIVING_ROOM, 0, c -> new BookshelfBlock(wood(c), px(0, 0, 0.5, 16, 16, 16)),
            Feature.ENCHANTING, Feature.SURFACE),
    DISPLAY_CABINET(Room.LIVING_ROOM, 0, c -> new DisplayCabinetBlock(wood(c).sound(SoundType.WOOD),
            px(0.5, 0, 1.5, 15.5, 16, 15.75)), Feature.DISPLAY, Feature.SURFACE),
    RUG(Room.LIVING_ROOM, 0, c -> new RugBlock(BlockBehaviour.Properties.of().mapColor(c.mapColor()).strength(0.1F)
            .sound(SoundType.WOOL).ignitedByLava().noOcclusion(), 1), Feature.CONNECTS),
    CURTAINS(Room.LIVING_ROOM, 0, c -> new CurtainBlock(fabric(c).strength(0.5F).noCollission(), px(0, 0, 12, 16, 16, 15.5),
            px(0, 0, 11.5, 16, 16, 16)), Feature.TOGGLE),
    // ------------------------------------------------------------------ kitchen
    KITCHEN_CABINET(Room.KITCHEN, 18, c -> new StorageBlock(wood(c).strength(2.0F), px(0, 0, 0, 16, 16, 16), false, 0, 18,
            "container.coquettefurniture.cabinet", SoundEvents.BARREL_OPEN, SoundEvents.BARREL_CLOSE),
            Feature.STORAGE, Feature.SURFACE),
    REFRIGERATOR(Room.KITCHEN, 36, c -> new TallStorageBlock(metal(c), px(1, 0, 1.5, 15, 16, 15), px(1, 0, 1.5, 15, 15.75, 15),
            36, "container.coquettefurniture.refrigerator", SoundEvents.IRON_DOOR_OPEN, SoundEvents.IRON_DOOR_CLOSE),
            Feature.STORAGE),
    KITCHEN_COUNTER(Room.KITCHEN, 18, c -> new StorageBlock(wood(c).strength(2.0F), px(0, 0, 0, 16, 16, 16), false, 0, 18,
            "container.coquettefurniture.drawers", SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundEvents.CHISELED_BOOKSHELF_INSERT),
            Feature.STORAGE, Feature.SURFACE),
    KITCHEN_SINK(Room.KITCHEN, 0, c -> new SinkBlock(wood(c).strength(2.0F), px(0, 0, 0, 16, 16, 16), DecorSurface.NOT_A_SURFACE,
            8, 12.75, 7.25), Feature.WATER),
    STOVE(Room.KITCHEN, 0, c -> new StoveBlock(metal(c).lightLevel(s -> s.getValue(StoveBlock.LIT) ? 13 : 0)), Feature.COOKING),
    DINING_TABLE(Room.KITCHEN, 0, c -> new TableBlock(wood(c)), Feature.CONNECTS, Feature.SURFACE),
    DINING_CHAIR(Room.KITCHEN, 0, c -> new ChairBlock(wood(c), px(2, 0, 2, 14, 10.5, 14, 2.5, 10.5, 12, 13.5, 16, 13.75),
            10.5, 7.5, 3), Feature.SEAT),
    TEA_SET(Room.KITCHEN, 0, c -> new TeaSetBlock(decor(c, SoundType.GLASS), px(1, 0, 3, 15, 6.5, 13)), Feature.TEA, Feature.DECOR),
    // ------------------------------------------------------------------ bathroom
    BATHTUB(Room.BATHROOM, 0, c -> new BathtubBlock(ceramic(c).strength(2.0F)), Feature.BATH, Feature.SEAT),
    BATHROOM_SINK(Room.BATHROOM, 0, c -> new SinkBlock(ceramic(c), px(5, 0, 5.5, 11, 10, 11.5, 1.75, 9.5, 2.75, 14.25, 14, 14.25,
            2, 13.5, 14, 14, 16, 15.5), DecorSurface.NOT_A_SURFACE, 8, 11.75, 8.5), Feature.WATER),
    WALL_MIRROR(Room.BATHROOM, 0, c -> new MirrorBlock(wood(c).strength(0.8F), px(2.5, 1, 14.5, 13.5, 15, 16)), Feature.PAMPER),
    BATHROOM_CABINET(Room.BATHROOM, 9, c -> new StorageBlock(wood(c), px(1.5, 1, 10.5, 14.5, 15.5, 16), true, DecorSurface.NOT_A_SURFACE, 9,
            "container.coquettefurniture.cabinet", SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundEvents.CHISELED_BOOKSHELF_INSERT),
            Feature.STORAGE),
    TOWEL_RACK(Room.BATHROOM, 0, c -> new FurnitureBlock(fabric(c).strength(0.5F), px(1.5, 1.5, 11.5, 14.5, 12.5, 16), true,
            DecorSurface.NOT_A_SURFACE)),
    SOAP_DISPENSER(Room.BATHROOM, 0, c -> new SoapDispenserBlock(decor(c, SoundType.GLASS),
            px(6, 0, 4.75, 10, 8.5, 10, 10.25, 0, 9.75, 13.5, 1.25, 12.25)), Feature.DECOR),
    // ------------------------------------------------------------------ garden
    GARDEN_BENCH(Room.GARDEN, 0, c -> new ConnectedSeatBlock(wood(c), px(0, 0, 2.75, 16, 7.5, 12.25, 0, 0, 12.5, 16, 16, 14.5),
            7.5, 7, 4), Feature.SEAT, Feature.CONNECTS),
    FLOWER_POT(Room.GARDEN, 0, c -> new DecorBlock(decor(c, SoundType.DECORATED_POT), px(4, 0, 4, 12, 6, 12, 3.5, 6, 3.5, 12.5, 13, 12.5)),
            Feature.DECOR),
    BIRDHOUSE(Room.GARDEN, 0, c -> new BirdhouseBlock(wood(c), px(6, 0, 6, 10, 7, 10, 3.5, 6.75, 4, 12.5, 16, 12)), Feature.BIRDS),
    PICKET_FENCE(Room.GARDEN, 0, c -> new PicketFenceBlock(c, BlockBehaviour.Properties.of().mapColor(c.mapColor()).forceSolidOn()
            .instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava()), Feature.CONNECTS),
    OUTDOOR_TABLE(Room.GARDEN, 0, c -> new FurnitureBlock(wood(c), px(1, 13, 1, 15, 14, 15, 6.5, 0, 6.5, 9.5, 13, 9.5), false, 1),
            Feature.SURFACE),
    PICNIC_BLANKET(Room.GARDEN, 0, c -> new RugBlock(BlockBehaviour.Properties.of().mapColor(c.mapColor()).strength(0.1F)
            .sound(SoundType.WOOL).ignitedByLava().noOcclusion(), 0.75), Feature.CONNECTS),
    SWING_SEAT(Room.GARDEN, 0, c -> new SwingBlock(wood(c), px(1, 5.5, 4, 15, 9.5, 12.5, 1, 8.5, 11.5, 15, 15, 13),
            px(1.25, 0, 4.5, 2.5, 16, 11.75, 13.5, 0, 4.5, 14.75, 16, 11.75)), Feature.SEAT);

    private final String id;
    private final Room room;
    private final int storageSlots;
    private final Function<FurnitureColor, Block> factory;
    private final List<Feature> features;

    FurnitureType(Room room, int storageSlots, Function<FurnitureColor, Block> factory, Feature... features) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.room = room;
        this.storageSlots = storageSlots;
        this.factory = factory;
        this.features = List.of(features);
    }

    public String id() {
        return id;
    }

    public String blockId(FurnitureColor color) {
        return color.id() + "_" + id;
    }

    public Room room() {
        return room;
    }

    public int storageSlots() {
        return storageSlots;
    }

    public List<Feature> features() {
        return features;
    }

    public Block create(FurnitureColor color) {
        return factory.apply(color);
    }

    // ------------------------------------------------------------------ block properties

    static BlockBehaviour.Properties wood(FurnitureColor color) {
        return BlockBehaviour.Properties.of().mapColor(color.mapColor()).sound(SoundType.WOOD).strength(1.5F)
                .instrument(NoteBlockInstrument.BASS).ignitedByLava().noOcclusion();
    }

    static BlockBehaviour.Properties fabric(FurnitureColor color) {
        return BlockBehaviour.Properties.of().mapColor(color.mapColor()).sound(SoundType.WOOL).strength(0.8F)
                .ignitedByLava().noOcclusion();
    }

    static BlockBehaviour.Properties ceramic(FurnitureColor color) {
        return BlockBehaviour.Properties.of().mapColor(color.mapColor()).sound(SoundType.STONE).strength(1.5F)
                .instrument(NoteBlockInstrument.BASEDRUM).noOcclusion();
    }

    static BlockBehaviour.Properties metal(FurnitureColor color) {
        return BlockBehaviour.Properties.of().mapColor(color.mapColor()).sound(SoundType.METAL).strength(2.5F).noOcclusion();
    }

    static BlockBehaviour.Properties decor(FurnitureColor color, SoundType sound) {
        return BlockBehaviour.Properties.of().mapColor(color.mapColor()).sound(sound).strength(0.3F).noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }
}
