package me.guerrieri.mario.represent;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;

/**
 * Created by marioguerrieri on 2/27/16.
 */
public class Representative {
    public final String name;
    public final RepType type;
    public final String state;
    public final Party party;
    public final Drawable photo;

    public final String username;
    public final String tweet;

    public final Bill[] bills;
    public final Committee[] committees;

    public static Representative getRep(String zip, Resources resources) {
        // TODO: replace with API calls
        String[] billNames = resources.getStringArray(R.array.default_rep_bill_names);
        String[] billDates = resources.getStringArray(R.array.default_rep_bill_dates);
        Bill[] bills = new Bill[billNames.length];
        for (int i = 0; i < billNames.length; i ++) {
            bills[i] = new Bill(billNames[i], billDates[i]);
        }
        String[] committeeNames = resources.getStringArray(R.array.default_rep_committee_names);
        String[] committeeDates = resources.getStringArray(R.array.default_rep_committee_dates);
        Committee[] committees = new Committee[committeeNames.length];
        for (int i = 0; i < committeeNames.length; i ++) {
            committees[i] = new Committee(committeeNames[i], committeeDates[i]);
        }
        return new Representative(
                resources.getString(R.string.default_rep_name),
                RepType.rep,
                resources.getString(R.string.default_rep_state),
                Party.values()[resources.getInteger(R.integer.default_rep_party)],
                ResourcesCompat.getDrawable(resources, R.drawable.default_rep_image, null),
                resources.getString(R.string.default_rep_username),
                resources.getString(R.string.default_rep_tweet),
                bills, committees
        );
    }

    public static Representative[] getSens(String zip, Resources resources) {
        // TODO: replace with API calls
        String[] billNames1 = resources.getStringArray(R.array.default_sen1_bill_names);
        String[] billDates1 = resources.getStringArray(R.array.default_sen1_bill_dates);
        Bill[] bills1 = new Bill[billNames1.length];
        for (int i = 0; i < billNames1.length; i ++) {
            bills1[i] = new Bill(billNames1[i], billDates1[i]);
        }
        String[] committeeNames1 = resources.getStringArray(R.array.default_sen1_committee_names);
        String[] committeeDates1 = resources.getStringArray(R.array.default_sen1_committee_dates);
        Committee[] committees1 = new Committee[committeeNames1.length];
        for (int i = 0; i < committeeNames1.length; i ++) {
            committees1[i] = new Committee(committeeNames1[i], committeeDates1[i]);
        }

        String[] billNames2 = resources.getStringArray(R.array.default_sen2_bill_names);
        String[] billDates2 = resources.getStringArray(R.array.default_sen2_bill_dates);
        Bill[] bills2 = new Bill[billNames2.length];
        for (int i = 0; i < billNames2.length; i ++) {
            bills2[i] = new Bill(billNames2[i], billDates2[i]);
        }
        String[] committeeNames2 = resources.getStringArray(R.array.default_sen2_committee_names);
        String[] committeeDates2 = resources.getStringArray(R.array.default_sen2_committee_dates);
        Committee[] committees2 = new Committee[committeeNames2.length];
        for (int i = 0; i < committeeNames2.length; i ++) {
            committees2[i] = new Committee(committeeNames2[i], committeeDates2[i]);
        }

        return new Representative[] {
            new Representative(
                    resources.getString(R.string.default_sen1_name),
                    RepType.sen,
                    resources.getString(R.string.default_sen1_state),
                    Party.values()[resources.getInteger(R.integer.default_sen1_party)],
                    ResourcesCompat.getDrawable(resources, R.drawable.default_sen1_image, null),
                    resources.getString(R.string.default_sen1_username),
                    resources.getString(R.string.default_sen1_tweet),
                    bills1, committees1
            ),
            new Representative(
                    resources.getString(R.string.default_sen2_name),
                    RepType.sen,
                    resources.getString(R.string.default_sen2_state),
                    Party.values()[resources.getInteger(R.integer.default_sen2_party)],
                    ResourcesCompat.getDrawable(resources, R.drawable.default_sen2_image, null),
                    resources.getString(R.string.default_sen2_username),
                    resources.getString(R.string.default_sen2_tweet),
                    bills2, committees2
            )
        };
    }

    public enum RepType {
        sen,
        rep;

        @Override
        public String toString() {
            if (this == sen) return "Senator";
            else return "Representative";
        }
    }

    public enum Party {
        dem,
        rep,
        ind;

        @Override
        public String toString() {
            if (this == dem) return "D";
            else if (this == rep) return "R";
            else return "I";
        }
    }

    public Representative(String name, RepType type, String state, Party party, Drawable photo,
                          String username, String tweet, Bill[] bills, Committee[] committees) {
        this.name = name;
        this.type = type;
        this.state = state;
        this.party = party;
        this.photo = photo;

        this.username = username;
        this.tweet = tweet;

        this.bills = bills;
        this.committees = committees;
    }
}
