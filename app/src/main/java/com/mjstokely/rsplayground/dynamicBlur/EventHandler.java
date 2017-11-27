package com.mjstokely.rsplayground.dynamicBlur;

import com.mystokely.rsplayground.ScriptC_viewportBlur;
import com.mystokely.rsplayground.ScriptC_viewportLetterBox;

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

    private PublishSubject<ViewPortRequest> mBlurRequests;
    private Observable<ViewPortResult> mRenders;


    public EventHandler(
        Single<Allocation> renderOut,
        Single<ScriptC_viewportBlur> script) {

        mBlurRequests = PublishSubject.create();

        mRenders = mBlurRequests
            .withLatestFrom(
                script.toObservable(),
                renderOut.toObservable(),
                new Function3<ViewPortRequest, ScriptC_viewportBlur, Allocation, ViewPortResult>() {

                    @Override
                    public ViewPortResult apply(ViewPortRequest viewPortRequest, ScriptC_viewportBlur script, Allocation out) throws Exception {

                        int frameHeight = out.getType()
                                             .getY();
                        int blurUntil = (int) (frameHeight * viewPortRequest.YPercentOffsetFromTop);

                        int blurAfter = (int) (frameHeight * viewPortRequest.YPercentOfTotalHeight) + blurUntil;
                        Log.d(DynamicBlurActivity.TAG, "call script kernel"
                            + "\nblur request: " + viewPortRequest.YPercentOffsetFromTop
                            + "\nblurUntil: " + blurUntil
                            + "\nblurAfter: " + blurAfter);



                        script.set_yApplyUntil(blurUntil);
                        script.set_yApplyAfter(blurAfter);
                        script.forEach_blur(out);
                        out.ioSend();
                        return new ViewPortResult();
                    }
                })
            .subscribeOn(Schedulers.computation());
    }

    public void onBlurRequest(float yPercentOffsetFromTop, float yPercentOfTotalHeight) {
        mBlurRequests.onNext(new ViewPortRequest(yPercentOffsetFromTop, yPercentOfTotalHeight));
    }

    public Observable<ViewPortResult> getRenders() {
        return mRenders;
    }

    static class ViewPortRequest {

        public final float YPercentOffsetFromTop;
        public final float YPercentOfTotalHeight;

        ViewPortRequest(float yPercentOffsetFromTop, float yPercentOfTotalHeight) {
            YPercentOffsetFromTop = yPercentOffsetFromTop;
            YPercentOfTotalHeight = yPercentOfTotalHeight;
        }
    }

    static class ViewPortResult {

    }
}
