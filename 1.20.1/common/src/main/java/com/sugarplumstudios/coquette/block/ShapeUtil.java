package com.sugarplumstudios.coquette.block;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Helpers for blocks whose models face north by default and are rotated by
 * blockstate {@code y} rotations (east = 90, south = 180, west = 270).
 */
public final class ShapeUtil {
    private ShapeUtil() {
    }

    /** Builds a shape from pixel boxes {x0, y0, z0, x1, y1, z1, ...}. */
    public static VoxelShape px(double... boxes) {
        VoxelShape shape = Shapes.empty();
        for (int i = 0; i + 5 < boxes.length; i += 6) {
            shape = Shapes.or(shape, Shapes.box(boxes[i] / 16.0D, boxes[i + 1] / 16.0D, boxes[i + 2] / 16.0D,
                    boxes[i + 3] / 16.0D, boxes[i + 4] / 16.0D, boxes[i + 5] / 16.0D));
        }
        return shape;
    }

    public static Map<Direction, VoxelShape> horizontal(VoxelShape north) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            shapes.put(direction, rotate(north, direction));
        }
        return shapes;
    }

    public static VoxelShape rotate(VoxelShape north, Direction facing) {
        VoxelShape[] result = {Shapes.empty()};
        north.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            double[] a = toWorld(facing, x1, z1);
            double[] b = toWorld(facing, x2, z2);
            result[0] = Shapes.or(result[0], Shapes.box(Math.min(a[0], b[0]), y1, Math.min(a[1], b[1]),
                    Math.max(a[0], b[0]), y2, Math.max(a[1], b[1])));
        });
        return result[0].optimize();
    }

    /** Maps a north-frame block-local (x, z) to world block-local coordinates. */
    public static double[] toWorld(Direction facing, double x, double z) {
        return switch (facing) {
            case EAST -> new double[]{1.0D - z, x};
            case SOUTH -> new double[]{1.0D - x, 1.0D - z};
            case WEST -> new double[]{z, 1.0D - x};
            default -> new double[]{x, z};
        };
    }

    /** Maps a world block-local (x, z) back into the north frame. */
    public static double[] toNorthFrame(Direction facing, double x, double z) {
        return switch (facing) {
            case EAST -> new double[]{z, 1.0D - x};
            case SOUTH -> new double[]{1.0D - x, 1.0D - z};
            case WEST -> new double[]{1.0D - z, x};
            default -> new double[]{x, z};
        };
    }

    /** Clockwise rotation (degrees, seen from above) matching blockstate rotations. */
    public static float yRotation(Direction facing) {
        return ((facing.get2DDataValue() + 2) % 4) * 90.0F;
    }
}
