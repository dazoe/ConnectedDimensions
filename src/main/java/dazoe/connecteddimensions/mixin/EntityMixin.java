package dazoe.connecteddimensions.mixin;

import dazoe.connecteddimensions.ConnectedDimensions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
	private void onMove(MoverType type, Vec3 movement, CallbackInfo cbi) {
//		ConnectedDimensions.entityCheckCount++;
		var entity = (Entity)(Object)this;
		if (!entity.canUsePortal(true)) {
//			ConnectedDimensions.LOGGER.info("Skipped: "+entity.getClass().toString());
//			ConnectedDimensions.entitySkippedCount++;
			return;
		}
		var level = entity.level();
		var dimension = level.dimension();
		var minY = level.getMinY()-1;
		var maxY = level.getMinY()+level.getHeight()+1;
		//TODO: try entity getheight and round up?
		var posY = entity.getY();
		if (dimension != Level.NETHER && posY < minY) {
			entity.setAsInsidePortal(ConnectedDimensions.portalDown, entity.blockPosition());
//			ConnectedDimensions.entityTPCount++;
//			ServerPlayerEntity spe = null;
//			if ((Object)this instanceof ServerPlayerEntity) {
//				spe = (ServerPlayerEntity) (Object) this;
//				spe.sendMessage(Text.literal("Should teleport down"));
//			}
		} else if (dimension != Level.END && posY > maxY) {
			entity.setAsInsidePortal(ConnectedDimensions.portalUp, entity.blockPosition());
//			ConnectedDimensions.entityTPCount++;
//			ServerPlayerEntity spe = null;
//			if ((Object)this instanceof ServerPlayerEntity) {
//				spe = (ServerPlayerEntity) (Object) this;
//				spe.sendMessage(Text.literal("Should teleport up"));
//			}
		}
	}
}
