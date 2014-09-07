package com.selbie.xkcdclock;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ClockView extends View
{

    static public final String TAG = ClockView.class.getSimpleName();

    Context _context;

    Bitmap _innerCircle;
    Bitmap _outerCircle;
    Paint _paint;
    boolean _firstframe;
    int _canvaswidth;
    int _canvasheight;
    int _canvasCenterX;
    int _canvasCenterY;
    int _bmpWidth;
    int _bmpHeight;
    float _scalefactor;

    float _refX;
    float _refY;
    float _activeRotation = 0;
    float _persistentRotation = 0;

    public ClockView(Context context)
    {
        super(context);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        _outerCircle = BitmapFactory.decodeResource(context.getResources(), R.drawable.outer, options);
        _innerCircle = BitmapFactory.decodeResource(context.getResources(), R.drawable.inner, options);
        _paint = new Paint();
        _paint.setColor(0xff000000);
        _paint.setTextSize(20);

        _firstframe = true;

        this.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                // TODO Auto-generated method stub
                return ClockView.this.onTouch(arg1);
            }
        });

    }

    boolean onTouch(MotionEvent event)
    {
        int action = event.getAction();
        int actionmasked = event.getActionMasked();
        Log.d(TAG, "action = " + action);
        Log.d(TAG, "actionmasked = " + actionmasked);

        if (_firstframe == true)
        {
            return false;
        }

        if (actionmasked == MotionEvent.ACTION_DOWN)
        {
            _refX = event.getX();
            _refY = event.getY();
            return true;
        }
        else if (actionmasked == MotionEvent.ACTION_MOVE)
        {

            float x = event.getX() - _canvasCenterX;
            float y = _canvasCenterY - event.getY();

            if ((x != 0) && (y != 0))
            {
                double angleB = ComputeAngle(x, y);

                x = _refX - _canvasCenterX;
                y = _canvasCenterY - _refY;
                double angleA = ComputeAngle(x, y);

                _activeRotation = (float) (angleA - angleB);

                Log.d(TAG, "_activeRotation = " + _activeRotation);

                this.invalidate();
            }
        }
        else if ((actionmasked == MotionEvent.ACTION_UP) || (actionmasked == MotionEvent.ACTION_CANCEL))
        {
            _persistentRotation += _activeRotation;
            
            while (_persistentRotation > 360)
            {
                _persistentRotation -= 360;
            }
            
            while (_persistentRotation < 0)
            {
                _persistentRotation += 360;
            }

            Log.d(TAG, "persistentRotation = " + _persistentRotation);

            _activeRotation = 0;
        }

        return true;
    }

    double ComputeAngle(float x, float y)
    {
        final double RADS_TO_DEGREES = 360 / (java.lang.Math.PI * 2);
        double result = java.lang.Math.atan2(y, x) * RADS_TO_DEGREES;

        if (result < 0)
        {
            result = 360 + result;
        }

        return result;
    }

    void DrawBitmapInCenter(Bitmap bmp, float scale, float rotation, Canvas canvas)
    {
        canvas.save();
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.scale(scale, scale);
        canvas.rotate(rotation);
        canvas.translate(-bmp.getWidth() / 2, -bmp.getHeight() / 2);
        canvas.drawBitmap(bmp, 0, 0, _paint);
        canvas.restore();
    }

    float ComputeTimeRotationOuter()
    {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        int totalminutes = hour * 60 + minute;
        float totaldegrees = totalminutes / 4.0f;

        return totaldegrees;
    }

    float ComputeTimeRotation()
    {
        return 8 * 15; // Every hour is 15 degrees. 8 hours is Seattle time
    }

    @Override
    public void onDraw(Canvas canvas)
    {

        if (_firstframe)
        {
            _firstframe = false;

            // figure out how to center the circle into the canvas with margins
            // about 10px

            _canvaswidth = canvas.getWidth();
            _canvasheight = canvas.getHeight();
            _canvasCenterX = _canvaswidth / 2;
            _canvasCenterY = _canvasheight / 2;

            int target = java.lang.Math.min(_canvaswidth, _canvasheight) - 20;

            _bmpWidth = _outerCircle.getWidth();
            _bmpHeight = _outerCircle.getHeight();

            _scalefactor = ((float) target) / (float) _bmpWidth;

            Log.d(TAG, "_canvaswidth=" + _canvaswidth);
            Log.d(TAG, "_canvasheight=" + _canvasheight);
            Log.d(TAG, "_bmpWidth=" + _bmpWidth);
            Log.d(TAG, "_bmpHeight=" + _bmpHeight);
            Log.d(TAG, "_scalefactor=" + _scalefactor);
            Log.d(TAG, "canvas.getDensity()=" + canvas.getDensity());
            Log.d(TAG, "_outerCircle.getDensity()=" + _outerCircle.getDensity());
        }

        DrawBitmapInCenter(_outerCircle, _scalefactor, _activeRotation + _persistentRotation + ComputeTimeRotationOuter(), canvas);
        DrawBitmapInCenter(_innerCircle, _scalefactor, _activeRotation + _persistentRotation + ComputeTimeRotation(), canvas);

        _paint.setColor(0xffff00ff);
        canvas.drawLine(0, 0, _canvaswidth, _canvasheight, _paint);
        canvas.drawLine(_canvaswidth, 0, 0, _canvasheight, _paint);
        canvas.drawLine(_canvasCenterX, _canvasCenterY - 10, _canvasCenterX, _canvasCenterY + 10, _paint);

    }

}
