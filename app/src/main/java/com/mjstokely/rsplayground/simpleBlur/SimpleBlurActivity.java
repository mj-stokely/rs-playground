package com.mjstokely.rsplayground.simpleBlur;

import com.mjstokely.rsplayground.R;
import com.mjstokely.rsplayground.ResourceUtil;
import com.mjstokely.rsplayground.databinding.ADatabindingBlurBinding;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by mjordan on 11/20/17.
 */

public class SimpleBlurActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ADatabindingBlurBinding binding = DataBindingUtil.setContentView(this,
                                                                         R.layout.a_databinding_blur);
        binding.setData(new BlurData(ResourceUtil.fromResId(this,
                                                            R.raw.pod),
                                     20f));

    }
}
