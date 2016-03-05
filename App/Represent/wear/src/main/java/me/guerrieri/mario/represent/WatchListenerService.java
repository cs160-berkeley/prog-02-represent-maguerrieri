package me.guerrieri.mario.represent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class WatchListenerService extends WearableListenerService {
    private static final String TAG = "WatchListenerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Watch listener created.");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Received reps from phone.");
        for (DataEvent e : dataEvents) {
            DataMap data = DataMapItem.fromDataItem(e.getDataItem()).getDataMap();
            Intent startRep = new Intent(this, RepActivity.class)
                    .putExtra("reps", dataMapsToBundle(data.getDataMapArrayList("reps")))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(startRep);
        }
    }

    private static Bundle dataMapsToBundle(ArrayList<DataMap> maps) {
        Bundle reps = new Bundle();
        for (int i = 0; i < maps.size(); i ++) {
            reps.putBundle(Integer.toString(i), dataMapToRep(maps.get(i)).toBundle());
        }
        return reps;
    }

    private static BytesRepresentative dataMapToRep(DataMap map) {
        ArrayList<DataMap> billsMaps = map.getDataMapArrayList("bills");
        ArrayList<Bill> billsList = new ArrayList<>();
        for (DataMap bMap : billsMaps)
            billsList.add(new Bill(bMap.getString("title"), bMap.getString("date")));

        ArrayList<DataMap> committeesMaps = map.getDataMapArrayList("bills");
        ArrayList<Committee> committeesList = new ArrayList<>();
        for (DataMap cMap : committeesMaps)
            committeesList.add(new Committee(cMap.getString("title"), cMap.getString("date")));
        return new BytesRepresentative(
                map.getString("name"),
                Representative.RepType.values()[map.getInt("type")],
                map.getString("state"),
                Representative.Party.values()[map.getInt("party")],
                new byte[0],
                map.getString("username"),
                map.getString("tweet"),
                billsList.toArray(new Bill[billsList.size()]),
                committeesList.toArray(new Committee[committeesList.size()])
        );
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, String.format("Message received: %s %s", messageEvent.getPath(), new String(messageEvent.getData())));
        Intent startRep = new Intent("rep_changed")
                .putExtra("ind", Integer.valueOf(new String(messageEvent.getData())))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startRep);
    }
}
