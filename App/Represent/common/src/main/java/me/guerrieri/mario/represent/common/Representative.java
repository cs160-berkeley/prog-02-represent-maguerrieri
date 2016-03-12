package me.guerrieri.mario.represent.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.nio.Buffer;
import java.nio.ByteBuffer;


/**
 * Created by marioguerrieri on 2/27/16.
 */
public class Representative {
    public final String name;
    public final RepType type;
    public final String state;
    public final Party party;
    public final Bitmap photo;
    public final Bitmap banner;

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

        public static RepType fromString(String title) {
            return title.equalsIgnoreCase("sen") ? sen : rep;
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

        public static Party fromString(String abr) {
            return abr.equalsIgnoreCase("d") ? dem : abr.equalsIgnoreCase("r") ? rep : ind;
        }
    }

    public Representative(String name, RepType type, String state, Party party, Bitmap photo, Bitmap banner,
                          Bill[] bills, Committee[] committees) {
        this.name = name;
        this.type = type;
        this.state = state;
        this.party = party;
        this.photo = photo;
        this.banner = banner;

        this.bills = bills;
        this.committees = committees;
    }

    public Representative(Bundle bundle) {
        this.name = bundle.getString("name");
        this.type = RepType.values()[bundle.getInt("type")];
        this.state = bundle.getString("state");
        this.party = Party.values()[bundle.getInt("party")];
        byte[] photo = bundle.getByteArray("photo");
        this.photo = photo != null ? BitmapFactory.decodeByteArray(photo, 0, photo.length) : null;
        byte[] banner = bundle.getByteArray("banner");
        this.banner = banner != null ? BitmapFactory.decodeByteArray(photo, 0, banner.length) : null;
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
        if (this.photo != null) {
            ByteBuffer pbuf = ByteBuffer.allocate(this.photo.getRowBytes() * this.photo.getHeight());
            this.photo.copyPixelsToBuffer(pbuf);
            out.putByteArray("photo", pbuf.array());
        }
        if (this.banner != null) {
            ByteBuffer bbuf = ByteBuffer.allocate(this.banner.getRowBytes() * this.banner.getHeight());
            this.banner.copyPixelsToBuffer(bbuf);
            out.putByteArray("banner", bbuf.array());
        }
        out.putParcelableArray("bills", bills);
        out.putParcelableArray("committees", committees);
        return out;
    }
}
