package me.guerrieri.mario.represent;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

public class WatchListenerService extends WearableListenerService {
    private static final String TAG = "WatchListenerService";

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Watch listener created.");
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        this.googleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Received from phone.");
        for (DataEvent e : dataEvents) {
            DataMap dataMap = DataMapItem.fromDataItem(e.getDataItem()).getDataMap();
            if (dataMap.getString("dataType").equals("rep")) {
                String dataPath = this.getString(R.string.rep_message_path);
                Intent dataChangedIntent = new Intent(this.getString(R.string.rep_changed_action));
                dataChangedIntent.putExtra("dataPath", dataPath);
                LocalBroadcastManager.getInstance(this).sendBroadcast(dataChangedIntent);
            } else {
                String dataPath = this.getString(R.string.loc_path);
                Intent dataChangedIntent = new Intent(this.getString(R.string.rep_changed_action));
                dataChangedIntent.putExtra("dataPath", dataPath);
                LocalBroadcastManager.getInstance(this).sendBroadcast(dataChangedIntent);
            }
//                Uri assetURI = dataMap.getAsset("photo").getUri();
//                int index = dataMap.getInt("ind");
////                Intent repChanged = new Intent(this.getString(R.string.rep_changed_action))
////                        .putExtra()
////                        ;
//                Bitmap test = loadBitmapFromAsset(Asset.createFromUri(assetURI));
//                Log.d(TAG, "fuck you IntelliJ");
//            }
//            Intent startRep = new Intent(this, RepActivity.class)
//                    .putExtra("reps", dataMapsToBundle(data.getDataMapArrayList("reps")))
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            this.startActivity(startRep);
        }
    }

//    private static Bundle dataMapsToBundle(ArrayList<DataMap> maps) {
//        Bundle reps = new Bundle();
//        for (int i = 0; i < maps.size(); i ++) {
//            reps.putBundle(Integer.toString(i), dataMapToRep(maps.get(i)).toBundle());
//        }
//        return reps;
//    }

//    private static BytesRepresentative dataMapToRep(DataMap map) {
//        ArrayList<DataMap> billsMaps = map.getDataMapArrayList("bills");
//        ArrayList<Bill> billsList = new ArrayList<>();
//        for (DataMap bMap : billsMaps)
//            billsList.add(new Bill(bMap.getString("title"), bMap.getString("desc")));
//
//        ArrayList<DataMap> committeesMaps = map.getDataMapArrayList("bills");
//        ArrayList<Committee> committeesList = new ArrayList<>();
//        for (DataMap cMap : committeesMaps)
//            committeesList.add(new Committee(cMap.getString("title"), cMap.getString("desc")));
//        return new BytesRepresentative(
//                map.getString("name"),
//                Representative.RepType.values()[map.getInt("type")],
//                map.getString("state"),
//                Representative.Party.values()[map.getInt("party")],
//                new byte[0],
//                map.getString("username"),
//                map.getString("tweet"),
//                billsList.toArray(new Bill[billsList.size()]),
//                committeesList.toArray(new Committee[committeesList.size()])
//        );
//    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, String.format("Message received: %s %s", messageEvent.getPath(), new String(messageEvent.getData())));
        Intent startRep = new Intent("rep_changed")
                .putExtra("ind", Integer.valueOf(new String(messageEvent.getData())))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startRep);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.googleApiClient.disconnect();
    }
}
