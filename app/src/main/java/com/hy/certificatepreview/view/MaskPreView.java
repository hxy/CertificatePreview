package com.hy.certificatepreview.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hy.certificatepreview.R;
import com.hy.certificatepreview.uilt.BitmapUtil;

import java.io.File;
import java.io.FileOutputStream;


/**
 * @author yue.huang
 */
public class MaskPreView extends RelativeLayout {
    public interface PictureCallBack{
        /**
         * 最终的图片
         * @param picPath 图片路径
         */
        void onPictureResult(String picPath);
    }
    private CameraPreView cameraPreView;
    private PictureCallBack pictureCallBack;
    private MaskView maskView;
    private Context mContext;
    public MaskPreView(Context context) {
        this(context,null);
    }

    public MaskPreView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MaskPreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initPreView(mContext,getWidth(),getHeight());
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    /**
     * 初始化预览视图
     * @param context
     */
    private void initPreView(final Context context,int wholeWidth,int wholeHeight){
        final FrameLayout cameraContainer = new FrameLayout(context);
        //相机预览承接view
        cameraContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        cameraContainer.setBackgroundColor(0xff000000);
        addView(cameraContainer);
        //创建相机预览并添加到承接view中
        cameraPreView = new CameraPreView(context);
        cameraContainer.addView(cameraPreView);
        //添加镂空蒙层
        maskView = new MaskView(context);
        maskView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int rectWidth = (int)(wholeWidth*0.92);
        int rectHeight = (int)(rectWidth*0.625);
        maskView.setRectAttributes(rectWidth,rectHeight,80);
        addView(maskView);
        //添加底部拍照button
        final ImageView takePicBtn = new ImageView(context);
        takePicBtn.setScaleType(ImageView.ScaleType.FIT_XY);
        takePicBtn.setImageResource(R.drawable.bizbase_preview_take_btn);
        int takePicBtnSize = (int)((wholeWidth<wholeHeight?wholeWidth:wholeHeight)*0.20);
        LayoutParams rlp=new LayoutParams(takePicBtnSize,takePicBtnSize);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.bottomMargin = (int)(wholeHeight*0.0615);
        takePicBtn.setLayoutParams(rlp);
        addView(takePicBtn);
        //添加左上角关闭图标
        ImageView closeBtn = new ImageView(context);
        closeBtn.setScaleType(ImageView.ScaleType.FIT_XY);
        closeBtn.setImageResource(R.drawable.bizbase_preview_close);
        int closeBtnSize = (int)((wholeWidth<wholeHeight?wholeWidth:wholeHeight)*0.05);
        LayoutParams closeRlp=new LayoutParams(closeBtnSize,closeBtnSize);
        closeRlp.leftMargin = closeBtnSize;
        closeRlp.topMargin = closeBtnSize;
        closeBtn.setLayoutParams(closeRlp);
        addView(closeBtn);
        //添加点击事件
        takePicBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreView.takePic(picCallBack);
                takePicBtn.setEnabled(false);
            }
        });
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)context).finish();
            }
        });
    }

    public void setPictureCallBack(PictureCallBack pictureCallBack){
        this.pictureCallBack = pictureCallBack;
    }

    private CameraPreView.PicCallBack picCallBack = new CameraPreView.PicCallBack() {
        @Override
        public void onPictureTaken(byte[] data) {
            Bitmap targetBitmap = getRectSectionPic(data,(int)maskView.mRectLeft,(int)maskView.mRectTop,(int)maskView.mRectRight,(int)maskView.mRectBottom);
            pictureCallBack.onPictureResult(savePortionPic(targetBitmap));
        }
    };

    /**
     * 获取矩形部分图片
     * @param data
     * @return
     */
    private Bitmap getRectSectionPic(byte[] data,int left,int top,int right,int bottom){
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);
        Bitmap rotatedBitmap = BitmapUtil.getRotateBitmap(originalBitmap,(float)info.orientation);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,maskView.getWidth(),maskView.getHeight(),true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap,left,top,right-left,bottom-top);
        originalBitmap.recycle();
        originalBitmap = null;
        rotatedBitmap.recycle();
        rotatedBitmap = null;
        scaledBitmap.recycle();
        scaledBitmap = null;
        return croppedBitmap;
    }


    /**
     * 保存最终图片
     * @param bitmap
     * @return
     */
    private String savePortionPic(Bitmap bitmap){
        File file = new File(mContext.getCacheDir().getAbsolutePath()+"/"+System.currentTimeMillis()+".jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(BitmapUtil.bitmapToBytes(bitmap));
            fileOutputStream.flush();
            fileOutputStream.close();
            return file.getAbsolutePath();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
