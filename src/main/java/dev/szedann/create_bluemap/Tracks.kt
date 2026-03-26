package dev.szedann.create_bluemap

import com.flowpowered.math.vector.Vector3d
import com.simibubi.create.Create
import com.simibubi.create.content.trains.graph.TrackEdge
import com.simibubi.create.content.trains.graph.TrackGraph
import com.simibubi.create.content.trains.graph.TrackNodeLocation
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.BlueMapWorld
import de.bluecolored.bluemap.api.markers.LineMarker
import de.bluecolored.bluemap.api.markers.MarkerSet
import de.bluecolored.bluemap.api.math.Color
import de.bluecolored.bluemap.api.math.Line
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.*
import java.util.function.Consumer

object Tracks {
    private val trackColor = Color("#fff")
    @JvmStatic
    fun update(api: BlueMapAPI) {
        if (!Config.renderTracks) return
        val lineMarkerSets: MutableMap<ResourceKey<Level>, MarkerSet> = HashMap<ResourceKey<Level>, MarkerSet>()

        Create.RAILWAYS.trackNetworks.forEach { (_: UUID, graph: TrackGraph) ->
            val edges = HashSet<TrackEdge>()
            graph.getNodes().forEach(Consumer { graphNode: TrackNodeLocation ->
                val node = graph.locateNode(graphNode)
                edges.addAll(graph.getConnectionsFrom(node).values)

                val level = node.getLocation().dimension
                if (!lineMarkerSets.containsKey(level)) {
                    lineMarkerSets[level] = MarkerSet.builder()
                        .label(String.format("Tracks in %s", level.location().toShortLanguageKey())).build()
                }
            })
            edges.forEach(Consumer { edge: TrackEdge ->
                val lineMarkerSet: MarkerSet = lineMarkerSets[edge.node1.getLocation().dimension]!!
                if (edge.isInterDimensional) return@Consumer
                val line = Line.builder()
                addEdge(line, edge, graph, false)
                val marker = LineMarker.builder()
                    .line(line.build())
                    .lineWidth(6) //                        .maxDistance(300)
                    .label("edge")
                    .depthTestEnabled(false)
                    .listed(false)
                    .lineColor(trackColor)
                    .build()
                lineMarkerSet.put(edge.toString(), marker)
            })
        }

        lineMarkerSets.forEach { (level: ResourceKey<Level>, markerSet: MarkerSet) ->
            api.getWorld(level).ifPresent(
                Consumer { world: BlueMapWorld ->
                    for (map in world.maps) {
                        map.markerSets[String.format("tracks-%s", level.location().toShortLanguageKey())] = markerSet
                    }
                })
        }
    }

    fun addEdge(line: Line.Builder, edge: TrackEdge, graph: TrackGraph, skipFirst: Boolean) {
        val segmentCount = if (edge.isTurn) (edge.length / 16).toInt() + 2 else 2
        for (i in (if (skipFirst) 1 else 0)..<segmentCount) {
            val pos = edge.getPosition(graph, i.toDouble() / (segmentCount - 1))
            line.addPoint(Vector3d(pos.x, pos.y + 1, pos.z))
        }
    }
}