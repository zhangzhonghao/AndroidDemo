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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.example.androiddemo.R;

public class PomodoroService extends Service {

    public static final String ACTION_START = "com.example.androiddemo.tools.ACTION_START";
    public static final String ACTION_PAUSE = "com.example.androiddemo.tools.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.androiddemo.tools.ACTION_STOP";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_STATE = "state";

    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long TICK_INTERVAL = 1000L;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private long remainingTimeMs = 0;
    private int currentState = PomodoroTimerActivity.STATE_WORK;

    private BroadcastReceiver commandReceiver;

    private Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && remainingTimeMs > 0) {
                remainingTimeMs -= TICK_INTERVAL;
                updateNotification();
                broadcastUpdate();

                if (remainingTimeMs <= 0) {
                    remainingTimeMs = 0;
                    onTimerComplete();
                } else {
                    handler.postDelayed(this, TICK_INTERVAL);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        commandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_PAUSE.equals(action)) {
                    pauseTimer();
                } else if (ACTION_STOP.equals(action)) {
                    stopTimer();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        registerReceiver(commandReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                remainingTimeMs = intent.getLongExtra(EXTRA_DURATION, remainingTimeMs);
                currentState = intent.getIntExtra(EXTRA_STATE, PomodoroTimerActivity.STATE_WORK);
                startTimer();
            }
        }
        return START_STICKY;
    }

    private void startTimer() {
        isRunning = true;
        startForeground(NOTIFICATION_ID, buildNotification());
        handler.post(tickRunnable);
    }

    private void pauseTimer() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
        updateNotification();
        broadcastUpdate();
    }

    private void stopTimer() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void onTimerComplete() {
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
        stopForeground(STOP_FOREGROUND_REMOVE);

        Intent broadcastIntent = new Intent(PomodoroTimerActivity.ACTION_COMPLETE);
        sendBroadcast(broadcastIntent);

        stopSelf();
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(PomodoroTimerActivity.ACTION_UPDATE);
        intent.putExtra(PomodoroTimerActivity.EXTRA_REMAINING_TIME, remainingTimeMs);
        intent.putExtra(PomodoroTimerActivity.EXTRA_IS_RUNNING, isRunning);
        intent.putExtra(PomodoroTimerActivity.EXTRA_CURRENT_TYPE, currentState);
        sendBroadcast(intent);
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, PomodoroTimerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String stateText = getStateText();
        String timeText = formatTime(remainingTimeMs);

        Intent pauseIntent = new Intent(this, PomodoroService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(
                this, 1, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, PomodoroService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(stateText)
                .setContentText(timeText)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .addAction(R.drawable.ic_pause, isRunning ? "暂停" : "继续", pausePendingIntent)
                .addAction(R.drawable.ic_stop, "停止", stopPendingIntent);

        return builder.build();
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private String getStateText() {
        switch (currentState) {
            case PomodoroTimerActivity.STATE_WORK:
                return "🍅 工作中";
            case PomodoroTimerActivity.STATE_SHORT_BREAK:
                return "☕ 短休息";
            case PomodoroTimerActivity.STATE_LONG_BREAK:
                return "🌴 长休息";
            default:
                return "番茄钟";
        }
    }

    private String formatTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "番茄钟",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("番茄钟计时通知");
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
        handler.removeCallbacks(tickRunnable);
        if (commandReceiver != null) {
            unregisterReceiver(commandReceiver);
        }
    }
}