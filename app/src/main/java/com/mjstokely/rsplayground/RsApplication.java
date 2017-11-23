package com.mjstokely.rsplayground;

import android.app.Application;
import android.renderscript.RenderScript;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by mjordan on 11/20/17.
 */

public class RsApplication extends Application {

    private static RsApplication mInstance;
    public static RsApplication getInstance() {
        return mInstance;
    }

    /**
     * from https://developer.android.com/guide/topics/renderscript/compute.html#using-rs-from-java
     *
     * "You should consider context creation to be a potentially long-running operation, since
     * it may create resources on different pieces of hardware; it should not be in an
     * application's critical path if at all possible. Typically, an application will have
     * only a single RenderScript context at a time."
     *
     * Using Rx for a cached, lazy loaded, init from background thread, singleton
     */
    public final Single<RenderScript> Rs = Single
        .fromCallable(new Callable<RenderScript>() {
            @Override
            public RenderScript call() throws Exception {
                return RenderScript.create(RsApplication.this);
            }
        })
        .cache()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());


    public RsApplication() {
        mInstance = this;
    }
}
