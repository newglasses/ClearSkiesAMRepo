package com.example.newglasses.clearskiesam;

import android.content.Intent;

/**
 * Created by newglasses on 09/08/2016.
 */
public class BackgroundService extends WakefulIntentService {

    public BackgroundService() {
        super("BackgroundService");
    }

    /**
     * Asynchronous background operations of service, with wakelock
     */
    @Override
    public void doWakefulWork(Intent intent) {
        // your code here...
        //AlarmTime alarmTime = new AlarmTime();
        //alarmTime.setAlarm(this);

        Intent i = new Intent(BackgroundService.this, ClearSkiesService.class);
        startService(i);
    }
}
