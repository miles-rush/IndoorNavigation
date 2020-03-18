package com.example.tool;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private PlayMusicBinder musicBinder = new PlayMusicBinder();

    public class PlayMusicBinder extends Binder {
        public void init(String path) {
            initMusic(path);
        }
        public void start() {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

        public void pause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }

        }

        public void stop() {
            mediaPlayer.reset();
        }

        public void getProgress() {
            mediaPlayer.getCurrentPosition();
        }

    }
    public MusicService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void initMusic(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
