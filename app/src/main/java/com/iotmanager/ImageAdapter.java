package com.iotmanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


/**
 * Created by connorstein on 15-06-02.
 */
public class ImageAdapter extends BaseAdapter {
    private static final int ROUNDED_CORNERS_IMAGE_FACTOR=100;
    private static final String TAG="Connors Debug";

    private Context context;
    private Resources resources;
    public Integer[] imageIDs={
            R.drawable.lights,R.drawable.thermometer, R.drawable.camera
    };

    // Constructor
    public ImageAdapter(Context c,Resources r){
        context=c;
        resources=r;
    }

    @Override
    public int getCount() {
        return imageIDs.length;
        }

    @Override
    public Object getItem(int position) {
        return imageIDs[position];
        }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        Bitmap bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[position]));
        imageView.setImageBitmap(bitmap);
        imageView.setLayoutParams(new GridView.LayoutParams(290, 290));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return imageView;
    }

    //Rounded corner method from http://ruibm.com/2009/06/16/rounded-corner-bitmaps-on-android/
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242; //Note color here does not seem to have an effect
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = ROUNDED_CORNERS_IMAGE_FACTOR;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
