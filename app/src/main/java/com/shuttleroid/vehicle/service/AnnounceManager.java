package com.shuttleroid.vehicle.service;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

import android.R;

public class AnnounceManager {
    private static AnnounceManager instance;
    private final Context context;
    private TextToSpeech tts;

    private Thread currentThread;
    private volatile boolean isPlaying;

    private AnnounceManager(Context context) {
        this.context = context.getApplicationContext();
        this.tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // 1. Set Locale (Language)
                tts.setLanguage(Locale.KOREAN);

                // 2. Set Voice
                Set<Voice> voices = tts.getVoices();
                for(Voice v:voices){
                    if(v.getLocale().equals(Locale.KOREA) && !v.isNetworkConnectionRequired())
                        //Log.d("TTS", "Voice: "+v.getName()+"/Locale: "+v.getLocale());
                        if(v.getName().contains("kob")) {
                            tts.setVoice(v);
                            Log.d("AnnounceManager", "Voice: "+v.getName()+"/Locale: "+v.getLocale());
                            break;
                        }
                }

                // 3. Set Pitch
                tts.setSpeechRate(1.05f);
                tts.setPitch(1.15f);
            }
        });



    }

    public static synchronized AnnounceManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnnounceManager(context);
        }
        return instance;
    }

    /** 새로운 안내방송 시작 (기존 방송 중단 후 새로 시작) */
    public synchronized void startAnnouncement(String currentStop, String nextStop) {
        stopCurrent(); // 기존 방송 중단

        currentThread = new Thread(() -> {
            try {
                isPlaying = true;

                // 1. 차임벨
                //playSound(R.raw.chime);

                // 2. "이번 정류소는~"
                //playSound(R.raw.this_stop);
                speak("이번 정류소는, " + currentStop + " 입니다.");

                // 3. "다음 정류소는~"
                //playSound(R.raw.next_stop);
                speak("다음 정류소는, " + nextStop + " 입니다.");

            } catch (InterruptedException e) {
                Log.d("AnnounceManager", "Announcement interrupted");
            } finally {
                isPlaying = false;
            }
        });
        currentThread.start();
        Log.d("AnnounceManager", "Announcement started");
    }

    /** 현재 방송 중단 */
    public synchronized void stopCurrent() {
        if (currentThread != null && currentThread.isAlive()) {
            currentThread.interrupt();
            currentThread = null;
        }
        isPlaying = false;
        tts.stop();
    }

    /** 파일 재생 */
    private void playSound(int resId) throws InterruptedException {
        MediaPlayer player = MediaPlayer.create(context, resId);
        player.setOnCompletionListener(MediaPlayer::release);
        player.start();

        // 동기 재생 보장
        while (player.isPlaying()) {
            Thread.sleep(100);
        }
    }

    /** TTS 재생 */
    private void speak(String text) throws InterruptedException {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AnnounceID");
        while (tts.isSpeaking()) {
            Thread.sleep(100);
        }
    }

    /** 리소스 해제 */
    public void release() {
        tts.shutdown();
    }
}
