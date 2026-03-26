package dev.szedann.create_bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Watcher {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> trainFuture;
    private static ScheduledFuture<?> trackFuture;

    private static void cancelScheduledTasks() {
        if (trainFuture != null) {
            trainFuture.cancel(false);
            trainFuture = null;
        }
        if (trackFuture != null) {
            trackFuture.cancel(false);
            trackFuture = null;
        }
    }

    public static synchronized void start(BlueMapAPI api) {
        Create_bluemap.LOGGER.info("Starting Create Bluemap updater");
        cancelScheduledTasks();

        Runnable trainUpdater = () -> {
            try {
                Trains.update(api);
            } catch (Exception e) {
                Create_bluemap.LOGGER.error("Failed to update trains", e);
            }
        };
        Runnable trackUpdater = () -> {
            try {
                Tracks.update(api);
            } catch (Exception e) {
                Create_bluemap.LOGGER.error("Failed to update tracks", e);
            }
        };
        trainFuture = scheduler.scheduleAtFixedRate(trainUpdater, 0, Config.trainInterval, TimeUnit.SECONDS);
        trackFuture = scheduler.scheduleAtFixedRate(trackUpdater, 0, Config.trackInterval, TimeUnit.SECONDS);

    }

    public static synchronized void stop() {
        cancelScheduledTasks();
    }
}