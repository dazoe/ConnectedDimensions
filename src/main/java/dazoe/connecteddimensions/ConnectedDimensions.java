package dazoe.connecteddimensions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.*;

public class ConnectedDimensions implements ModInitializer {
	public static final String MOD_ID = "connecteddimensions";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ConnectedDimensionsPortal portalDown = new ConnectedDimensionsPortal(true);
	public static ConnectedDimensionsPortal portalUp = new ConnectedDimensionsPortal(false);
//	public static int entityCheckCount;
//	public static int entitySkippedCount;
//	public static int entityTPCount;

	@Override
	public void onInitialize() {
		LOGGER.info("ConnectedDimensions Initialized!");
		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> {
					dispatcher.register(
							literal("cd")
									.requires(source -> source.hasPermissionLevel(3))
									.executes((ctx) -> {
										var src = ctx.getSource();

										src.sendFeedback(() ->
														Text.literal(String.format("You are %s", src.getEntity().getClass().toString())),
												false
										);
										return 1;
									})
					);
				}
		);
//		ServerTickEvents.START_SERVER_TICK.register(server -> {
//			entityCheckCount = 0;
//			entitySkippedCount = 0;
//			entityTPCount = 0;
//		});
//		ServerTickEvents.END_SERVER_TICK.register(server -> {
//			LOGGER.info("Num checks: "+entityCheckCount+"/"+entitySkippedCount+" TP: "+entityTPCount);
//		});
	}
}
