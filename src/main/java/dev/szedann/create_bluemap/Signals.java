package dev.szedann.create_bluemap;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class Signals {
    public static void update(BlueMapAPI api) {
        if (!Config.renderSignals) return;
        Map<ResourceKey<Level>, MarkerSet> markerSets = new HashMap<>();

        Create.RAILWAYS.trackNetworks.forEach((uuid, graph) -> {
            for (SignalBoundary signal : graph.getPoints(EdgePointType.SIGNAL)) {
                if (signal.edgeLocation == null) continue;
                ResourceKey<Level> level = signal.edgeLocation.getFirst().dimension;
                if (level == null) continue;

                markerSets.computeIfAbsent(level, k -> MarkerSet.builder()
                        .label(String.format("Signals in %s", k.location().toShortLanguageKey()))
                        .defaultHidden(true)
                        .build());

                for (boolean primary : new boolean[]{true, false}) {
                    SignalState state = signal.cachedStates.get(primary);
                    String stateText = (state == null || state == SignalState.INVALID) ? "Signal" : "Signal [" + state.name() + "]";
                    signal.blockEntities.get(primary).keySet().forEach(pos ->
                            markerSets.get(level).put(
                                    signal.id + "-" + (primary ? "p" : "s") + "-" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ(),
                                    POIMarker.builder()
                                            .label(stateText)
                                            .position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                                            .maxDistance(150)
                                            .build()
                            )
                    );
                }
            }
        });

        markerSets.forEach((level, markerSet) -> api.getWorld(level).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put(String.format("signals-%s", level.location().toShortLanguageKey()), markerSet);
            }
        }));
    }
}
