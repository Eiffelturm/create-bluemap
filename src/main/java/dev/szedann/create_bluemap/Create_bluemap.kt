package dev.szedann.create_bluemap

import com.mojang.logging.LogUtils
import de.bluecolored.bluemap.api.BlueMapAPI
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import org.slf4j.Logger

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Create_bluemap.MODID)
class Create_bluemap
    (modContainer: ModContainer) {
    init {
        NeoForge.EVENT_BUS.register(this)

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        BlueMapAPI.onEnable { api: BlueMapAPI -> Watcher.start(api) }
        BlueMapAPI.onDisable { _: BlueMapAPI -> Watcher.stop() }
    }

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        Watcher.stop()
    }

    companion object {
        // Define mod id in a common place for everything to reference
        const val MODID: String = "create_bluemap"

        // Directly reference a slf4j logger
        val LOGGER: Logger = LogUtils.getLogger()
    }
}
