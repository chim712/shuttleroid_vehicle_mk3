package com.shuttleroid.vehicle.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 앱 전역에서 공용으로 사용하는 Executor/Handler
 * - diskIO: DB/파일 I/O 등 블로킹 연산
 * - mainThread: UI 갱신 작업
 */
public class AppExecutors {
    private static final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static ExecutorService diskIO() {
        return diskIO;
    }

    public static Handler mainThread() {
        return mainHandler;
    }
}
