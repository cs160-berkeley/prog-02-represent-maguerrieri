package me.guerrieri.mario.represent.common;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by marioguerrieri on 3/4/16.
 */
public class HTTPImageCache {
    public static final HTTPImageCache current = new HTTPImageCache();

    private HashMap<String, Drawable> images;

    public HTTPImageCache() {
        this.images = new HashMap<>();
    }

    public Drawable get(String url) {
        try {
            if (this.images.containsKey(url)) return this.images.get(url);
            else {
                Drawable result = Drawable.createFromStream((InputStream) new URL(url).getContent(), "");
                this.images.put(url, result);
                return result;
            }
        } catch (IOException e) {
//            throw new IllegalArgumentException("invalid url");
            e.printStackTrace();
            return null;
        }
    }
}
