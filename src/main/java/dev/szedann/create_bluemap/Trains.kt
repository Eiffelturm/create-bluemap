package dev.szedann.create_bluemap

import com.flowpowered.math.vector.Vector3d
import com.simibubi.create.Create
import com.simibubi.create.content.trains.entity.Train
import com.simibubi.create.content.trains.graph.TrackNode
import com.simibubi.create.content.trains.schedule.ScheduleRuntime
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.BlueMapWorld
import de.bluecolored.bluemap.api.markers.LineMarker
import de.bluecolored.bluemap.api.markers.MarkerSet
import de.bluecolored.bluemap.api.markers.POIMarker
import de.bluecolored.bluemap.api.math.Color
import de.bluecolored.bluemap.api.math.Line
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

object Trains {
    private val manualColor = Color("#f99")
    private val scheduledColor = Color("#99f")

    fun update(api: BlueMapAPI) {
        if (Config.renderTrains) updatePOIs(api)
        if (Config.renderCarriages) updateCarriages(api)
    }

    private fun updatePOIs(api: BlueMapAPI) {
        val POIMarkerSets: MutableMap<ResourceKey<Level>, MarkerSet> = HashMap<ResourceKey<Level>, MarkerSet>()

        Create.RAILWAYS.trains.forEach { (uuid: UUID, train: Train) ->
            val node: TrackNode = train.carriages[0].leadingPoint.node1 ?: return@forEach
            val level = node.getLocation().dimension
            if (!POIMarkerSets.containsKey(level)) {
                POIMarkerSets[level] = MarkerSet.builder()
                    .defaultHidden(true)
                    .label(String.format("Trains in %s", level.location().toShortLanguageKey())).build()
            }

            val pos: Vec3 = train.carriages[0].leadingPoint.getPosition(train.graph)
            val marker = POIMarker.builder()
                .label(train.name.string)
                .position(pos.x, pos.y, pos.z)
                .maxDistance(150.0)
                .build()
            POIMarkerSets[level]!!.put(uuid.toString(), marker)
        }

        POIMarkerSets.forEach { (level: ResourceKey<Level>, markerSet: MarkerSet) ->
            api.getWorld(level).ifPresent(
                Consumer { world: BlueMapWorld ->
                    for (map in world.maps) {
                        map.markerSets[String.format("trains-%s", level.location().toShortLanguageKey())] = markerSet
                    }
                })
        }
    }

    private fun updateCarriages(api: BlueMapAPI) {
        val lineMarkerMap: MutableMap<ResourceKey<Level>, MarkerSet> = HashMap<ResourceKey<Level>, MarkerSet>()

        Create.RAILWAYS.trains.forEach { (_: UUID, train: Train) ->
            var i = 0
            for (carriage in train.carriages) {
                val node = carriage.leadingPoint.node1 ?: return@forEach
                val level = node.getLocation().dimension

                if (carriage.trailingPoint.node2.getLocation().dimension !== level) return@forEach
                if (!lineMarkerMap.containsKey(level)) {
                    lineMarkerMap[level] = MarkerSet.builder()
                        .label(String.format("Carriages in %s", level.location().toShortLanguageKey())).build()
                }
                i++
                val p1 = carriage.leadingPoint.getPosition(train.graph)
                val p2 = carriage.trailingPoint.getPosition(train.graph)
                val front = if (train.currentlyBackwards) i == train.carriages.size else i == 1
                val scheduled = train.runtime.state == ScheduleRuntime.State.IN_TRANSIT
                lineMarkerMap[level]!!.put(
                    train.id.toString() + "-" + carriage.id, LineMarker.builder()
                        .label(String.format("%s carriage %s", train.name.string, i))
                        .line(
                            Line.builder()
                                .addPoint(Vector3d(p1.x, p1.y + 1, p1.z))
                                .addPoint(Vector3d(p2.x, p2.y + 1, p2.z))
                                .build()
                        )
                        .lineColor(if (scheduled) scheduledColor else manualColor)
                        .lineWidth(if (front) 7 else 5)
                        .depthTestEnabled(false) // .maxDistance(400)
                        .build()
                )
            }
        }

        lineMarkerMap.forEach { (level: ResourceKey<Level>, markerSet: MarkerSet) ->
            api.getWorld(level).ifPresent(
                Consumer { world: BlueMapWorld ->
                    for (map in world.maps) {
                        map.markerSets[String.format("carriages-%s", level.location().toShortLanguageKey())] =
                            markerSet
                    }
                })
        }
    }
}