package dev.szedann.create_bluemap;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.station.GlobalStation;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class Stations {
    public static void update(BlueMapAPI api) {
        if (!Config.renderStations) return;
        Map<ResourceKey<Level>, MarkerSet> markerSets = new HashMap<>();

        Create.RAILWAYS.trackNetworks.forEach((uuid, graph) -> {
            for (GlobalStation station : graph.getPoints(EdgePointType.STATION)) {
                if (station.blockEntityPos == null || station.blockEntityDimension == null) continue;
                ResourceKey<Level> level = station.blockEntityDimension;
                markerSets.computeIfAbsent(level, k -> MarkerSet.builder()
                        .label(String.format("Stations in %s", k.location().toShortLanguageKey()))
                        .build());
                BlockPos pos = station.blockEntityPos;
                markerSets.get(level).put(station.id.toString(), POIMarker.builder()
                        .label(station.name)
                        .position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                        .maxDistance(300)
                        .build());
            }
        });

        markerSets.forEach((level, markerSet) -> api.getWorld(level).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put(String.format("stations-%s", level.location().toShortLanguageKey()), markerSet);
            }
        }));
    }
}
