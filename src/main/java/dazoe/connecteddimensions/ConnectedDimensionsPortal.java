package dazoe.connecteddimensions;

import net.minecraft.block.Portal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.World;

public record ConnectedDimensionsPortal(boolean goingDown) implements Portal {

	@Override
	public @Nullable TeleportTarget createTeleportTarget(ServerWorld srcWorld, Entity entity, BlockPos srcPos) {
		var srcWorldKey = srcWorld.getRegistryKey();
		RegistryKey<World> dstWorldKey = null;
		if (goingDown) {
			if (srcWorldKey == World.OVERWORLD) {
				dstWorldKey = World.NETHER;
			} else if (srcWorldKey == World.END) {
				dstWorldKey = World.OVERWORLD;
			}
		} else {
			if (srcWorldKey == World.OVERWORLD) {
				dstWorldKey = World.END;
			} else if (srcWorldKey == World.NETHER) {
				dstWorldKey = World.OVERWORLD;
			}
		}
		if (dstWorldKey == null) {
			ConnectedDimensions.LOGGER.info("Don't this this should have happened.");
			return null;
		}

		var dstWorld = srcWorld.getServer().getWorld(dstWorldKey);
		assert dstWorld != null;
		var scale = DimensionType.getCoordinateScaleFactor(srcWorld.getDimension(), dstWorld.getDimension());
		var border = dstWorld.getWorldBorder();
		var dstY = goingDown ? dstWorld.getTopYInclusive() + 1 : dstWorld.getBottomY();
		var entPos = entity.getEntityPos();
		var dstVec3d = new Vec3d(entPos.x * scale, dstY, entPos.z * scale);

		TeleportTarget tgt = new TeleportTarget(
				dstWorld, dstVec3d, Vec3d.ZERO, 0.0F, 0.0F,
				PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT), TeleportTarget.ADD_PORTAL_CHUNK_TICKET);

		// Player entities can teleport regardless
		if (entity instanceof PlayerEntity) {
			// todo: find a safe place to send players?
			// idea: make it so they can stand on maxY of srcWorld in dstWorld? ie: so then can build up from
			// the nether and dig into the overworld, or build up in overworld and throw a portal in the end.
			entity.setPortalCooldown(0);
			return tgt;
		}

		// non-living entities can only teleport if chunks are loaded.
		var dstPos = border.clampFloored(dstVec3d.x, dstY, dstVec3d.z);
		if (dstWorld.isChunkLoaded(dstPos.getX() >> 4, dstPos.getZ() >> 4)) {
			return tgt;
		}

		return null;
	}
}
