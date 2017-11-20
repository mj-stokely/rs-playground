package com.mjstokely.rsplayground.simpleBlur;

import android.net.Uri;

/**
 * Created by mjordan on 11/20/17.
 */

public class BlurData {
    public final String imageUrl;
    public final float blurRadius;

    public BlurData(Uri imageUri, float blurRadius) {
        this.imageUrl = imageUri.toString();
        this.blurRadius = blurRadius;
    }
}
