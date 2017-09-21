package com.xhunmon.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by xhunmon on 2017/9/18.
 */

public class CarBoardView extends View {

    public static final String TAG = "CarBoardView";

    private int mOutRingColor;
    private int mInnerRingColor;
    private int mSpeedColor;
    private int mIndicatorColor;
    private float mOutRingRadius;
    private float mInnerRingRadius;
    private float mOutSpeedSize;
    private float mInnerSpeedSize;
    private float mSpeedUnitSize;
    private int mWidth = 0;
    private int mHeight = 0;
    private Paint mPaint;
    private RectF mOutRingRectF;
    private Rect mBound;
    private float mDensity;
    private Shader mShader;
    private float mStartMarkX;//刻度线的其实x坐标
    private float mStartMarkY;//刻度线的其实y坐标
    private Paint mTextPaint;
    private float mMarkAngle;
    private int mSpeed = 0;

    public CarBoardView(Context context) {
        this(context,null);
    }

    public CarBoardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CarBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public CarBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //屏幕密度，为了适配各种不同像素的手机
        mDensity = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CarBoardView,
                defStyleAttr, defStyleRes);
        mOutRingColor = a.getColor(R.styleable.CarBoardView_outRingColor, Color.BLUE);
        mInnerRingColor = a.getColor(R.styleable.CarBoardView_innerRingColor, Color.BLUE);
        mSpeedColor = a.getColor(R.styleable.CarBoardView_speedColor, Color.WHITE);
        mIndicatorColor = a.getColor(R.styleable.CarBoardView_indicatorColor, Color.RED);
        mOutRingRadius = a.getFloat(R.styleable.CarBoardView_outRingRadius, 100) * mDensity;
        mInnerRingRadius = a.getFloat(R.styleable.CarBoardView_innerRingRadius, 50) * mDensity;
        mOutSpeedSize = a.getFloat(R.styleable.CarBoardView_outSpeedSize, 13) * mDensity;
        mInnerSpeedSize = a.getFloat(R.styleable.CarBoardView_innerSpeedSize, 18) * mDensity;
        mSpeedUnitSize = a.getFloat(R.styleable.CarBoardView_speedUnitSize, 13) * mDensity;
        a.recycle();

        mHeight = mWidth = (int) (mOutRingRadius*2 + 10*mDensity);
        initTools();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mStartMarkX = (float) (mWidth/2 - mOutRingRadius*Math.sin(Math.PI*45/180) + 5*mDensity);
        mStartMarkY = (float) (mWidth/2 + mOutRingRadius*Math.cos(Math.PI*45/180) + 5*mDensity);
        mMarkAngle = 270 / 15f;
        Log.i(TAG, "onMeasure mWidth: " + mWidth + ",mHeight: " + mHeight + " ,mMarkAngle: "+mMarkAngle);
        setMeasuredDimension(mWidth, mHeight);//限定本view的宽高要一致
    }

    private void initTools() {
        Log.i(TAG,"initTools");
        mPaint = new Paint();
        mOutRingRectF = new RectF(5*mDensity,5*mDensity,mWidth-5*mDensity,mHeight-5*mDensity);//距离边界5*mDensity
        mShader = new RadialGradient(mWidth/2,mHeight/2,mInnerRingRadius, //三个数字分别表示，圆心的X、Y轴坐标以及半径
                new int[]{mInnerRingColor,0xFF53C0E7, 0xFF2062E8}, //这里是用来设置颜色值的，在这个int数组内可以有N组Color值
                new float[]{0.6f,0.8f,1f},Shader.TileMode.MIRROR);//0.6f,0.8f,1f透明度是指从里到外的渐变；而且注意要跟上面Color数据长度相等
        mTextPaint = new Paint();
        mBound = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1.画外圆弧
        mPaint.setStrokeWidth(2*mDensity);
        mPaint.setColor(mOutRingColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mOutRingRectF,120,300,false, mPaint);
        Log.i(TAG,"onDrawmOutRingColor: "+mOutRingColor);

        //2.画渐变内圆
        mPaint.setStrokeWidth(7*mDensity);
        mPaint.setShader(mShader);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mWidth/2,mHeight/2,mInnerRingRadius-5*mDensity, mPaint);//这里绘制出来一个比渐变圆略小的圆，并且覆盖到渐变圆上

        //3.画外圆刻度
        canvas.save(); //这时候保存的是画布没旋转之前的状态
        mPaint.reset();//重置画笔
        float degreeLength = 10*mDensity;
        mPaint.setColor(mOutRingColor);
        mPaint.setStrokeWidth(2*mDensity);
        mPaint.setAntiAlias(true);
        for(int i=0;i<16;i++){
            canvas.drawLine(mStartMarkX, mStartMarkY, mStartMarkX+degreeLength, mStartMarkY-degreeLength, mPaint);
            canvas.rotate(mMarkAngle, mWidth/2, mHeight/2);//旋转角度，x支点，y支点（就是环绕支点移动）
        }

        //4.画外圆时速（数字）
        canvas.restore();//还原状态(还原上一个save的状态),即将旋转过的画布重置
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setColor(mSpeedColor);
        mTextPaint.setTextSize(mOutSpeedSize);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        float x;
        float y;
        for(int i=0;i<16;i++){
            x = (float) (mWidth/2 - (mOutRingRadius-degreeLength)*Math.cos((Math.PI*45-Math.PI*mMarkAngle*i)/180));
            y = (float) (mWidth/2 + (mOutRingRadius-degreeLength)*Math.sin((Math.PI*45-Math.PI*mMarkAngle*i)/180));
            switch (i){
                case 0:case 1:case 2:
                    x = x+(i+1)*mDensity*(i==0?4:(i==1?3:2));
                    y = y+(i+1)*mDensity*4;
                    break;
                case 3:case 4:case 5:
                    x = x-i*mDensity/(i==3?-1:2);
                    y = y+i*mDensity*(i==5?3:4);
                    break;
                case 6:case 7:case 8:
                    x = x-i*mDensity*2;
                    y = (float) (y+i*mDensity*(i==6?3:(i==7?2:1.5)));
                    break;
                case 9:case 10:case 11:
                    x = x-i*mDensity*2;
                    y = (float) (y+i*mDensity/(i==9?1:(i==10?1.5:3)));
                    break;
                case 12:case 13:case 14:
                    x = (float) (x-i*mDensity*(i==12?2:1.5));
                    y = y-i*mDensity/2;
                    break;
                case 15:
                    x = x-i*mDensity;
                    y = y-i*mDensity/2;
                    break;
            }
            canvas.drawText(String.valueOf(30*i),x, y,mTextPaint);
        }


        //5.画内圆时速
        String text = String.valueOf(mSpeed);
        mTextPaint.setTextSize(mInnerSpeedSize);
        mTextPaint.setColor(mSpeedColor);
        mTextPaint.getTextBounds(text, 0, text.length(), mBound);
        float startX1 = mWidth/2 - mBound.width()/2;//控件宽度/2 - 文字宽度/2
        float startY1 = mHeight/2 + mBound.height()/2-mInnerSpeedSize;//控件高度/2 + 文字高度/2,绘制文字从文字左下角开始,因此"+"
        canvas.drawText(text, startX1, startY1, mTextPaint);// 绘制文字

        //6.画内圆速度单位
        String text2 = "km·h";
        mTextPaint.setTextSize(mSpeedUnitSize);
        mTextPaint.setColor(mOutRingColor);
        mTextPaint.getTextBounds(text2, 0, text2.length(), mBound);
        float startX2 = mWidth/2 - mBound.width()/2;//控件宽度/2 - 文字宽度/2
        float startY2 = mHeight/2 + mBound.height()/2;//控件高度/2 + 文字高度/2,绘制文字从文字左下角开始,因此"+"
        canvas.drawText(text2,startX2, startY2,mTextPaint);

        //7.画时速指针
        Path path = new Path();//这里创建Path对象为了保证每次绘制都是新一条path（并且显示出来只有一条）
        mPaint.setStrokeWidth(2*mDensity);
        mPaint.setColor(mIndicatorColor);
        mPaint.setStyle(Paint.Style.FILL);
        int m = 7;
        float startX = (float) (mWidth/2 - mOutRingRadius*Math.cos((Math.PI*45 - Math.PI*(mSpeed-m)/450*270)/180));
        float startY = (float) (mWidth/2 + mOutRingRadius*Math.sin((Math.PI*45 - Math.PI*(mSpeed-m)/450*270)/180));
        float endX1 = (float) (mWidth/2 - (mInnerRingRadius-m*mDensity)*Math.cos((Math.PI*45-Math.PI*(mSpeed+m)/450*270)/180));
        float endY1 = (float) (mWidth/2 + (mInnerRingRadius-m*mDensity)*Math.sin((Math.PI*45-Math.PI*(mSpeed+m)/450*270)/180));
        float endX2 = (float) (mWidth/2 - (mInnerRingRadius-m*mDensity)*Math.cos((Math.PI*45-Math.PI*(mSpeed-m)/450*270)/180));
        float endY2 = (float) (mWidth/2 + (mInnerRingRadius-m*mDensity)*Math.sin((Math.PI*45-Math.PI*(mSpeed-m)/450*270)/180));
        path.moveTo(startX, startY);// 此点为多边形的起点,指针的尖的一端
        path.lineTo(endX1, endY1);
        path.lineTo(endX2, endY2);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, mPaint);
    }

    public void setSpeed(int speed){
        mSpeed = speed;
        if(isMainThread())
            invalidate();//在UI线程中调用，进行重绘
        else
            postInvalidate();//在子线程中调用，进行重绘
    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
