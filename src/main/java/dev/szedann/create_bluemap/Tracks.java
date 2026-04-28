package dev.szedann.create_bluemap;

import com.flowpowered.math.vector.Vector3d;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.LineMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Line;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Tracks {
    private static final Color trackColor = new Color("#fff");
    private static final Color clearColor = new Color("#4f4");
    private static final Color reservedColor = new Color("#ff4");
    private static final Color occupiedColor = new Color("#f44");

    private static SignalBlockState getSignalBlockState(TrackEdge edge) {
        EdgeData edgeData = edge.getEdgeData();
        if (edgeData.hasSignalBoundaries()) return SignalBlockState.NONE;
        UUID groupId = edgeData.getSingleSignalGroup();
        if (groupId == null || groupId.equals(EdgeData.passiveGroup)) return SignalBlockState.NONE;
        SignalEdgeGroup group = Create.RAILWAYS.signalEdgeGroups.get(groupId);
        if (group == null) return SignalBlockState.NONE;
        if (!group.trains.isEmpty()) return SignalBlockState.OCCUPIED;
        if (group.reserved != null) return SignalBlockState.RESERVED;
        return SignalBlockState.CLEAR;
    }

    private static Color getEdgeColor(SignalBlockState state) {
        return switch (state) {
            case CLEAR -> clearColor;
            case RESERVED -> reservedColor;
            case OCCUPIED -> occupiedColor;
            default -> trackColor;
        };
    }

    private static String getEdgeLabel(SignalBlockState state) {
        return switch (state) {
            case CLEAR -> "Track [CLEAR]";
            case RESERVED -> "Track [RESERVED]";
            case OCCUPIED -> "Track [OCCUPIED]";
            default -> "Track";
        };
    }

    public static void update(BlueMapAPI api) {
        if (!Config.renderTracks) return;

        Map<ResourceKey<Level>, MarkerSet> lineMarkerSets = new HashMap<>();

        Create.RAILWAYS.trackNetworks.forEach((graphUuid, graph) -> {
            var edges = new HashSet<TrackEdge>();

            graph.getNodes().forEach(graphNode -> {
                var node = graph.locateNode(graphNode);
                edges.addAll(graph.getConnectionsFrom(node).values());

                ResourceKey<Level> level = node.getLocation().dimension;
                if (!lineMarkerSets.containsKey(level)) {
                    lineMarkerSets.put(level, MarkerSet.builder()
                            .label(String.format("Tracks in %s", level.location().toShortLanguageKey()))
                            .build());
                }
            });

            edges.forEach(edge -> {
                if (edge.isInterDimensional()) return;

                MarkerSet lineMarkerSet = lineMarkerSets.get(edge.node1.getLocation().dimension);
                SignalBlockState state = getSignalBlockState(edge);

                Line.Builder line = Line.builder();
                addEdge(line, edge, graph, false);

                LineMarker marker = LineMarker.builder()
                        .line(line.build())
                        .lineWidth(6)
//                        .maxDistance(300)
                        .label(getEdgeLabel(state))
                        .depthTestEnabled(false)
                        .listed(false)
                        .lineColor(getEdgeColor(state))
                        .build();

                lineMarkerSet.put(edge.toString(), marker);
            });
        });

        lineMarkerSets.forEach((level, markerSet) ->
                api.getWorld(level).ifPresent(world -> {
                    for (BlueMapMap map : world.getMaps()) {
                        map.getMarkerSets().put(
                                String.format("tracks-%s", level.location().toShortLanguageKey()),
                                markerSet);
                    }
                })
        );
    }

    public static void addEdge(Line.Builder line, TrackEdge edge, TrackGraph graph, boolean skipFirst) {
        int segmentCount = edge.isTurn() ? (int) (edge.getLength() / 16) + 2 : 2;

        for (int i = skipFirst ? 1 : 0; i < segmentCount; i++) {
            Vec3 pos = edge.getPosition(graph, (double) i / (segmentCount - 1));
            line.addPoint(new Vector3d(pos.x, pos.y + 1, pos.z));
        }
    }

    private enum SignalBlockState {NONE, CLEAR, RESERVED, OCCUPIED}
}