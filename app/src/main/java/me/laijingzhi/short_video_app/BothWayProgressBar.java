package me.laijingzhi.short_video_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BothWayProgressBar extends View {
    private static final String TAG = "BothWayProgressBar";
    //正在录制的画笔
    private Paint mRecordPaint;
    //是否显示
    private int mVisibility;
    // 当前进度
    private int progress;
    //进度条结束的监听
    private OnProgressEndListener mOnProgressEndListener;

    public BothWayProgressBar(Context context) {
        super(context, null);
    }

    public BothWayProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mVisibility = INVISIBLE;
        mRecordPaint = new Paint();
        mRecordPaint.setColor(Color.GREEN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mVisibility == View.VISIBLE) {
            int height = getHeight();
            int width = getWidth();
            int mid = width / 2;

            //画出进度条
            if (progress < mid){
                canvas.drawRect(progress, 0, width - progress, height, mRecordPaint);
            }
            else {
                if (mOnProgressEndListener != null) {
                    mOnProgressEndListener.onProgressEndListener();
                }
            }
        }
        else {
            canvas.drawColor(Color.argb(0, 0, 0, 0));
        }
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    /**
     * 重写是否可见方法
     * @param visibility
     */
    @Override
    public void setVisibility(int visibility) {
        mVisibility = visibility;
        //重新绘制
        invalidate();
    }

    /**
     * 当进度条结束后的 监听
     * @param onProgressEndListener
     */
    public void setOnProgressEndListener(OnProgressEndListener onProgressEndListener) {
        mOnProgressEndListener = onProgressEndListener;
    }


    public interface OnProgressEndListener{
        void onProgressEndListener();
    }

}
