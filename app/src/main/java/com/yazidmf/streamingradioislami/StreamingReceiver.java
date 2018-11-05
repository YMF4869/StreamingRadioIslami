package com.yazidmf.streamingradioislami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StreamingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String param = intent.getAction();
        if (param.equals("Keluar")) {
            Intent service1 = new Intent(context, StreamingService.class);
            context.stopService(service1);
        } else if (param.equals("Matikan")) {
            context.sendBroadcast(new Intent("Matikan"));
        } else if (param.equals("Nyalakan")) {
            context.sendBroadcast(new Intent("Nyalakan"));
        }
    }
}
