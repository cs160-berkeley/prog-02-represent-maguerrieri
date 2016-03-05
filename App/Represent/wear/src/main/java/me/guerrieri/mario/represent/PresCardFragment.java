package me.guerrieri.mario.represent;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        if (this.getArguments() != null) ((TextView) this.view.findViewById(R.id.loc_name)).setText(this.getArguments().getString("zip"));
        return this.view;
    }

    public void updateZip(String zip) {
        ((TextView) this.view.findViewById(R.id.loc_name)).setText(zip);
    }
}
