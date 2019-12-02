package com.hy.certificatepreview.uilt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * Created by yue.huang on 2019/12/2.
 */
public class BitmapUtil {

    public static Bitmap getRotateBitmap(Bitmap b, Float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
    }

    /**
     * bitmap转byte数组
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getImage(String srcPath) {
        int ww = 720;
        int hh = 1280;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        //此时返回bm为空
        BitmapFactory.decodeFile(srcPath, newOpts);
        int  w = newOpts.outWidth;
        int h = newOpts.outHeight;
        newOpts.inJustDecodeBounds = false;


        //现在主流手机比较多是1280*720分辨率，所以高和宽我们设置为
        //        float hh = 1280f;//这里设置高度为1280f
        //        float ww = 720f;//这里设置宽度为720f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        //如果宽度大的话根据宽度固定大小缩放
        if (w > h && w > ww) {
            be = newOpts.outWidth / ww;
            //如果高度高的话根据宽度固定大小缩放
        } else if (w < h && h > hh) {
            be = newOpts.outHeight / hh;
        }
        if (be <= 0) {
            be = 1;
        }
        //设置缩放比例
        newOpts.inSampleSize = be;
        Bitmap bitmap = null;
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        while (true) {
            try {
                bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
                break;
            } catch (Exception e) {
                be *= 2;
                newOpts.inSampleSize = be;
            }

        }
        //压缩好比例大小后再进行质量压缩
        //return compressImage(bitmap);
        return bitmap;
    }
}
