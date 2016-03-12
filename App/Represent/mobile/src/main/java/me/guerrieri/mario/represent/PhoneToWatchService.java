package me.guerrieri.mario.represent;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.FutureRunnable;
import com.koushikdutta.async.future.FutureThread;
import com.koushikdutta.ion.Ion;

import java.io.ByteArrayOutputStream;
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
        this.apiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            String type = extras.getString("type");
            if (type.equals("reps")) {
                ArrayList<UnloadedRepresentative> reps = new ArrayList<>();
                for (int i = 0; extras.containsKey(Integer.toString(i)); i ++)
                    reps.add(new UnloadedRepresentative(extras.getBundle(Integer.toString(i))));
                this.send(reps);
            } else {
                this.send(extras.getString("county"), extras.getDouble("obama"), extras.getDouble("romney"));
            }
        }

        return START_STICKY;
    }

//    private Future<DataMap> repToDataMap(final UnloadedRepresentative rep) {
//        return new FutureThread<DataMap>(new FutureRunnable<DataMap>() {
//            @Override
//            public DataMap run() throws Exception {
//                DataMap out = new DataMap();
//
//                Bitmap banner = Ion.with(service)
//                        .load(rep.bannerURL)
//                        .asBitmap()
//                        .get();
//                ByteBuffer bannerBuf = ByteBuffer.allocate(photo.getRowBytes() * photo.getHeight());
//                photo.copyPixelsToBuffer(bannerBuf);
//                out.putByteArray("photo", bannerBuf.array());
//            }
//        });
//
//    }

    private void send(ArrayList<UnloadedRepresentative> reps) {
        PutDataMapRequest mapReq = PutDataMapRequest.create(this.getString(R.string.rep_message_path));
        DataMap map = mapReq.getDataMap();
        map.putString("dataType", "rep");
        for (int i = 0; i < reps.size(); i ++) {
            DataMap innerMap = new DataMap();
            this.copyRepToDataMap(reps.get(i), innerMap);
            map.putDataMap(Integer.toString(i), innerMap);
        }
        PutDataRequest req = mapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(this.apiClient, req)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, String.format("Sent reps to watch: %s", dataItemResult.getStatus().getStatusMessage()));
                    }
                });
    }

    private void send(String county, double obama, double romney) {
        PutDataMapRequest mapReq = PutDataMapRequest.create(this.getString(R.string.loc_path));
        DataMap map = mapReq.getDataMap();
        map.putString("dataType", "loc");
        map.putString("county", county);
        map.putDouble("obama", obama);
        map.putDouble("romney", romney);
        PutDataRequest req = mapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(this.apiClient, req)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(TAG, String.format("Sent loc change to watch: %s", dataItemResult.getStatus().getStatusMessage()));
                    }
                });
    }

    private void copyRepToDataMap(UnloadedRepresentative rep, DataMap toSend) {
        try {
            toSend.putString("name", rep.name);
            toSend.putInt("type", rep.type.ordinal());
            toSend.putString("state", rep.state);
            toSend.putInt("party", rep.party.ordinal());

            Bitmap photo = Ion.with(service)
                    .load(rep.photoURL)
                    .asBitmap()
                    .get();
//            ByteBuffer photoBuf = ByteBuffer.allocate(photo.getRowBytes() * photo.getHeight());
            ByteArrayOutputStream photoStream = new ByteArrayOutputStream();
            double photoScale = photo.getHeight() / 300.0;
            Bitmap.createScaledBitmap(photo,
                    (int) (photoScale * photo.getWidth()),
                    (int) (photoScale * photo.getHeight()), false)
                    .compress(Bitmap.CompressFormat.PNG, 100, photoStream);
            toSend.putAsset("photo", Asset.createFromBytes(photoStream.toByteArray()));
            photoStream.close();

            ByteArrayOutputStream bannerStream = new ByteArrayOutputStream();
            double bannerScale = photo.getHeight() / 300.0;
            Bitmap.createScaledBitmap(photo,
                    (int) (bannerScale * photo.getWidth()),
                    (int) (bannerScale * photo.getHeight()), false)
                    .compress(Bitmap.CompressFormat.PNG, 100, bannerStream);
            toSend.putAsset("banner", Asset.createFromBytes(bannerStream.toByteArray()));
            bannerStream.close();

//            Bitmap banner = Ion.with(service)
//                    .load(rep.bannerURL)
//                    .asBitmap()
//                    .get();
//            ByteBuffer bannerBuf = ByteBuffer.allocate(banner.getRowBytes() * banner.getHeight());
//            banner.copyPixelsToBuffer(bannerBuf);
//            toSend.putByteArray("banner", bannerBuf.array());

            ArrayList<DataMap> bills = new ArrayList<>();
            for (int i = 0; i < rep.bills.length; i ++) {
                DataMap bill = new DataMap();
                bill.putString("title", rep.bills[i].title);
                bill.putString("date", rep.bills[i].date);
                bills.add(bill);
            }
            toSend.putDataMapArrayList("bills", bills);
            ArrayList<DataMap> committees = new ArrayList<>();
            for (int i = 0; i < rep.committees.length; i ++) {
                DataMap committee = new DataMap();
                committee.putString("title", rep.committees[i].title);
                committee.putString("desc", rep.committees[i].desc);
                committees.add(committee);
            }
            toSend.putDataMapArrayList("committees", committees);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
