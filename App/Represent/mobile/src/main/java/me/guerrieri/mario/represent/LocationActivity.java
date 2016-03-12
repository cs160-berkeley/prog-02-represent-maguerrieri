package me.guerrieri.mario.represent;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    LocationActivity activity = this;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();

                ((EditText) this.findViewById(R.id.enter_zip)).setOnEditorActionListener(this.zipEditListener);
        this.findViewById(R.id.loc_button).setOnClickListener(this.locationClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.googleApiClient.disconnect();
    }

    TextView.OnEditorActionListener zipEditListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (v.getText().length() == 5) {
                    final String zip = v.getText().toString();
                    Ion.with(activity)
                            .load(String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s", zip))
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    Log.d(TAG, result.toString());
                                    result = result.get("results").getAsJsonArray()
                                            .get(0).getAsJsonObject()
                                            .get("geometry").getAsJsonObject()
                                            .get("location").getAsJsonObject();
                                    Intent intent = new Intent(activity, RepListActivity.class)
                                            .putExtra(activity.getString(R.string.zip_bundle_key),
                                                    zip)
                                            .putExtra(activity.getString(R.string.lat_bundle_key),
                                                    result.get("lat").getAsDouble())
                                            .putExtra(activity.getString(R.string.long_bundle_key),
                                                    result.get("lng").getAsDouble());
                                    activity.startActivity(intent);
                                }
                            });
                    return true;
                } else {
                    v.setError(getString(R.string.invalid_zip));
                }
            }
            return false;
        }
    };

    View.OnClickListener locationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            String zip = getString(R.string.default_zip); // TODO: replace with geolocation API call
//            ((EditText) activity.findViewById(R.id.enter_zip)).setText(zip);
            Intent intent = new Intent(activity, RepListActivity.class);
            Location loc = LocationServices.FusedLocationApi
                    .getLastLocation(activity.googleApiClient);
            intent.putExtra(activity.getString(R.string.lat_bundle_key), loc.getLatitude());
            intent.putExtra(activity.getString(R.string.long_bundle_key), loc.getLongitude());
            activity.startActivity(intent);
        }
    };
}
