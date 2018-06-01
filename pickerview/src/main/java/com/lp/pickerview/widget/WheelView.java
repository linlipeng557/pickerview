package com.lp.pickerview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.util.IslamicCalendar;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;


import com.lp.pickerview.R;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {
    private float textSize = sp2px(getContext(), 15);//字体大小
    private float outTextSize = sp2px(getContext(), 13);//出界的文字大小度标准，即出界的字体必须必中间字体小
    private float textPadding = sp2px(getContext(), 3);//字体距离
    private float itemHeight = 0;//条目高
    private float itemHeightHalf = 0;//条目高的一般
    private float middleTopY = 0;//中间两线的上面那部分
    private float middleBottomY = 0;//中间两线的下面那部分
    private int measuredHeight = 0;// 布局高
    private int measuredWidth = 0;//布局宽
    private float scrollHeight = 0;//滑动总高
    private int offset = 2;//偏移量
    private boolean isEndSlide = false;//是否停止滑动
    private boolean autoScroll = false;
    private OverScroller mScroller;//用于实现惯性滚动
    //    private OverScroller mOverScroller;
    private Float middlebaseLine = Float.NaN; //
    private String text = null;
    private Paint centerTextPaint;//中间字体画笔
    private Paint outTextPaint;//出界字体画笔
    private int selectedPosition = 0;//选中的位置
    private int tmpSelectedPosition = -1;
    private int lastY = 0;
    private int eventAction = -0;//当前操作
    private ValueAnimator correctionScrollYValueAnimator;//修正动画
    private VelocityTracker velocityTracker;//用于计算滑动速度
    private OnSelectPickerListener mOnSelectPickerListener;
    private boolean setDataWillScrollYTo0 = false;
    private List list = new ArrayList<>();
    private List<TextMidbody> textMidbodies = new ArrayList<>();

    public WheelView(Context context) {
        super(context);
        init(context, null);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private float lineWidth = dp2px(getContext(), 0.75f);
    private boolean hasLine = true;
    private int lineColor = Color.parseColor("#80323232");
    private int mTextColor = Color.parseColor("#323232");
    private int outTextColor = Color.parseColor("#80323232");

    public void setText(String text) {
        if (text != this.text) {
            this.text = text;
            invalidate();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (null != attrs) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
            mTextColor = ta.getColor(R.styleable.WheelView_textColor, mTextColor);
            outTextColor = ta.getColor(R.styleable.WheelView_outTextColor, outTextColor);
            textSize = ta.getDimensionPixelSize(R.styleable.WheelView_textSize, (int) textSize);
            textPadding = ta.getDimensionPixelSize(R.styleable.WheelView_textPadding, (int) textPadding);
            offset = ta.getInteger(R.styleable.WheelView_offset, offset);
            text = ta.getString(R.styleable.WheelView_text);
            lineWidth = ta.getDimensionPixelSize(R.styleable.WheelView_lineWidth, (int) lineWidth);
            lineColor = ta.getColor(R.styleable.WheelView_lineColor, lineColor);
            hasLine = ta.getBoolean(R.styleable.WheelView_hasLine, hasLine);
            outTextSize = ta.getDimensionPixelSize(R.styleable.WheelView_outTextSize, (int) outTextSize);
            ta.recycle();  //注意回收
        }

        centerTextPaint = new Paint();
        centerTextPaint.setTextSize(textSize);
        centerTextPaint.setColor(mTextColor);
        centerTextPaint.setAntiAlias(true);
        outTextPaint = new Paint();
        mScroller = new OverScroller(context);
    }

    public void setOnSelectPickerListener(OnSelectPickerListener onSelectPickerListener) {
        mOnSelectPickerListener = onSelectPickerListener;
    }

    public Object getObject(int i) {
        if (list.isEmpty())
            return null;
        return list.get(i);
    }

    public void setDataWillScrollYTo0(boolean setDataWillScrollYTo0) {
        this.setDataWillScrollYTo0 = setDataWillScrollYTo0;
    }

    public int getCurrentPosition() {
        return selectedPosition;
    }

    public void setCurrentPosition(int position) {
        setCurrentPosition(position, false);
    }

    /**
     * @param position
     * @param animation
     */
    public void setCurrentPosition(int position, boolean animation) {
        setSelectedPosition(position);
        final int toY = (int) (position * itemHeight);
        if (animation) {
            stopAutoScroll();
            autoScroll = true;
            smoothScroll(getScrollY(), toY, 200);
        } else {
            scrollTo(0, toY);
        }
    }


    public void setList(@Nullable List list) {
        if (list.isEmpty()) {
            return;
        }
        this.list.clear();
        this.list.addAll(list);
        tmpSelectedPosition = -1;
        selectedPosition = 0;
        calculateTextBaseLine();//计算字体的基线
        if (setDataWillScrollYTo0) {
            scrollTo(0, 0);
        } else {
            if (getScrollY() > scrollHeight)
                scrollTo(0, (int) scrollHeight);
        }
        invalidate();
        setSelectedPosition(0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        //测量总高度
        measureHeight();
        setMeasuredDimension(measuredWidth, measuredHeight);
        calculateTextBaseLine();//计算字体的基线
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int top = (int) (getScrollY() - itemHeight);
        int bottom = (int) (getScrollY() + measuredHeight + itemHeight);
        int dMiddleTopY = (int) (getScrollY() + middleTopY);
        int dMiddleBottomY = (int) (getScrollY() + middleBottomY);

        outTextPaint.reset();
        outTextPaint.setAntiAlias(true);
        outTextPaint.setTextSize(outTextSize);
        outTextPaint.setColor(outTextColor);

        for (TextMidbody textMidbody : textMidbodies) {
            float baseLine = textMidbody.baseLine;
            if (baseLine >= top && baseLine <= bottom) {
                String text = textMidbody.getText();
                if (baseLine >= top && baseLine < dMiddleBottomY) {
                    //上面区域
                    canvas.save();
                    canvas.clipRect(0, top, measuredWidth, dMiddleTopY);
                    canvas.drawText(text, (measuredWidth - measureTextWidth(text, outTextPaint)) / 2, baseLine, outTextPaint);
                    canvas.restore();
                }
                if (baseLine >= dMiddleTopY && baseLine < bottom) {
//                    //下面区域
                    canvas.save();
                    canvas.clipRect(0, dMiddleBottomY, measuredWidth, bottom);
                    canvas.drawText(text, (measuredWidth - measureTextWidth(text, outTextPaint)) / 2, baseLine, outTextPaint);
                    canvas.restore();
                }
                if (baseLine >= dMiddleTopY - itemHeight && baseLine < dMiddleBottomY + itemHeight) {
                    //中间区域
                    canvas.save();
                    canvas.clipRect(0, dMiddleTopY, measuredWidth, dMiddleBottomY);
                    canvas.drawText(text, (measuredWidth - measureTextWidth(text, centerTextPaint)) / 2, baseLine, centerTextPaint);
                    canvas.restore();
                }

            }
        }
        //写后面那块字
        if (text != null) {
            if (middlebaseLine.isNaN()) {
                middlebaseLine = getBaseline(centerTextPaint);
            }
            canvas.drawText(text, measuredWidth - measureTextWidth(text, centerTextPaint) - textPadding * 2,
                    getScrollY() + measuredHeight / 2 + middlebaseLine, centerTextPaint);
        }
        //画线
        if (hasLine) {
            outTextPaint.reset();
            outTextPaint.setAntiAlias(true);
            outTextPaint.setColor(lineColor);
            outTextPaint.setStrokeWidth(lineWidth);
            canvas.drawLine(0, dMiddleTopY, measuredWidth, dMiddleTopY, outTextPaint);
            canvas.drawLine(0, dMiddleBottomY, measuredWidth, dMiddleBottomY, outTextPaint);
        }
    }

    /**
     * 测量高度（item高度，view高度）
     */
    private void measureHeight() {
        itemHeight = getFontHeight(textSize) + textPadding * 2;
        itemHeightHalf = itemHeight / 2;
        measuredHeight = (int) (itemHeight * (offset * 2 + 1));
        middleTopY = itemHeight * offset;
        middleBottomY = itemHeight * (offset + 1);
    }

    /**
     * 测量滑动高度
     */
    private void measureScrollHeight() {
        scrollHeight = (list == null || list.isEmpty() ? 0 : list.size() - 1) * itemHeight;//可以滑动的总高度
    }

    /**
     * 计算字体基线
     */
    private void calculateTextBaseLine() {
        textMidbodies.clear();
        measureScrollHeight();//测量滑动高度
        float tmpTotalY = itemHeight * offset;
        if (middlebaseLine.isNaN()) {
            middlebaseLine = getBaseline(centerTextPaint);
        }
        for (int i = 0; i < list.size(); i++) {
            tmpTotalY += itemHeight;
            textMidbodies.add(new TextMidbody(list.get(i), (tmpTotalY - itemHeightHalf) + middlebaseLine));
        }
    }

    /**
     * 计算最大length的Text的宽高度
     */
    private int measureTextWidth(String s, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(s, 0, s.length(), rect);
        return rect.width();
    }

    /**
     * 获取字体高度
     *
     * @param fontSize
     * @return
     */
    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    /**
     * 计算绘制文字时的基线到中轴线的距离
     *
     * @param p
     * @return 基线和centerY的距离
     */
    public static float getBaseline(Paint p) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        return (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

    private class TextMidbody {
        Object mObject;
        float baseLine;

        public TextMidbody(Object o, float baseLine) {
            this.mObject = o;
            this.baseLine = baseLine;
        }

        public String getText() {
            if (mObject instanceof String) {
                return mObject.toString();
            } else if (mObject instanceof PickerData) {
                return ((PickerData) mObject).getText();
            } else {
                throw new RuntimeException("数据源类型只能为 String 或 PickerData");
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("dddddq", String.format(this.toString() + "  onTouchEvent x: %f ,y: %f", event.getX(), event.getY()));
        int y = (int) event.getY();
        eventAction = event.getAction();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                autoScroll = false;
                stopAutoScroll();
                //记录触摸点的坐标
                lastY = y;
                velocityTracker = VelocityTracker.obtain();
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000);
                int offsetY = lastY - y;
                scrollBy(0, offsetY);
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                int yVelocity = (int) velocityTracker.getYVelocity();
                smoothFling(yVelocity);
            case MotionEvent.ACTION_CANCEL:
                //回收
                velocityTracker.clear();
                velocityTracker.recycle();
                velocityTracker = null;
                break;

        }
        return true;
    }

    @Override
    public void scrollTo(int x, int y) {
        int scrollToY = y;
        if (scrollToY >= scrollHeight) {
            scrollToY = (int) scrollHeight;
            if (!isEndSlide) {
                isEndSlide = true;
                setSelectedPosition(list.size() - 1);
            }
        } else if (scrollToY <= 0) {
            scrollToY = 0;
            if (!isEndSlide) {
                isEndSlide = true;
                setSelectedPosition(0);
            }
        } else {
            isEndSlide = false;
        }
        super.scrollTo(x, scrollToY);
        invalidate();
    }

    /**
     * 设置当前选中项
     *
     * @param position
     */
    private void setSelectedPosition(int position) {
        if (tmpSelectedPosition != position) {
            this.selectedPosition = position;
            this.tmpSelectedPosition = selectedPosition;
            if (mOnSelectPickerListener != null) {
                mOnSelectPickerListener.onSelect(this, selectedPosition, list.get(selectedPosition));
            }
        }
    }

    /**
     * 修正滑动距离
     */
    private void correctionScrollY() {
        if (eventAction == 0) {
            return;
        }
        eventAction = 0;
        int y = getScrollY();
        int s = (int) (y % itemHeight);
        int index = (int) (y / itemHeight);
        if (s >= itemHeightHalf) {
            //当前
            ++index;
        } else if (s < itemHeightHalf) {
            //下一个
        }
        int endY = (int) (index * itemHeight);
        int dy = endY - y;
        smoothScroll(y, endY, Math.abs(dy * 6));
        setSelectedPosition(index);
    }

    /**
     * 移动距离
     *
     * @param startY
     * @param endY
     */
    private void smoothScroll(int startY, int endY, int duration) {
        if (correctionScrollYValueAnimator == null) {
            correctionScrollYValueAnimator = ValueAnimator.ofInt(startY, endY);
            correctionScrollYValueAnimator.setDuration(duration);
            correctionScrollYValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    scrollTo(0, i);
                }
            });
            correctionScrollYValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    autoScroll = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    autoScroll = false;
                }
            });
        } else {
            correctionScrollYValueAnimator.cancel();
            correctionScrollYValueAnimator.setIntValues(startY, endY);
            correctionScrollYValueAnimator.setDuration(duration);
        }
        correctionScrollYValueAnimator.start();
    }

    /**
     * 停止滑动（惯性滚动或修正滚动）
     */
    private void stopAutoScroll() {
        if (correctionScrollYValueAnimator != null && correctionScrollYValueAnimator.isRunning()) {
            correctionScrollYValueAnimator.cancel();
        }
        mScroller.abortAnimation();
    }

    public void smoothFling(int dy) {
        mScroller.fling(0, getScrollY(), 0, -dy,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        //重新绘制View
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (eventAction == 0) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            return;
        }
        boolean flag = mScroller.computeScrollOffset();
        if (flag == false) {
            //递归终止条件:滑动结束
            if (eventAction == MotionEvent.ACTION_UP) {
                if (!isEndSlide) {
                    isEndSlide = true;
                    if (!autoScroll)
                        correctionScrollY();
                }
            }
            return;
        } else {
            int scrollY = mScroller.getCurrY();
            if (scrollY >= scrollHeight) {
                scrollY = (int) scrollHeight;
            } else if (scrollY <= 0) {
                scrollY = 0;
            }
            scrollTo(0, scrollY);
        }
    }


    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, final float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public interface OnSelectPickerListener {
        void onSelect(View view, int poistion, Object o);
    }

    public interface PickerData {
        String getText();
    }


}
