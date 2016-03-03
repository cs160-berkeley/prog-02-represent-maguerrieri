package me.guerrieri.mario.represent;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by marioguerrieri on 3/1/16.
 */
public class Bill implements Parcelable {
    public final String title;
    public final String date;

    public Bill(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public static final Parcelable.Creator<Bill> CREATOR = new Creator<Bill>() {
        @Override
        public Bill createFromParcel(Parcel source) {
            return new Bill(source);
        }

        @Override
        public Bill[] newArray(int size) {
            return new Bill[size];
        }
    };

    public Bill(Parcel parcel) {
        this.title = parcel.readString();
        this.date = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.date);
    }
}
