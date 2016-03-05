package me.guerrieri.mario.represent;

import android.app.Fragment;
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
        view.findViewById(R.id.info_box).setBackgroundColor(this.getResources().getColor(Representative.Party.values()[args.getInt("party")].getColor()));
        ((TextView) view.findViewById(R.id.rep_name)).setText(args.getString("name"));
        ((TextView) view.findViewById(R.id.rep_desc)).setText(
                String.format(this.getResources().getString(R.string.rep_desc_format),
                        Representative.RepType.values()[args.getInt("type")].toString(),
                        Representative.Party.values()[args.getInt("party")].toString(),
                        args.getString("state")
                )
        );
        ((ImageView) view.findViewById(R.id.rep_tile)).setImageDrawable(this.getResources().getDrawable(
                args.getString("name").startsWith("F") ? R.drawable.default_rep_image :
                        args.getString("name").startsWith("C") ? R.drawable.default_sen2_image :
                                R.drawable.default_sen1_image,
                null
        ));
        return view;
    }
}
