package me.guerrieri.mario.represent;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by marioguerrieri on 3/3/16.
 */
public class PresCardFragment extends Fragment {
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_pres_card, container, false);
        ((TextView) this.view.findViewById(R.id.loc_name)).setText(this.getArguments().getString("county"));
        ((TextView) this.view.findViewById(R.id.obama_num)).setText(String.format("%d", (int) this.getArguments().getDouble("obama")));
        ((TextView) this.view.findViewById(R.id.romney_num)).setText(String.format("%d", (int) this.getArguments().getDouble("romney")));
        LinearLayout.LayoutParams obamaParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) this.getArguments().getDouble("obama"));
        LinearLayout.LayoutParams romneyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (float) this.getArguments().getDouble("romney"));
        this.view.findViewById(R.id.obama_tile).setLayoutParams(obamaParams);
        this.view.findViewById(R.id.romney_tile).setLayoutParams(romneyParams);
        return this.view;
    }

    public void updateZip(String zip) {
        ((TextView) this.view.findViewById(R.id.loc_name)).setText(zip);
    }
}
