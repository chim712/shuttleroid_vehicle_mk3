package com.shuttleroid.vehicle.app;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;

public class RetryPolicy {
    public interface Callback<T>{
        void onSuccess(T result);
        void onFailure(Throwable t);
        void onRetry(int attempt);
    }

    public static <T> void retryAsync(
            int maxAttempts, long delayMs, Callable<T> task, Callback<T> cb){
        Handler h = new Handler(Looper.getMainLooper());
        attempt(h, 1, maxAttempts, delayMs, task, cb);
    }

    private static <T> void attempt(Handler h, int attempt, int max, long delay,
                                    Callable<T> task, Callback<T> cb){
        h.post(() -> {
            try {
                T r = task.call();
                cb.onSuccess(r);
            } catch (Throwable t){
                if (attempt < max){
                    int next = attempt + 1;
                    cb.onRetry(attempt);
                    h.postDelayed(() -> attempt(h, next, max, delay, task, cb), delay);
                } else {
                    cb.onFailure(t);
                }
            }
        });
    }
}
