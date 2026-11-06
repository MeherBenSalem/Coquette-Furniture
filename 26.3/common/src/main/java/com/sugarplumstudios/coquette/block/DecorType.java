package com.sugarplumstudios.coquette.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.sugarplumstudios.coquette.block.ShapeUtil.px;

/** Potted plants and the small decorations (one design each). */
public enum DecorType {
    POTTED_MONSTERA(Room.LIVING_ROOM, () -> new FurnitureBlock(plant(MapColor.PLANT), px(3.5, 0, 3.5, 12.5, 16, 12.5))),
    POTTED_ROSE_BUSH(Room.LIVING_ROOM, () -> new FurnitureBlock(plant(MapColor.COLOR_PINK), px(3, 0, 3, 13, 16, 13))),
    POTTED_LAVENDER(Room.LIVING_ROOM, () -> new FurnitureBlock(plant(MapColor.COLOR_PURPLE), px(3.5, 0, 3.5, 12.5, 16, 12.5))),
    POTTED_CHERRY_BLOSSOM(Room.LIVING_ROOM, () -> new FurnitureBlock(plant(MapColor.COLOR_PINK), px(3.5, 0, 3.5, 12.5, 16, 12.5))),

    BOOK_STACK(Room.DECOR, () -> new BookStackBlock(small(MapColor.COLOR_PINK, SoundType.WOOD), px(3.75, 0, 5, 12.25, 4, 11)),
            Feature.STACK, Feature.DECOR),
    ROSE_VASE(Room.DECOR, () -> new DecorBlock(small(MapColor.SNOW, SoundType.DECORATED_POT), px(5.5, 0, 5.5, 10.5, 13, 10.5)),
            Feature.DECOR),
    TULIP_VASE(Room.DECOR, () -> new DecorBlock(small(MapColor.COLOR_LIGHT_BLUE, SoundType.GLASS), px(5.5, 0, 5.5, 10.5, 12, 10.5)),
            Feature.DECOR),
    PASTEL_CANDLES(Room.DECOR, () -> new CandleDecorBlock(small(MapColor.COLOR_PINK, SoundType.CANDLE)
            .lightLevel(s -> s.getValue(LampBlock.LIT) ? 12 : 0), px(3.5, 0, 3.5, 12.5, 8, 12.5)), Feature.CANDLES, Feature.LIGHT,
            Feature.DECOR),
    PERFUME_BOTTLES(Room.DECOR, () -> new DecorBlock(small(MapColor.COLOR_PINK, SoundType.GLASS), px(3, 0, 4.5, 13, 7, 11.5)),
            Feature.DECOR),
    HEART_MUSIC_BOX(Room.DECOR, () -> new MusicBoxBlock(small(MapColor.COLOR_PINK, SoundType.WOOD), px(3, 0, 4, 13.5, 7.5, 13)),
            Feature.MUSIC, Feature.DECOR),
    TEDDY_BEAR(Room.DECOR, () -> new DecorBlock(small(MapColor.COLOR_PINK, SoundType.WOOL), px(3.75, 0, 3.5, 12.25, 11.5, 11)),
            Feature.DECOR),
    BUNNY_PLUSH(Room.DECOR, () -> new DecorBlock(small(MapColor.SNOW, SoundType.WOOL), px(4, 0, 4, 12, 15, 12.75)), Feature.DECOR),
    CUPCAKE_STAND(Room.DECOR, () -> new DecorBlock(small(MapColor.SNOW, SoundType.GLASS), px(2.5, 0, 2.5, 13.5, 11, 13.5)),
            Feature.DECOR),
    HEART_PHOTO_FRAME(Room.DECOR, () -> new DecorBlock(small(MapColor.GOLD, SoundType.WOOD), px(3, 0, 6.5, 13, 10, 9.5)),
            Feature.DECOR),
    RIBBON_GIFT_BOX(Room.DECOR, () -> new DecorBlock(small(MapColor.COLOR_LIGHT_BLUE, SoundType.WOOL),
            px(3.65, 0, 3.65, 12.35, 8.5, 12.35)), Feature.DECOR),
    STRAWBERRY_BASKET(Room.DECOR, () -> new DecorBlock(small(MapColor.COLOR_BROWN, SoundType.BAMBOO_WOOD), px(4, 0, 5, 12, 8.75, 11)),
            Feature.DECOR),
    ANGEL_FIGURINE(Room.DECOR, () -> new DecorBlock(small(MapColor.SNOW, SoundType.DECORATED_POT), px(3, 0, 5.5, 13, 10.5, 10.5)),
            Feature.DECOR);

    private final String id;
    private final Room room;
    private final Supplier<Block> factory;
    private final List<Feature> features;

    DecorType(Room room, Supplier<Block> factory, Feature... features) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.room = room;
        this.factory = factory;
        this.features = List.of(features);
    }

    public String id() {
        return id;
    }

    public Room room() {
        return room;
    }

    public List<Feature> features() {
        return features;
    }

    public Block create() {
        return BlockIds.create(id, factory);
    }

    private static BlockBehaviour.Properties plant(MapColor color) {
        return BlockIds.props().mapColor(color).sound(SoundType.DECORATED_POT).strength(0.6F).noOcclusion();
    }

    private static BlockBehaviour.Properties small(MapColor color, SoundType sound) {
        return BlockIds.props().mapColor(color).sound(sound).strength(0.3F).noOcclusion()
                .pushReaction(PushReaction.POPPED);
    }
}
