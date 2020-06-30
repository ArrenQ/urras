package com.chuang.urras.toolskit.basic;

import com.chuang.urras.toolskit.basic.util.ScheduleKit;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FutureKit {
    public static <T> CompletableFuture<T> error(Throwable throwable) {
        CompletableFuture f = new CompletableFuture();
        f.completeExceptionally(throwable);
        return f;
    }

    public static <T> CompletableFuture<List<T>> allOf(CompletableFuture<T>... futures) {
        return CompletableFuture.allOf(futures)
                .thenApply(v ->
                        Arrays.stream(futures).map(CompletableFuture::join).collect(Collectors.toList())
                );

    }
    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream().map(CompletableFuture::join).collect(Collectors.toList())
                );
    }

    public static <T> CompletableFuture<T> retry(Supplier<CompletableFuture<T>> futureGetter, int times, int delay, TimeUnit unit, Predicate<Object> retryMatcher) {
        CompletableFuture<T> future = new CompletableFuture<>();

        futureGetter.get().whenComplete((t, throwable) -> {
            if(times <= 0) {
                done(future, t, throwable);
                return;
            }
            boolean needRetry =
                    (null != throwable && retryMatcher.test(throwable))
                            || retryMatcher.test(t);
            if (needRetry) {
                Runnable command = () -> retry(futureGetter, times - 1, delay, unit, retryMatcher)
                        .whenComplete((t1, throwable1) -> done(future, t1, throwable1));

                if(delay < 0) {
                    command.run();
                } else {
                    ScheduleKit.schedule(command, delay, unit);
                }
                return;
            }

            done(future, t, throwable);
        });

        return future;
    }

    private static <T> void done(CompletableFuture<T> future, @Nullable T value, @Nullable Throwable throwable) {
        if(null != throwable) {
            future.completeExceptionally(throwable);
        } else {
            future.complete(value);
        }
    }
}
