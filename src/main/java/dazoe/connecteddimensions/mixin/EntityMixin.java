package dazoe.connecteddimensions.mixin;

import dazoe.connecteddimensions.ConnectedDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Unique
	private int spamTicks;

	@Inject(method = "move", at = @At("HEAD"))
	private void onMove(MovementType type, Vec3d movement, CallbackInfo cbi) {
//		ConnectedDimensions.entityCheckCount++;
		var entity = (Entity)(Object)this;
		if (!entity.canUsePortals(true)) {
//			ConnectedDimensions.LOGGER.info("Skipped: "+entity.getClass().toString());
//			ConnectedDimensions.entitySkippedCount++;
			return;
		}
		var world = entity.getWorld();
		var worldKey = world.getRegistryKey();
		var minY = world.getBottomY()-1;
		var maxY = world.getBottomY()+world.getHeight()+1;
		//TODO: try entity getheight and round up?
		var posY = entity.getY();
		if (worldKey != World.NETHER && posY < minY) {
			entity.tryUsePortal(ConnectedDimensions.portalDown, entity.getBlockPos());
//			ConnectedDimensions.entityTPCount++;
//			ServerPlayerEntity spe = null;
//			if ((Object)this instanceof ServerPlayerEntity) {
//				spe = (ServerPlayerEntity) (Object) this;
//				spe.sendMessage(Text.literal("Should teleport down"));
//			}
		} else if (worldKey != World.END && posY > maxY) {
			entity.tryUsePortal(ConnectedDimensions.portalUp, entity.getBlockPos());
//			ConnectedDimensions.entityTPCount++;
//			ServerPlayerEntity spe = null;
//			if ((Object)this instanceof ServerPlayerEntity) {
//				spe = (ServerPlayerEntity) (Object) this;
//				spe.sendMessage(Text.literal("Should teleport up"));
//			}
		}
	}
}
