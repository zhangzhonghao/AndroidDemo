package com.example.androiddemo.tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class WhiteNoiseService extends Service {

    public static final String ACTION_START = "com.example.androiddemo.tools.ACTION_WHITE_NOISE_START";
    public static final String ACTION_STOP = "com.example.androiddemo.tools.ACTION_WHITE_NOISE_STOP";
    public static final String ACTION_UPDATE_VOLUME = "com.example.androiddemo.tools.ACTION_WHITE_NOISE_UPDATE_VOLUME";
    public static final String EXTRA_SOUND_TYPE = "sound_type";
    public static final String EXTRA_VOLUME = "volume";
    public static final String EXTRA_MASTER_VOLUME = "master_volume";

    private static final String CHANNEL_ID = "white_noise_channel";
    private static final int NOTIFICATION_ID = 1002;

    // Sound types
    public static final int SOUND_RAIN = 0;
    public static final int SOUND_WAVE = 1;
    public static final int SOUND_FOREST = 2;
    public static final int SOUND_FIRE = 3;
    public static final int SOUND_WIND = 4;
    public static final int SOUND_WATERFALL = 5;
    public static final int SOUND_AIRCON = 6;
    public static final int SOUND_TRAFFIC = 7;
    public static final int SOUND_COUNT = 8;

    private Map<Integer, MediaPlayer> mediaPlayers = new HashMap<>();
    private Map<Integer, Boolean> soundStates = new HashMap<>();
    private float masterVolume = 0.8f;
    private Map<Integer, Float> soundVolumes = new HashMap<>();

    private BroadcastReceiver commandReceiver;
    private Handler handler = new Handler(Looper.getMainLooper());

    public WhiteNoiseService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Initialize sound states and volumes
        for (int i = 0; i < SOUND_COUNT; i++) {
            soundStates.put(i, false);
            soundVolumes.put(i, 0.5f);
        }

        commandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_STOP.equals(action)) {
                    stopAllSounds();
                    stopSelf();
                } else if (ACTION_UPDATE_VOLUME.equals(action)) {
                    int soundType = intent.getIntExtra(EXTRA_SOUND_TYPE, -1);
                    float volume = intent.getFloatExtra(EXTRA_VOLUME, 0.5f);
                    if (soundType >= 0 && soundType < SOUND_COUNT) {
                        soundVolumes.put(soundType, volume);
                        updateVolume(soundType);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_UPDATE_VOLUME);
        registerReceiver(commandReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startForeground(NOTIFICATION_ID, buildNotification());
            }
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private void stopAllSounds() {
        for (Map.Entry<Integer, MediaPlayer> entry : mediaPlayers.entrySet()) {
            MediaPlayer player = entry.getValue();
            if (player != null) {
                try {
                    if (player.isPlaying()) {
                        player.stop();
                    }
                    player.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            soundStates.put(entry.getKey(), false);
        }
        mediaPlayers.clear();
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    private void playSound(int soundType) {
        if (soundStates.getOrDefault(soundType, false)) {
            return; // Already playing
        }

        MediaPlayer player = mediaPlayers.get(soundType);
        if (player == null) {
            // Create new MediaPlayer - in production, load from raw resources
            // For now, we create a silent player as placeholder
            try {
                player = MediaPlayer.create(this, R.raw.sound_rain);
                if (player != null) {
                    player.setLooping(true);
                    mediaPlayers.put(soundType, player);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        if (player != null) {
            float volume = masterVolume * soundVolumes.get(soundType);
            player.setVolume(volume, volume);
            player.start();
            soundStates.put(soundType, true);
            updateNotification();
        }
    }

    private void stopSound(int soundType) {
        MediaPlayer player = mediaPlayers.get(soundType);
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayers.remove(soundType);
        }
        soundStates.put(soundType, false);
        updateNotification();
    }

    private void updateVolume(int soundType) {
        MediaPlayer player = mediaPlayers.get(soundType);
        if (player != null && soundStates.getOrDefault(soundType, false)) {
            float volume = masterVolume * soundVolumes.get(soundType);
            player.setVolume(volume, volume);
        }
    }

    private void setMasterVolume(float volume) {
        masterVolume = volume;
        for (Map.Entry<Integer, MediaPlayer> entry : mediaPlayers.entrySet()) {
            MediaPlayer player = entry.getValue();
            int soundType = entry.getKey();
            if (player != null && soundStates.getOrDefault(soundType, false)) {
                float vol = masterVolume * soundVolumes.get(soundType);
                player.setVolume(vol, vol);
            }
        }
    }

    private int getPlayingCount() {
        int count = 0;
        for (Boolean playing : soundStates.values()) {
            if (playing) count++;
        }
        return count;
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, WhiteNoiseActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, WhiteNoiseService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int playingCount = getPlayingCount();
        String contentText = playingCount > 0 ?
                "正在播放 " + playingCount + " 个音效" : "白噪音助眠";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("白噪音")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .addAction(R.drawable.ic_stop, "停止全部", stopPendingIntent);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "白噪音",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("白噪音播放通知");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllSounds();
        if (commandReceiver != null) {
            unregisterReceiver(commandReceiver);
        }
        handler.removeCallbacksAndMessages(null);
    }
}