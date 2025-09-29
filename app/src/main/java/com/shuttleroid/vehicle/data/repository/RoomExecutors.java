package com.shuttleroid.vehicle.data.repository;

import android.os.Handler;
import android.os.Looper;

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
}
