package me.guerrieri.mario.represent;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;

/**
 * Created by marioguerrieri on 2/27/16.
 */
public class Representative {
    public final String name;
    public final RepType type;
    public final String state;
    public final Party party;
    public final int photoId;

    public final String username;
    public final String tweet;

    public final Bill[] bills;
    public final Committee[] committees;

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

        public int getColor() {
            if (this == Representative.Party.dem) return R.color.demColor;
            else if (this == Representative.Party.rep) return R.color.repColor;
            else return R.color.indColor;
        }

        @Override
        public String toString() {
            if (this == dem) return "D";
            else if (this == rep) return "R";
            else return "I";
        }
    }

    public Representative(String name, RepType type, String state, Party party, int photoId,
                          String username, String tweet, Bill[] bills, Committee[] committees) {
        this.name = name;
        this.type = type;
        this.state = state;
        this.party = party;
        this.photoId = photoId;

        this.username = username;
        this.tweet = tweet;

        this.bills = bills;
        this.committees = committees;
    }

    public Representative(Bundle bundle) {
        this.name = bundle.getString("name");
        this.type = RepType.values()[bundle.getInt("type")];
        this.state = bundle.getString("state");
        this.party = Party.values()[bundle.getInt("party")];
        this.photoId = bundle.getInt("photoId");
        this.username = bundle.getString("username");
        this.tweet = bundle.getString("tweet");
        Parcelable[] bills = bundle.getParcelableArray("bills");
        this.bills = new Bill[bills.length];
        for (int i = 0; i < bills.length; i ++) this.bills[i] = (Bill) bills[i];
        Parcelable[] committees = bundle.getParcelableArray("committees");
        this.committees = new Committee[committees.length];
        for (int i = 0; i < committees.length; i ++) this.committees[i] = (Committee) committees[i];
    }

    public Bundle toBundle() {
        Bundle out = new Bundle();
        out.putString("name", this.name);
        out.putInt("type", this.type.ordinal());
        out.putString("state", this.state);
        out.putInt("party", this.party.ordinal());
        out.putInt("photoId", this.photoId);
        out.putString("username", this.username);
        out.putString("tweet", this.tweet);
        out.putParcelableArray("bills", bills);
        out.putParcelableArray("committees", committees);
        return out;
    }
}
