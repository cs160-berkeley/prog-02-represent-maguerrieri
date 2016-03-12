package me.guerrieri.mario.represent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class PhoneListenerService extends WearableListenerService {
    private static final String TAG = "PhoneListenerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "created");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, String.format("Message received: %s %s", messageEvent.getPath(), new String(messageEvent.getData())));
        String path = messageEvent.getPath();
        if (path.equalsIgnoreCase(this.getString(R.string.rep_changed_path))) {
            Intent startRep = new Intent(this.getString(R.string.rep_changed_action))
                    .putExtra("rep", new String(messageEvent.getData()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(startRep);
        } else if (path.equalsIgnoreCase(this.getString(R.string.rep_random_path))) {
            Intent startRep = new Intent(this.getString(R.string.rep_random_action))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(startRep);
        }
    }
}
