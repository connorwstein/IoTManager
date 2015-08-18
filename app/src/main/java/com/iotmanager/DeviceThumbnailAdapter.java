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
 * Creates the device thumbnail icons (i.e. fills a gridview with a textview and imageview viewgroup)
 */
public class DeviceThumbnailAdapter extends BaseAdapter {
    private static final int ROUNDED_CORNERS_IMAGE_FACTOR=100;
    private static final int ROUNDED_CORNERS_IMAGE_FACTOR_HEATER_ICON=40; //heater icon is slightly different dimensions and requires a different rounding factor
    private static final String TAG="Connors Debug";

    private Context context;
    private Resources resources;
    public Integer[] imageIDs={
            R.drawable.lights,R.drawable.thermometer, R.drawable.camera, R.drawable.heater
    };
    private ArrayList<String> deviceNames;
    private ArrayList<String> deviceTypes;

    // Constructor
    public DeviceThumbnailAdapter(Context c,Resources r, ArrayList<Device> devices){
        this.context=c;
        this.resources=r;
        this.deviceNames=new ArrayList<>();
        this.deviceTypes=new ArrayList<>();
        for(Device device:devices){
            this.deviceNames.add(device.getName());
            this.deviceTypes.add(device.getType());
        }
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
        View v=null;

        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView==null) {
            //v = new View(context);
            v = inflater.inflate(R.layout.image_and_text, null);
            TextView textView = (TextView) v.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView) v.findViewById(R.id.grid_image);
            textView.setText(deviceNames.get(position));
            Bitmap bitmap = null;
            String type=deviceTypes.get(position);
            switch (type) {
                case "Lighting":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[0]),type);
                    break;
                case "Temperature":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[1]),type);
                    break;
                case "Camera":
                    bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[2]),type);
                    break;
                case "Heater":
                    //NOTE THIS WAS ONLY FOR THE DEMO (LIMITED HARDWARE AVAILABLE) to resolve just remove the if and keep the else case
                    //Dirtiest hack known to man
                    //Only one actual light with dimming hardware available
                    //Heaters are being represented by lights for the demo
                    //So in order to have lights that turn only on and off like the heater representations
                    //Just name the extra heaters as "lights" and display the icon for light
                    if(deviceNames.get(position).contains("Light")){
                        bitmap=getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[0]),"Lighting");
                    }
                    else {
                        bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), imageIDs[3]), type);

                    }
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
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, String type) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242; //Note color here does not seem to have an effect
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx;
        //Heater icon is slightly different size and requires different rounding
        if(type.equals("Heater")){
            roundPx = ROUNDED_CORNERS_IMAGE_FACTOR_HEATER_ICON;
        }
        else{
            roundPx = ROUNDED_CORNERS_IMAGE_FACTOR;

        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
