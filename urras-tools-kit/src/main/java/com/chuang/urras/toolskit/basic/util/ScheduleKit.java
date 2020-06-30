package com.chuang.urras.toolskit.basic.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleKit {
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void schedule(Runnable run, int time, TimeUnit timeUnit) {
        executor.schedule(run, time, timeUnit);
    }
}
