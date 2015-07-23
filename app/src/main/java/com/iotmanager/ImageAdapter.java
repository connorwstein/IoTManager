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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


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
    private ArrayList<String> deviceNames;
    private ArrayList<String> deviceTypes;

    // Constructor
    public ImageAdapter(Context c,Resources r, ArrayList<String> deviceNames,ArrayList<String> deviceTypes){
        context=c;
        resources=r;
        this.deviceNames=deviceNames;
        this.deviceTypes=deviceTypes;
    }

    @Override
    public int getCount() {
        return deviceNames.size();
        }

    @Override
    public Object getItem(int position) {
        return null;
        }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;

        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView==null) {
            //v = new View(context);
            v = inflater.inflate(R.layout.image_and_text, null);
            TextView textView = (TextView) v.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView) v.findViewById(R.id.grid_image);
            textView.setText(deviceNames.get(position));
            Bitmap bitmap = null;
            switch (deviceTypes.get(position)) {
                case "Lighting":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[0]));
                    break;
                case "Temperature":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[1]));
                    break;
                case "Camera":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[2]));
                    break;
            }
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        else{
            v=convertView;
        }
        return v;
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
