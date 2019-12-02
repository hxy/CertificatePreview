package com.hy.certificatepreview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.hy.certificatepreview.R;

/**
 * Created by yue.huang on 2019/10/5.
 */
public class MaskView extends View {

    private int mViewWidth;   // 屏幕的宽
    private int mViewHeight;  // 屏幕的高

    private int mRectWidth, mRectHeight;
    //镂空矩形的坐标
    public float mRectLeft;
    public float mRectTop;
    public float mRectRight;
    public float mRectBottom;
    //镂空矩形的圆角弧度
    private int mRadius;

    public MaskView(Context context) {
        this(context, null);
    }

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
        //因为镂空矩形的布局需要整个view宽高，onGlobalLayout回调时view已经测量完成
        //所以在此监听中初始化整个MaskView的宽高
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewWidth = getWidth();
                mViewHeight = getHeight();
                if(mRectWidth>mViewWidth){
                    mRectWidth = mViewWidth;
                }
                if(mRectHeight>mViewHeight){
                    mRectHeight = mViewHeight;
                }
            }
        });
    }

    /**
     * 矩形属性
     * @param width   矩形宽度
     * @param height  矩形长度
     * @param mRadius 圆角
     */
    public void setRectAttributes(int width, int height, int mRadius) {
        this.mRectWidth = width;
        this.mRectHeight = height;
        this.mRadius = mRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        canvas.saveLayer(0, 0, mViewWidth, mViewHeight, null,
                Canvas.ALL_SAVE_FLAG);
        //画镂空矩形
        canvas.drawBitmap(makeDstRoundRect(), 0, 0, paint);
        //画灰色蒙层
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setAlpha(150);
        canvas.drawBitmap(makeSrcRect(), 0, 0, paint);
        //画提示文字
        paint.setXfermode(null);
        canvas.saveLayer(0, 0, mViewWidth, mViewHeight, null,
                Canvas.ALL_SAVE_FLAG);
        paint.setAlpha(255);
        Bitmap tipsBitmap = createPromptBitmap();
        canvas.drawBitmap(tipsBitmap, (mViewWidth -tipsBitmap.getWidth())/2, mRectTop-tipsBitmap.getHeight()*1.7f, paint);
        //画镂空矩形边框
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawRoundRect(new RectF(mRectLeft,mRectTop,mRectRight,mRectBottom),mRadius,mRadius,paint);
    }

    /**
     * 创建镂空圆角长方形形状
     * @return
     */
    private Bitmap makeDstRoundRect() {
        Bitmap bm = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvcs = new Canvas(bm);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        mRectLeft = (mViewWidth -mRectWidth)/2f;
        mRectTop = (mViewHeight -mRectHeight)/2f-200;
        mRectRight = mRectLeft+mRectWidth;
        mRectBottom = mRectTop+mRectHeight;
        canvcs.drawRoundRect(new RectF(mRectLeft, mRectTop, mRectRight,mRectBottom),mRadius,mRadius, paint);
        return bm;
    }

    /**
     * 创建遮罩层形状
     *
     * @return
     */
    private Bitmap makeSrcRect() {
        Bitmap bm = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvcs = new Canvas(bm);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvcs.drawRect(new RectF(0, 0, mViewWidth, mViewHeight), paint);
        return bm;
    }

    /**
     * 创建提示信息文字图片的bitmap
     *
     * @return
     */
    private Bitmap createPromptBitmap() {
        Bitmap transitionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bizbase_preview_tips);
        Bitmap bitmap = Bitmap.createScaledBitmap(transitionBitmap,(int)(getWidth()*0.48),(int)((getWidth()*0.48)*0.205),true);
        transitionBitmap.recycle();
        transitionBitmap = null;
        return bitmap;
    }
}
