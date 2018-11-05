package com.yazidmf.streamingradioislami;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class StreamingService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    //player
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //phone
    private boolean isPausedCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //receiver
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;

    @Override
    public void onCreate() {
        //init receiver
        filter = new IntentFilter();
        filter.addAction("Matikan");
        filter.addAction("Nyalakan");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("Matikan")) {
                    if (mediaPlayer != null) {
                        pauseMedia();
                    }
                } else if (action.equals("Nyalakan")) {
                    playMedia();
                }
            }
        };

        registerReceiver(broadcastReceiver, filter);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initIfPhoneCall();
        showNotif(intent.getExtras().getString("name"));
        mediaPlayer.reset();
        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(intent.getExtras().getString("url"));
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        unregisterReceiver(broadcastReceiver);
        hideNotif();
    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void initIfPhoneCall() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            isPausedCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (isPausedCall) {
                                isPausedCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void showNotif(String name) {
        Intent stopIntent = new Intent(this, StreamingReceiver.class);
        stopIntent.setAction("Keluar");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 12345, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, StreamingReceiver.class);
        pauseIntent.setAction("Matikan");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 12345, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, StreamingReceiver.class);
        playIntent.setAction("Nyalakan");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 12345, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker("Channel: " + name)
                .setOngoing(true)
                .setContentTitle(name)
                .setContentText("Sedang Didengarkan...")
                .addAction(android.R.drawable.ic_media_play, "Nyalakan", playPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Matikan", pausePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Keluar", stopPendingIntent)
        ;
        startForeground(115, builder.build());
    }

    private void hideNotif() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(115);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
