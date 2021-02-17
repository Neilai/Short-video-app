package me.laijingzhi.short_video_app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class SurfaceView169 extends SurfaceView {
    public SurfaceView169(Context context) {
        this(context, null, 0);
    }

    public SurfaceView169(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceView169(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = width / 9 * 16;
        setMeasuredDimension(width, height);
    }
}
