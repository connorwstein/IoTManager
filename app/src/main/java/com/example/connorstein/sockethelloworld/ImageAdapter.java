package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by connorstein on 15-06-02.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    public Integer[] imageIDs={
            R.drawable.lights,R.drawable.thermometer
    };

    // Constructor
    public ImageAdapter(Context c){
        context = c;
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
        imageView.setImageResource(imageIDs[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
        return imageView;
    }

}
