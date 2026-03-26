package dev.szedann.create_bluemap

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent.Loading
import net.neoforged.fml.event.config.ModConfigEvent.Reloading
import net.neoforged.neoforge.common.ModConfigSpec

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Create_bluemap.MODID)
object Config {
    private val BUILDER = ModConfigSpec.Builder()

    private val INTERVAL_TRAINS: ModConfigSpec.IntValue = BUILDER
        .comment("Interval between train updates")
        .defineInRange("interval", 5, 1, 30)

    private val INTERVAL_TRACKS: ModConfigSpec.IntValue = BUILDER
        .comment("Interval between track updates")
        .defineInRange("interval_tracks", 30, 10, 240)

    private val RENDER_TRACKS: ModConfigSpec.BooleanValue = BUILDER
        .comment("Whether to render tracks")
        .define("renderTracks", false)

    private val RENDER_CARRIAGES: ModConfigSpec.BooleanValue = BUILDER
        .comment("Whether to render carriages")
        .define("renderCarriages", true)

    private val RENDER_TRAINS: ModConfigSpec.BooleanValue = BUILDER
        .comment("Whether to render trains")
        .define("renderTrains", true)

    val SPEC: ModConfigSpec = BUILDER.build()

    var trainInterval: Int = 0
    var trackInterval: Int = 0
    var renderTracks: Boolean = false
    var renderCarriages: Boolean = false
    var renderTrains: Boolean = false

    private fun applyValues() {
        trainInterval = INTERVAL_TRAINS.get()
        trackInterval = INTERVAL_TRACKS.get()
        renderTracks = RENDER_TRACKS.get()
        renderCarriages = RENDER_CARRIAGES.get()
        renderTrains = RENDER_TRAINS.get()
    }

    @SubscribeEvent
    fun onLoad(event: Loading) {
        applyValues()
    }

    @SubscribeEvent
    fun onReload(event: Reloading) {
        applyValues()
    }
}
