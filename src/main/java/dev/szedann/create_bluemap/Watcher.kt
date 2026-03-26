package dev.szedann.create_bluemap

import de.bluecolored.bluemap.api.BlueMapAPI
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Watcher {
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var trainFuture: ScheduledFuture<*>? = null
    private var trackFuture: ScheduledFuture<*>? = null

    private fun cancelScheduledTasks() {
        if (trainFuture != null) {
            trainFuture!!.cancel(false)
            trainFuture = null
        }
        if (trackFuture != null) {
            trackFuture!!.cancel(false)
            trackFuture = null
        }
    }

    @Synchronized
    fun start(api: BlueMapAPI) {
        Create_bluemap.LOGGER.info("Starting Create Bluemap updater")
        cancelScheduledTasks()

        val trainUpdater = Runnable {
            try {
                Trains.update(api)
            } catch (e: Exception) {
                Create_bluemap.LOGGER.error("Failed to update trains", e)
            }
        }
        val trackUpdater = Runnable {
            try {
                Tracks.update(api)
            } catch (e: Exception) {
                Create_bluemap.LOGGER.error("Failed to update tracks", e)
            }
        }
        trainFuture = scheduler.scheduleAtFixedRate(trainUpdater, 0, Config.trainInterval.toLong(), TimeUnit.SECONDS)
        trackFuture = scheduler.scheduleAtFixedRate(trackUpdater, 0, Config.trackInterval.toLong(), TimeUnit.SECONDS)
    }

    @Synchronized
    fun stop() {
        cancelScheduledTasks()
    }
}