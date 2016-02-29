package me.guerrieri.mario.represent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocationActivity extends AppCompatActivity implements TextView.OnEditorActionListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ((EditText) this.findViewById(R.id.enter_zip)).setOnEditorActionListener(this);
        this.findViewById(R.id.loc_button).setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            startActivity(new Intent(this, RepActivity.class).putExtra(Intent.EXTRA_TEXT, ((EditText) this.findViewById(R.id.enter_zip)).getText()));
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        String zip = "94709"; // TODO: replace with geolocation API call
        this.startActivity(new Intent(this, RepActivity.class).putExtra(Intent.EXTRA_TEXT, zip));
    }
}
