package com.mjstokely.rsplayground;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import io.reactivex.functions.Consumer;

/**
 * Created by mjordan on 11/20/17.
 */

public class BindingAdapters {

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String url) {
        Picasso.with(view.getContext())
               .load(url)
               .into(view);
    }

    @BindingAdapter({"bind:imageUrl", "bind:blurRadius"})
    public static void loadImage(final ImageView view, final String url, final float blurRadius) {

        RsApplication.getInstance()
                     .Rs
            .subscribe(new Consumer<RenderScript>() {
                @Override
                public void accept(RenderScript renderScript) throws Exception {
                    Picasso.with(view.getContext())
                           .load(url)
                           .transform(new BlurTransform(renderScript,
                                                        blurRadius))
                           .into(view);
                }
            });


    }

    static class BlurTransform implements Transformation{

        private final RenderScript mRs;
        private final float mRadius;

        BlurTransform(RenderScript rs, float radius) {
            mRs = rs;
            mRadius = radius;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Allocation allocIn = Allocation.createFromBitmap(mRs, source);

            Bitmap result = Bitmap.createBitmap(source);
            Allocation allocOut = Allocation.createFromBitmap(mRs, result);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(mRs, Element.U8_4(mRs));
            blur.setRadius(mRadius);
            blur.setInput(allocIn);
            blur.forEach(allocOut);
            allocOut.copyTo(result);

            if (source != result) {
                source.recycle();
            }

            return result;
        }

        @Override
        public String key() {
            return "rs blur";
        }
    }
}
