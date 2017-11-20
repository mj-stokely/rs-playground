package com.mjstokely.rsplayground;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by mjordan on 11/20/17.
 */

public class ResourceUtil {

    public static Uri fromResId(Context context, long resId) {
        return new Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.getPackageName())
            .appendPath(String.valueOf(resId))
            .build();
    }
}
