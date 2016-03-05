package me.guerrieri.mario.represent;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

/**
 * Created by marioguerrieri on 3/4/16.
 */
public class BytesRepresentative extends Representative {
    public final byte[] photo;

    public BytesRepresentative(String name, RepType type, String state, Party party,
                               byte[] photo, String username, String tweet, Bill[] bills,
                               Committee[] committees) {
        super(name, type, state, party, 0, username, tweet, bills, committees);
        this.photo = photo;
    }

    public BytesRepresentative(Bundle bundle) {
        super(bundle);
        this.photo = bundle.getByteArray("photo");
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putByteArray("photo", this.photo);
        return bundle;
    }
}
