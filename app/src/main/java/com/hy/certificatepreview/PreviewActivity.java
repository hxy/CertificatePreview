package com.hy.certificatepreview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hy.certificatepreview.view.MaskPreView;

import androidx.annotation.Nullable;

/**
 * Created by yue.huang on 2019/12/2.
 */
public class PreviewActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        MaskPreView preView = findViewById(R.id.preview);
        preView.setPictureCallBack(new MaskPreView.PictureCallBack() {
            @Override
            public void onPictureResult(String picPath) {
                Intent intent = new Intent();
                intent.putExtra("path",picPath);
                setResult(100,intent);
                finish();
            }
        });
    }
}
