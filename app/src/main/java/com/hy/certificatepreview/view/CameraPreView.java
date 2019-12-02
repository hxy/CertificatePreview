package com.hy.certificatepreview.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * @author yue.huang
 * 预览视图
 */

public class CameraPreView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final int AUTO_FOCUS = 1100;
    private SurfaceHolder mHolder;
    public Camera mCamera;
    public Context mContext;
    public WeakReference<Activity> mOuter;
    public MyHandler myHandler;

    public interface PicCallBack{
        /**
         * 照片返回方法
         * @param data
         */
        void onPictureTaken(byte[] data);
    }
    private PicCallBack picCallBack = null;
    private boolean isTakePic = false;

    public CameraPreView(Context context) {
        super(context);
        mContext = context;
        mOuter = new WeakReference<>((Activity) context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //打开相机
        if (mCamera == null) {
            mCamera = CameraSetting.getInstance(mContext.getApplicationContext()).open(0, mCamera);
        }
        //相机参数初始化
        CameraSetting.getInstance(mContext.getApplicationContext()).initCameraParamters(mCamera, mHolder, mOuter.get());
        mCamera.setPreviewCallback(this);
        if (myHandler == null) {
            myHandler = new MyHandler(this, mContext.getApplicationContext());
        }
        //自动对焦
        myHandler.sendEmptyMessageDelayed(AUTO_FOCUS, 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Message msg = new Message();
        msg.what = AUTO_FOCUS;
        myHandler.sendMessage(msg);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraSetting.getInstance(mContext.getApplicationContext()).closeCamera(mCamera);
        myHandler.removeMessages(AUTO_FOCUS);
        myHandler.removeCallbacksAndMessages(null);
        mCamera = null;
    }

    public void takePic(PicCallBack callBack){
        picCallBack = callBack;
        isTakePic = true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(isTakePic && picCallBack!=null){
            isTakePic = false;
            camera.stopPreview();
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            //ImageFormat.NV21
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
            // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 70, outputSteam);
            byte[] jpegData = outputSteam.toByteArray();
            picCallBack.onPictureTaken(jpegData);
        }
    }

    /**
     * 自定义 静态 handler 主要是为了防止内存泄漏
     */
    static class MyHandler extends Handler {
        // WeakReference to the outer class's instance.
        private WeakReference<CameraPreView> mOuterPreview;
        private Context appContext;

        public MyHandler(CameraPreView cameraPreView, Context mContext) {
            mOuterPreview = new WeakReference<CameraPreView>(cameraPreView);
            appContext = mContext;
        }

        @Override
        public void handleMessage(Message msg) {
            //自动对焦
            if (msg.what == AUTO_FOCUS) {
                if (mOuterPreview.get() != null && mOuterPreview.get().mCamera != null) {
                    CameraSetting.getInstance(appContext).autoFocus(mOuterPreview.get().mCamera);
                    mOuterPreview.get().myHandler.sendEmptyMessageDelayed(AUTO_FOCUS, 2500);
                }
            }
        }
    }
}
