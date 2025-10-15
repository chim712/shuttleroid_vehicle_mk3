package com.shuttleroid.vehicle.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * - diskIO: DB/파일 I/O 등 블로킹 연산
 * - mainThread: UI 갱신 작업
 */
public class RoomExecutors {
    private static final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    public static ExecutorService diskIO() {
        return diskIO;
    }

    // 메인(UI) 스레드 작업용
    private static final Executor mainThread = new Executor() {
        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainHandler.post(command);
        }
    };

    public static Executor mainThread() {
        return mainThread;
    }
}
