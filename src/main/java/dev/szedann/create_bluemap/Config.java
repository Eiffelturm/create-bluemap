package dev.szedann.create_bluemap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Create_bluemap.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec SPEC = BUILDER.build();
    private static final ModConfigSpec.IntValue INTERVAL_TRAINS = BUILDER
            .comment("Interval between train updates in seconds")
            .defineInRange("interval", 5, 1, 30);
    private static final ModConfigSpec.IntValue INTERVAL_TRACKS = BUILDER
            .comment("Interval between track and station updates in seconds")
            .defineInRange("interval_tracks", 30, 10, 240);
    private static final ModConfigSpec.BooleanValue RENDER_TRACKS = BUILDER
            .comment("Whether to render tracks")
            .define("renderTracks", false);
    private static final ModConfigSpec.BooleanValue RENDER_CARRIAGES = BUILDER
            .comment("Whether to render carriages")
            .define("renderCarriages", true);
    private static final ModConfigSpec.BooleanValue RENDER_TRAINS = BUILDER
            .comment("Whether to render trains")
            .define("renderTrains", true);
    private static final ModConfigSpec.BooleanValue RENDER_STATIONS = BUILDER
            .comment("Whether to render stations")
            .define("renderStations", true);
    private static final ModConfigSpec.BooleanValue RENDER_SIGNALS = BUILDER
            .comment("Whether to render signals")
            .define("renderSignals", true);
    public static int trainInterval;
    public static int trackInterval;
    public static boolean renderTracks;
    public static boolean renderCarriages;
    public static boolean renderTrains;
    public static boolean renderStations;
    public static boolean renderSignals;

    private static void applyValues() {
        trainInterval = INTERVAL_TRAINS.get();
        trackInterval = INTERVAL_TRACKS.get();
        renderTracks = RENDER_TRACKS.get();
        renderCarriages = RENDER_CARRIAGES.get();
        renderTrains = RENDER_TRAINS.get();
        renderStations = RENDER_STATIONS.get();
        renderSignals = RENDER_SIGNALS.get();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        applyValues();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        applyValues();
    }
}
