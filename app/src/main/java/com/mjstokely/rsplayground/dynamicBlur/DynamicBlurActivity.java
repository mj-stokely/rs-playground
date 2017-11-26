package com.mjstokely.rsplayground.dynamicBlur;

import com.mjstokely.rsplayground.R;
import com.mjstokely.rsplayground.RsApplication;
import com.mjstokely.rsplayground.databinding.ADynamicBlurBinding;
import com.mystokely.rsplayground.ScriptC_viewportBlur;
import com.mystokely.rsplayground.ScriptC_viewportLetterBox;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by mjordan on 11/22/17.
 */

public class DynamicBlurActivity extends AppCompatActivity {

    public static final String TAG = DynamicBlurActivity.class.toString();

    private EventHandler mEventHandler;
    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ADynamicBlurBinding binding = DataBindingUtil.setContentView(this,
                                                                     R.layout.a_dynamic_blur);

        Bitmap input = provideInputSample();

        resizeTextureToFitInput(binding, input);

        Single<RenderScript> rs = RsApplication.getInstance().Rs;
        mEventHandler = new EventHandler(provideRenderAllocation(rs,
                                                                 provideSurfaceSingle(binding.textureView)),
                                         provideLetterBoxScript(input,
                                                           rs));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDisposable = mEventHandler.getRenders()
                                   .subscribe();

        Flowable.intervalRange(1, 340, 0, 16, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "send blur request");
                        float yPercent = (float) aLong / 810;
                        mEventHandler.onBlurRequest(yPercent, 0.6f);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.dispose();
    }

    private void resizeTextureToFitInput(ADynamicBlurBinding binding, Bitmap scaledInput) {
        ViewGroup.LayoutParams layoutParams = binding.textureView.getLayoutParams();
        layoutParams.width = scaledInput.getWidth();
        layoutParams.height = scaledInput.getHeight();
        binding.textureView.setLayoutParams(layoutParams);
    }

    private Bitmap provideInputSample() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                          .getMetrics(metrics);

        Bitmap inputBitmap = BitmapFactory.decodeResource(getResources(),
                                                          R.raw.pod);

        int inputWidth = metrics.widthPixels;
        int inputHeight = (3 * inputWidth) / 4;
        Bitmap scaledInput = Bitmap.createScaledBitmap(inputBitmap,
                                                       inputWidth,
                                                       inputHeight,
                                                       false);
        if (scaledInput != inputBitmap) {
            inputBitmap.recycle();
        }
        return scaledInput;
    }

    private Single<ScriptC_viewportLetterBox> provideLetterBoxScript(Bitmap inputBitmap, Single<RenderScript> rs) {
        return Single.zip(rs,
                          provideInputAllocation(rs,
                                                 inputBitmap),
                          new BiFunction<RenderScript, Allocation, ScriptC_viewportLetterBox>() {
                              @Override
                              public ScriptC_viewportLetterBox apply(RenderScript renderScript, Allocation in)
                                  throws Exception {
                                  ScriptC_viewportLetterBox script = new ScriptC_viewportLetterBox(renderScript);
                                  script.set_inAllocation(in);
                                  Log.d(TAG, "return script");
                                  return script;
                              }
                          });
    }

    private Single<ScriptC_viewportBlur> provideBlurScript(Bitmap inputBitmap, Single<RenderScript> rs) {
        return Single.zip(rs,
                          provideInputAllocation(rs,
                                                 inputBitmap),
                          new BiFunction<RenderScript, Allocation, ScriptC_viewportBlur>() {
                              @Override
                              public ScriptC_viewportBlur apply(RenderScript renderScript, Allocation in)
                                  throws Exception {
                                  ScriptC_viewportBlur script = new ScriptC_viewportBlur(renderScript);
                                  script.set_inAllocation(in);
                                  Log.d(TAG, "return script");
                                  return script;
                              }
                          });
    }

    private Single<Allocation> provideInputAllocation(Single<RenderScript> renderScript, final Bitmap input) {
        return renderScript.map(new Function<RenderScript, Allocation>() {
            @Override
            public Allocation apply(RenderScript renderScript) throws Exception {
                Log.d(TAG, "return input allocation\ninput width: " + input.getWidth() + "\ninput height: " + input.getHeight());
                return Allocation.createFromBitmap(renderScript, input);
            }
        });
    }

    private Single<Allocation> provideRenderAllocation(Single<RenderScript> renderScript, Single<SurfaceResult> surfaceTexture) {
        return renderScript.zipWith(surfaceTexture,
                                    new BiFunction<RenderScript, SurfaceResult, Allocation>() {
                                        @Override
                                        public Allocation apply(RenderScript rs, SurfaceResult surface) throws Exception {
                                            Type type = new Type.Builder(rs, Element.RGBA_8888(rs))
                                                .setX(surface.Width)
                                                .setY(surface.Height)
                                                .create();

                                            Allocation out = Allocation.createTyped(rs,
                                                                                    type,
                                                                                    Allocation.USAGE_SCRIPT | Allocation.USAGE_IO_OUTPUT);

                                            out.setSurface(surface.Surface);
                                            Log.d(TAG, "return render allocation\nwidth: " + surface.Width
                                                + "\nheight" + surface.Height);
                                            return out;
                                        }
                                    });
    }

    private Single<SurfaceResult> provideSurfaceSingle(final TextureView textureView) {
        return Single.create(new SingleOnSubscribe<SurfaceResult>() {
            @Override
            public void subscribe(final SingleEmitter<SurfaceResult> emitter) throws Exception {
                TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        Log.d(TAG, "surface created, return surface\nwidth: " + width + "\nheight: " + height);
                        emitter.onSuccess(new SurfaceResult(new Surface(surface),
                                                            width,
                                                            height));
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                        Log.d(TAG, "surface texture size changed\nwidth: " + width + "\nheight: " + height);
                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                    }
                };

                Log.d(TAG, "set surface texture listener");
                textureView.setSurfaceTextureListener(textureListener);
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        Log.d(TAG, "cancel texture listener");
                        textureView.setSurfaceTextureListener(null);
                    }
                });
            }
        });

    }

    public static class SurfaceResult {

        public final Surface Surface;
        public final int Width;
        public final int Height;

        public SurfaceResult(Surface surface, int width, int height) {
            Surface = surface;
            Width = width;
            Height = height;
        }
    }
}
