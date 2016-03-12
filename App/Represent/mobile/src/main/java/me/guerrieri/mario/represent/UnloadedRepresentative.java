package me.guerrieri.mario.represent;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureRunnable;
import com.koushikdutta.async.future.FutureThread;
import com.koushikdutta.ion.Ion;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import me.guerrieri.mario.represent.common.Bill;
import me.guerrieri.mario.represent.common.Committee;
import me.guerrieri.mario.represent.common.Representative;

/**
 * Created by marioguerrieri on 3/10/16.
 */
public class UnloadedRepresentative extends Representative {
    public final long tweetID;
    public final String photoURL;
    public final String bannerURL;
    public final boolean showBanner;
    public final String website;
    public final String email;

    public UnloadedRepresentative(String name, RepType type, String state, Party party,
                                  String photoURL, String bannerURL, boolean showBanner,
                                  long tweetID, String website, String email, Bill[] bills,
                                  Committee[] committees) {
        super(name, type, state, party, null, null, bills, committees);
        this.tweetID = tweetID;
        this.photoURL = photoURL;
        this.bannerURL = bannerURL;
        this.showBanner = showBanner;
        this.website = website;
        this.email = email;
    }

    public UnloadedRepresentative(Bundle bundle) {
        super(bundle);
        this.tweetID = bundle.getLong("tweetID");
        this.photoURL = bundle.getString("photoURL");
        this.bannerURL = bundle.getString("bannerURL");
        this.showBanner = bundle.getBoolean("showBanner");
        this.website = bundle.getString("website");
        this.email = bundle.getString("email");
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = super.toBundle();
        bundle.putLong("tweetID", this.tweetID);
        bundle.putString("photoURL", this.photoURL);
        bundle.putString("bannerURL", this.bannerURL);
        bundle.putBoolean("showBanner", this.showBanner);
        bundle.putString("website", this.website);
        bundle.putString("email", this.email);
        return bundle;
    }

    public Future<Bundle> toLoadedBundle(final Context context) {
        final UnloadedRepresentative rep = this;
        return new FutureThread<>(new FutureRunnable<Bundle>() {
            @Override
            public Bundle run() throws Exception {
                Bitmap photo = Ion.with(context)
                        .load(rep.photoURL)
                        .asBitmap()
                        .get();
                ByteBuffer photoBuf = ByteBuffer.allocate(photo.getRowBytes() * photo.getHeight());
                photo.copyPixelsToBuffer(photoBuf);
                String photoFileName = String.format("%s photo", rep.name);
                FileOutputStream photoFileStream = context.openFileOutput(photoFileName,
                        Context.MODE_PRIVATE);
                photoFileStream.write(photoBuf.array());
                photoFileStream.close();

                Bitmap banner = Ion.with(context)
                        .load(rep.bannerURL)
                        .asBitmap()
                        .get();
                ByteBuffer bannerBuf = ByteBuffer.allocate(banner.getRowBytes() * banner.getHeight());
                banner.copyPixelsToBuffer(bannerBuf);
                String bannerFileName = String.format("%s banner", rep.name);
                FileOutputStream bannerFileStream = context.openFileOutput(bannerFileName,
                        Context.MODE_PRIVATE);
                bannerFileStream.write(bannerBuf.array());
                bannerFileStream.close();

                Bundle bundle = rep.toBundle();
                bundle.putString("photoFileName", photoFileName);
                bundle.putString("bannerFileName", bannerFileName);
                return bundle;
            }
        });
    }

//    public Future<LocalLoadedRepresentative> load(final Context context) {
//        final UnloadedRepresentative rep = this;
//        return new FutureThread<>(new FutureRunnable<LocalLoadedRepresentative>() {
//            @Override
//            public LocalLoadedRepresentative run() throws Exception {
//                Bitmap photo = Ion.with(context)
//                        .load(rep.photoURL)
//                        .asBitmap()
//                        .get();
//                Bitmap banner = Ion.with(context)
//                        .load(rep.bannerURL)
//                        .asBitmap()
//                        .get();
//                return new LocalLoadedRepresentative(rep.name, rep.type, rep.state, rep.party, photo, banner,
//                        rep.bills, rep.committees);
//            }
//        });
//    }
}
