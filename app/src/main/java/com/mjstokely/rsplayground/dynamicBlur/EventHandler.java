package com.mjstokely.rsplayground.dynamicBlur;

import com.mystokely.rsplayground.ScriptC_viewportBlur;

import android.renderscript.Allocation;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by mjordan on 11/22/17.
 */

public class EventHandler {

    private PublishSubject<BlurRequest> mBlurRequests;
    private Observable<BlurResult> mRenders;

    public EventHandler(
        Single<Allocation> renderOut,
        Single<ScriptC_viewportBlur> script) {

        mBlurRequests = PublishSubject.create();

        mRenders = mBlurRequests
            .withLatestFrom(
                script.toObservable(),
                renderOut.toObservable(),
                new Function3<BlurRequest,ScriptC_viewportBlur,Allocation,BlurResult>() {

                    @Override
                    public BlurResult apply(BlurRequest blurRequest, ScriptC_viewportBlur script, Allocation out) throws Exception {
                        Log.d(DynamicBlurActivity.TAG, "call script kernel");
                        script.forEach_blur(out);
                        out.ioSend();
                        return new BlurResult();
                    }
                })
            .subscribeOn(Schedulers.computation());
    }

    public void onBlurRequest() {
        mBlurRequests.onNext(new BlurRequest());
    }

    public Observable<BlurResult> getRenders() {
        return mRenders;
    }

    static class BlurRequest {

        final float BlurRadius = 10f;
    }

    static class BlurResult{

    }
}
