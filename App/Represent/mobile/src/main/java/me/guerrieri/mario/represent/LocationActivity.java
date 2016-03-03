package me.guerrieri.mario.represent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    static final String EXTRA_ZIP = "me.guerrieri.mario.represent.ZIP";
    LocationActivity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ((EditText) this.findViewById(R.id.enter_zip)).setOnEditorActionListener(this.zipEditListener);
        this.findViewById(R.id.loc_button).setOnClickListener(this.locationClickListener);
    }

    TextView.OnEditorActionListener zipEditListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (v.getText().length() == 5) {
                    startActivity(new Intent(activity, RepListActivity.class).putExtra(EXTRA_ZIP,
                            v.getText().toString()));
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
            String zip = getString(R.string.default_zip); // TODO: replace with geolocation API call
            ((EditText) activity.findViewById(R.id.enter_zip)).setText(zip);
            activity.startActivity(new Intent(activity, RepListActivity.class).putExtra(EXTRA_ZIP, zip));
        }
    };
}
