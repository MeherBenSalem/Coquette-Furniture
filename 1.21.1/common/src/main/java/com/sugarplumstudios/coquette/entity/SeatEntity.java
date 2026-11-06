package com.sugarplumstudios.coquette.entity;

import com.sugarplumstudios.coquette.block.SeatProvider;
import com.sugarplumstudios.coquette.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** The invisible entity a sitting player rides. It lives only while someone sits on it. */
public class SeatEntity extends Entity {
    /**
     * Thighs are 4px thick, so the hips sit 2px above the seat: a sitting player's feet go 0.625 below the seat point.
     * A player's vehicle attachment is 0.6 above their feet, so the passenger attachment is 0.025 below this entity.
     */
    private static final Vec3 PASSENGER_ATTACHMENT = new Vec3(0.0D, -0.025D, 0.0D);

    private BlockPos seatBlock = BlockPos.ZERO;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos seatBlock, Vec3 at, float yaw) {
        this(ModEntities.SEAT.get(), level);
        this.seatBlock = seatBlock.immutable();
        setPos(at.x, at.y, at.z);
        setYRot(yaw);
        this.yRotO = yaw;
    }

    public BlockPos seatBlock() {
        return seatBlock;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        seatBlock = NbtUtils.readBlockPos(tag, "Seat").orElse(BlockPos.ZERO);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("Seat", NbtUtils.writeBlockPos(seatBlock));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        BlockState state = level().getBlockState(seatBlock);
        Entity passenger = getFirstPassenger();
        if (passenger == null || !(state.getBlock() instanceof SeatProvider seat) || seat.seatPosition(state, seatBlock) == null) {
            ejectPassengers();
            discard();
            return;
        }
        seat.onSeatTick(level(), seatBlock, state, passenger, tickCount);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return PASSENGER_ATTACHMENT;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        super.positionRider(passenger, move);
        clampRotation(passenger);
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        clampRotation(passenger);
    }

    private void clampRotation(Entity passenger) {
        passenger.setYBodyRot(getYRot());
        float delta = Mth.wrapDegrees(passenger.getYRot() - getYRot());
        float clamped = Mth.clamp(delta, -100.0F, 100.0F);
        passenger.yRotO += clamped - delta;
        passenger.setYRot(passenger.getYRot() + clamped - delta);
        passenger.setYHeadRot(passenger.getYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Direction facing = Direction.fromYRot(getYRot());
        BlockPos[] candidates = {seatBlock.relative(facing), seatBlock.relative(facing.getClockWise()),
                seatBlock.relative(facing.getCounterClockWise()), seatBlock.relative(facing.getOpposite()), seatBlock.above()};
        for (BlockPos candidate : candidates) {
            Vec3 spot = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), candidate, true);
            if (spot != null) {
                return spot;
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
