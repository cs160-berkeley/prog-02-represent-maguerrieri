package me.guerrieri.mario.represent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import me.guerrieri.mario.represent.common.Representative;

public class PhoneToWatchService extends Service {
    private static String TAG = "PhoneToWatchService";

    private PhoneToWatchService service = this;

    private GoogleApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        this.apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "API connected.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                }).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            ArrayList<Representative> repBundles = new ArrayList<>();
            int i = 0;
            Bundle bundle = extras.getBundle("0");
            while (bundle != null) {
                repBundles.add(new Representative(bundle));
                bundle = extras.getBundle(Integer.toString(++i));
            }
            this.apiClient.connect();
            this.sendReps(repBundles.toArray(new Representative[repBundles.size()]));
        }

        return START_STICKY;
    }

    private DataMap repToDataMap(Representative rep) {
        DataMap out = new DataMap();
        out.putString("name", rep.name);
        out.putInt("type", rep.type.ordinal());
        out.putString("state", rep.state);
        out.putInt("party", rep.party.ordinal());
        out.putInt("photoId", rep.photoId);
        out.putString("username", rep.username);
        out.putString("tweet", rep.tweet);
        ArrayList<DataMap> bills = new ArrayList<>();
        for (int i = 0; i < rep.bills.length; i ++) {
            DataMap bill = new DataMap();
            bill.putString("title", rep.bills[i].title);
            bill.putString("date", rep.bills[i].date);
            bills.add(bill);
        }
        out.putDataMapArrayList("bills", bills);
        ArrayList<DataMap> committees = new ArrayList<>();
        for (int i = 0; i < rep.committees.length; i ++) {
            DataMap committee = new DataMap();
            committee.putString("title", rep.committees[i].title);
            committee.putString("date", rep.committees[i].date);
            committees.add(committee);
        }
        out.putDataMapArrayList("committees", committees);
        return out;
    }

    private void sendReps(Representative[] reps) {
        PutDataMapRequest mapReq = PutDataMapRequest.create(this.getString(R.string.rep_message_path));

        DataMap map = mapReq.getDataMap();
        map.putString("timestamp", Long.toString(System.currentTimeMillis()));
        ArrayList<DataMap> repsData = new ArrayList<>();
        for (Representative rep : reps) {
            repsData.add(repToDataMap(rep));
        }
        map.putDataMapArrayList("reps", repsData);

        PutDataRequest req = mapReq.asPutDataRequest();//.setUrgent(); TODO
        PendingResult<DataApi.DataItemResult> result = Wearable.DataApi.putDataItem(this.apiClient, req);
        result.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, String.format("Sent reps to watch: %s", dataItemResult.getStatus().getStatusMessage()));
            }
        });

//        new Thread( new Runnable() {
//            @Override
//            public void run() {
//                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( service.apiClient ).await();
//                for(Node node : nodes.getNodes()) {
//                    //we find 'nodes', which are nearby bluetooth devices (aka emulators)
//                    //send a message for each of these nodes (just one, for an emulator)
//                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
//                            service.apiClient, node.getId(), "test", "test".getBytes() ).await();
//                    //4 arguments: api client, the node ID, the path (for the listener to parse),
//                    //and the message itself (you need to convert it to bytes.)
//                }
//            }
//        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.apiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
