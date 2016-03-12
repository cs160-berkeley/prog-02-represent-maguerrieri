package me.guerrieri.mario.represent;

import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.guerrieri.mario.represent.common.Representative;

/**
 * Created by marioguerrieri on 3/4/16.
 */
public class RepCardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rep_card, container, false);
        Bundle args = this.getArguments();
        Representative rep = ((RepActivity) this.getActivity()).getRep(args.getInt("ind"));
        view.findViewById(R.id.info_box).setBackgroundColor(this.getResources().getColor(rep.party.getColor()));
        ((TextView) view.findViewById(R.id.rep_name)).setText(rep.name);
        ((TextView) view.findViewById(R.id.rep_desc)).setText(
                String.format(this.getResources().getString(R.string.rep_desc_format),
                        rep.type.toString(),
                        rep.party.toString(),
                        rep.state
                )
        );
        ((ImageView) view.findViewById(R.id.rep_photo)).setImageDrawable(new BitmapDrawable(this.getResources(), rep.photo));
        ((ImageView) view.findViewById(R.id.rep_tile)).setImageDrawable(new BitmapDrawable(this.getResources(), rep.banner));
        return view;
    }
}
