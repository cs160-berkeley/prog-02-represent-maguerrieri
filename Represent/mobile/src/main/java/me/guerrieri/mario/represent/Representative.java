package me.guerrieri.mario.represent;

import android.media.Image;

/**
 * Created by marioguerrieri on 2/27/16.
 */
public class Representative {
    private final String name;
    private final RepType type;
    private final String state;
    private final Party party;
    private final Image photo;

    public enum RepType {
        sen,
        rep
    }

    public enum Party {
        dem,
        rep,
        ind
    }

    public Representative(String name, RepType type, String state, Party party, Image photo) {
        this.name = name;
        this.type = type;
        this.state = state;
        this.party = party;
        this.photo = photo;
    }
}
