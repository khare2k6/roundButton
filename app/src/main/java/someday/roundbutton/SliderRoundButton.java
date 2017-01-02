package someday.roundbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ankit on 27/12/16.
 */

public class SliderRoundButton extends View {
    private final int mButtonOneBoundryColor,mButtonOneFillColorSelected,mButtonOneFillColorNormal,
            mButtonOneFontColorNormal,mButtonOneFontColorSelected,mButtonTwoFillColor,mButtonTwoFontColor,mHolderLineColor;
    private Context mContext;
    private Paint mPaintLine;
    float mLeftLineStartX,mLeftLineStartY,mRightLineStartX, mRightLineStartY,mLeftLineEndX,mLeftLineEndY,mRightLineEndX,mRightLineEndY,mHolderLineStrokeWidth;
    RectF mHolderArc;
    private RoundButton.BUTTON_STATE mContentButtonState = RoundButton.BUTTON_STATE.NORMAL;
    /**
     * There are two round button in this slider view.
     * Button 1 hosting the main content and sub content
     * Button 2 the end point, the start button, till where button 1 needs to be dragged
     */
    private RoundButton mButtonContent,mButtonStart;
    /**
     * Slider radius will be radius of both the round buttons
     */
    private float mCenterX, mCenterY, mSliderRadius;
    /**
     * How long user needs to drag button 1 to reach button two.
     */
    private float mSliderDistance;
    /**
     * Main content and sub content of the slider view refers to main content and sub content
     * of button 1
     */
    private String mMainContent, mSubContent;
    private GestureDetector mDetector;

