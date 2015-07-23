package com.iotmanager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by connorstein on 15-07-22.
 */
public class CircleView extends View {

    private String color=null;
    private String background=null;
    private int radius=0;
    public CircleView(Context context){
        super(context);
    }
    public CircleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void setCircleColor(String color){
        this.color=color;
    }
    public void setCircleBackgroundColor(String background){
        this.background=background;
    }
    public void setCircleRadius(int radius){
        this.radius=radius;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = getWidth();
        int y = getHeight();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        if(background!=null){
            paint.setColor(Color.parseColor(background));
        }
        else{
            paint.setColor(Color.parseColor("#009688")); //default background color is gray same
        }
        canvas.drawPaint(paint);
        // Use Color.parseColor to define HTML colors
        if(color!=null){
            paint.setColor(Color.parseColor(color));
        }
        else{
            paint.setColor(Color.parseColor("#CD5C5C")); //default color is red
        }
        if(radius!=0) {
            canvas.drawCircle(x / 2, y / 2, radius, paint);
        }
        else{
            canvas.drawCircle(x / 2, y / 2, 100, paint); //default to 100dp
        }
    }
}
