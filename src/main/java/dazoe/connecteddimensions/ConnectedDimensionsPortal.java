package dazoe.connecteddimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ConnectedDimensionsPortal(boolean goingDown) implements Portal {

	@Override
	public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, final Entity entity, final BlockPos srcPos) {
		var currentDimension = currentLevel.dimension();
		ResourceKey<Level> dstDimension = null;
		if (goingDown) {
			if (currentDimension == Level.OVERWORLD) {
				dstDimension = Level.NETHER;
			} else if (currentDimension == Level.END) {
				dstDimension = Level.OVERWORLD;
			}
		} else {
			if (currentDimension == Level.OVERWORLD) {
				dstDimension = Level.END;
			} else if (currentDimension == Level.NETHER) {
				dstDimension = Level.OVERWORLD;
			}
		}
		if (dstDimension == null) {
			ConnectedDimensions.LOGGER.info("Don't this this should have happened.");
			return null;
		}

		var dstLevel = currentLevel.getServer().getLevel(dstDimension);
		assert dstLevel != null;
		var scale = DimensionType.getTeleportationScale(currentLevel.dimensionType(), dstLevel.dimensionType());
		var border = dstLevel.getWorldBorder();
		var dstY = goingDown ? dstLevel.getMaxY() + 1 : dstLevel.getMinY();
		var entPos = entity.position();
		var dstVec3d = new Vec3(entPos.x * scale, dstY, entPos.z * scale);

		TeleportTransition tgt = new TeleportTransition(
				dstLevel, dstVec3d, Vec3.ZERO, 0.0F, 0.0F,
				Relative.union(Relative.DELTA, Relative.ROTATION),
				TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));

		// Player entities can teleport regardless
		if (entity instanceof Player) {
			// todo: find a safe place to send players?
			// idea: make it so they can stand on maxY of srcWorld in dstWorld? ie: so then can build up from
			// the nether and dig into the overworld, or build up in overworld and throw a portal in the end.
			entity.setPortalCooldown(0);
			return tgt;
		}

		// non-living entities can only teleport if chunks are loaded.
		if (dstLevel.isLoaded(border.clampToBounds(dstVec3d))) {
			return tgt;
		}

		return null;
	}
}