    public SliderRoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SliderView,
                0, 0);
        mButtonContent = new RoundButton(context);
        mButtonStart = new RoundButton(context);

        try {
            mSliderRadius = a.getFloat(R.styleable.SliderView_radius, 100);
            mSliderDistance = a.getFloat(R.styleable.SliderView_slidingDistance, 2 * mSliderRadius);
            //button one details
            mMainContent = a.getString(R.styleable.SliderView_mainTextSliderView);
            mSubContent = a.getString(R.styleable.SliderView_subTextSliderView);
            mButtonOneBoundryColor = a.getColor(R.styleable.SliderView_buttonOneBoundryColor, Color.BLACK);
            mButtonOneFillColorNormal = a.getColor(R.styleable.SliderView_buttonOneFillColorNormal, Color.GRAY);
            mButtonOneFillColorSelected = a.getColor(R.styleable.SliderView_buttonOneFillColorSelected, Color.BLUE);
            mButtonOneFontColorNormal = a.getColor(R.styleable.SliderView_buttonOneFontColorNormal, Color.BLACK);
            mButtonOneFontColorSelected= a.getColor(R.styleable.SliderView_buttonOneFontColorSelected, Color.WHITE);

            mButtonTwoFillColor= a.getColor(R.styleable.SliderView_buttonTwoFillColorNormal, Color.GREEN);
            mButtonTwoFontColor= a.getColor(R.styleable.SliderView_buttonTwoFontColorNormal, Color.BLACK);

            mHolderLineColor = a.getColor(R.styleable.SliderView_holderLineColor, Color.GREEN);
            mHolderLineStrokeWidth = a.getFloat(R.styleable.SliderView_holderLineWidth, (float) 2.0);

            mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintLine.setStyle(Paint.Style.STROKE);
            mPaintLine.setColor(mHolderLineColor);
            mPaintLine.setStrokeWidth(mHolderLineStrokeWidth);
            mDetector = new GestureDetector(context, new GestureDetection(this));

        }finally {
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        initRoundButtons();

        mButtonContent.onSizeChanged(w, h, oldw, oldh);
        mButtonStart.onSizeChanged(w, h, oldw, oldh);

        mButtonContent.recenter(0,-mSliderDistance/2);
        mButtonStart.recenter(0,mSliderDistance/2);

        mLeftLineStartX = mCenterX - mSliderRadius;
        mLeftLineStartY = mCenterY - mSliderDistance / 2;
        mLeftLineEndX = mCenterX - mSliderRadius;
        mLeftLineEndY = mCenterY + mSliderDistance / 2;

        mRightLineStartX = mCenterX + mSliderRadius;
        mRightLineStartY = mCenterY - mSliderDistance / 2;
        mRightLineEndX = mCenterX + mSliderRadius;
        mRightLineEndY = mCenterY + mSliderDistance / 2;
        mHolderArc =  new RectF(mCenterX - mSliderRadius, mCenterY + mSliderDistance/2 -mSliderRadius,
                mCenterX + mSliderRadius, mCenterY + mSliderRadius+ mSliderDistance/2);
    }

    private void initRoundButtons() {
        //set radius for button one
        mButtonContent.setRadius(mSliderRadius);
        //set content of button one
        mButtonContent.setMainText(mMainContent);
        mButtonContent.setSubText(mSubContent);
        //set center for button one's circle
        mButtonContent.setCenterX(mCenterX);
        mButtonContent.setCenterY(mCenterY - mSliderDistance / 2);
        //set content type of button one
        mButtonContent.setContentType(RoundButton.MAIN_CONTENT_TYPE.SINGLE_CHARACTER);
        //set different colors for button one
        mButtonContent.setButtonOutlineColor(mButtonOneBoundryColor);
        mButtonContent.setButtonFillColorNormal(mButtonOneFillColorNormal);
        mButtonContent.setButtonFillColorSelected(mButtonOneFillColorSelected);
        mButtonContent.setMainTextColorNormal(mButtonOneFontColorNormal);
        mButtonContent.setMainTextColorSelected(mButtonOneFontColorSelected);
        mButtonContent.setSubTextColorNormal(mButtonOneFontColorNormal);
        mButtonContent.setSubTextColorSelected(mButtonOneFontColorSelected);

        mButtonContent.init();


        mButtonStart.setRadius(mSliderRadius);
        mButtonStart.setContentType(RoundButton.MAIN_CONTENT_TYPE.TEXT);
        mButtonStart.setMainText("Start");
        mButtonStart.setSubText("min");
        mButtonStart.setCenterX(mCenterX);
        mButtonStart.setCenterY(mCenterY + mSliderDistance / 2);
        mButtonStart.setButtonOutlineColor(mButtonOneBoundryColor);
        mButtonStart.setButtonFillColorNormal(mButtonTwoFillColor);
        mButtonStart.setMainTextColorNormal(mButtonTwoFontColor);
        mButtonStart.init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mButtonContent.draw(canvas);
        if (mContentButtonState.equals(RoundButton.BUTTON_STATE.SELECTED)) {
            mButtonStart.draw(canvas);
            canvas.drawLine(mLeftLineStartX, mLeftLineStartY, mLeftLineEndX, mLeftLineEndY, mPaintLine);
            canvas.drawLine(mRightLineStartX ,mRightLineStartY ,mRightLineEndX,mRightLineEndY ,mPaintLine);
            canvas.drawArc(mHolderArc, 0, 180, false, mPaintLine);
        }


    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mContentButtonState = RoundButton.BUTTON_STATE.NORMAL;
            invalidate();
        }
        return mDetector.onTouchEvent(event);
    }
    /**
     * Gesture detection on slider view
     */
    private class GestureDetection extends GestureDetector.SimpleOnGestureListener{
        private final String TAG = this.getClass().getSimpleName();
        private SliderRoundButton mSliderView;

        protected GestureDetection(SliderRoundButton slider) {
            this.mSliderView = slider;
        }

        private boolean isInButtonOneRange(MotionEvent event) {
            float topEndOfButtonOne = mCenterY - mSliderDistance / 2 - mSliderRadius;
            float bottomEndOfButtonOne = mCenterY - mSliderDistance / 2 + mSliderRadius;
             return (mLeftLineStartX < event.getX() && event.getX() < mRightLineEndX &&
                    topEndOfButtonOne < event.getY() && event.getY() < bottomEndOfButtonOne);
        }
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown called");
            Log.d(TAG, "isInButtonOneRange:" + isInButtonOneRange(e));
//            mButtonStart.setVisibility(INVISIBLE);
            if (isInButtonOneRange(e)) {
                mContentButtonState = RoundButton.BUTTON_STATE.SELECTED;
                mSliderView.invalidate();
            }

            return true;

        }

//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            mContentButtonState = RoundButton.BUTTON_STATE.NORMAL;
//            mSliderView.invalidate();
//            return super.onSingleTapConfirmed(e);
//        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "event2.getY():" + e2.getY() + " distance y:" + distanceY);
            mButtonContent.recenter(0,-distanceY);
            return true;
        }
    }
}
