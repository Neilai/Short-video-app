package me.laijingzhi.short_video_app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class SurfaceView43 extends SurfaceView {
    public SurfaceView43(Context context) {
        this(context, null, 0);
    }

    public SurfaceView43(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceView43(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = width / 3 * 4;
        setMeasuredDimension(width, height);
    }
}
