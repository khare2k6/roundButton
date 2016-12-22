package someday.roundbutton;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ankit on 19/11/16.
 */

public class RoundButton extends View {
    private static final int WIDTH_OF_MAIN_TEXT = 50;
    private int GAP_FROM_TOP_EDGE_OF_CIRCLE = 40;
    private int GAP_FROM_MAIN_TEXT_BOTTOM_EDGE = 10;
    private final String TAG = RoundButton.class.getSimpleName();
    //paint object for drawing edge of rounded button
    private Paint mPaintButtonOutline, mPaintButtonFill,mPenMainText, mPenSubText;
    private boolean mAnimateFillColor = false;
    //colour of rounded button's edge.
    private int mButtonOutlineColor,
            mButtonFillColorNormal,
            mMainTextColorNormal,
            mSubTextColorNormal,

            mButtonFillColorSelected,
            mMainTextColorSelected,
            mSubTextColorSelected;
    //size of diameter of round
    private int mRadius,mCenterX,mCenterY,mMainTextXStart,mMainTextYStart, mSubTextXStart, mSubTextYStart;
    //subtext string
    private String mSubText;
    //main text string
    private String mMainText;
    //for detecting general touch events on the view
    private GestureDetector mDetector;
    //defines the type of main content for this view, the button should be drawn differently for
    //each type
    private MAIN_CONTENT_TYPE mContentType;
    private final Context mContext;
    // will help to identify current state of button, if it is currently selected or not.
    private BUTTON_STATE mButtonState = BUTTON_STATE.NORMAL;
    // bitmap incase main content of this button is an image
    private Bitmap mBitmap;
    private Rect mDrawableRect;

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundButton,
                0, 0);

        try {
            mSubText = a.getString(R.styleable.RoundButton_subText);
            mMainText = a.getString(R.styleable.RoundButton_mainText);
            mButtonOutlineColor = a.getColor(R.styleable.RoundButton_outlineColor, Color.BLACK);
            mButtonFillColorNormal = a.getColor(R.styleable.RoundButton_buttonBackgroundColorNormal, Color.WHITE);
            mMainTextColorNormal = a.getColor(R.styleable.RoundButton_fontColorMainTextNormal, Color.BLACK);
            mSubTextColorNormal = a.getColor(R.styleable.RoundButton_fontColorSubTextNormal, Color.BLACK);

            mButtonFillColorSelected = a.getColor(R.styleable.RoundButton_buttonBackgroundColorSelected, Color.WHITE);
            mMainTextColorSelected = a.getColor(R.styleable.RoundButton_fontColorMainTextSelected, Color.BLACK);
            mSubTextColorSelected = a.getColor(R.styleable.RoundButton_pressedSubTextFontColor, Color.BLACK);

            mAnimateFillColor = a.getBoolean(R.styleable.RoundButton_tranisitionColorBackground, false);
            mRadius = a.getInt(R.styleable.RoundButton_radiusOfButton, -1);
            mContentType = MAIN_CONTENT_TYPE.fromInt(a.getInteger(
                    R.styleable.RoundButton_typeOfMainContent, -1));
        } finally {
            a.recycle();
        }
        mDetector = new GestureDetector(mContext, new GestureDetection(this));
        init();
        Log.d(TAG, "constructor");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private void init() {
        mPaintButtonOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintButtonOutline.setColor(mButtonOutlineColor);
        mPaintButtonOutline.setStyle(Paint.Style.STROKE);
        mPaintButtonOutline.setStrokeWidth((float)3.2);

        mPaintButtonFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintButtonFill.setStyle(Paint.Style.FILL);
        // uncomment this to get gradient filled button style
//        mPaintButtonFill.setShader(new RadialGradient(mCenterX, mCenterY, mRadius, Color.LTGRAY, Color.WHITE, Shader.TileMode.MIRROR));

        mPenMainText = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPenSubText = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintColors();
    }

    private void setPaintColors() {
        switch (mButtonState) {
            case NORMAL:
                mPaintButtonFill.setColor(mButtonFillColorNormal);
                mPenMainText.setColor(mMainTextColorNormal);
                mPenSubText.setColor(mSubTextColorNormal);
                break;

            case SELECTED:
                mPaintButtonFill.setColor(mButtonFillColorSelected);
                mPenMainText.setColor(mMainTextColorSelected);
                mPenSubText.setColor(mSubTextColorSelected);
                break;

            case PRESSED:
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Account for padding
        Log.d(TAG, "w=" + w + " , h=" + h);
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());
        mCenterX = w / 2;
        mCenterY = h / 2;
        switch (mContentType) {
            case SINGLE_CHARACTER: {
                //starting position of text which is supposed to be inside the button
                mPenMainText.setTextSize(mRadius * (float) 1.5);

                Rect mainTextBound = new Rect();
                mPenMainText.getTextBounds(mMainText, 0, mMainText.length(), mainTextBound);
                mMainTextXStart = mCenterX - mainTextBound.width() / 2;
                GAP_FROM_TOP_EDGE_OF_CIRCLE = (int) (mRadius / 2.5);
                mMainTextYStart = mCenterY + GAP_FROM_TOP_EDGE_OF_CIRCLE;

                Rect subTextBound = new Rect();
                mPenSubText.setTextSize(mRadius / 3);
                mPenSubText.getTextBounds(mSubText, 0, mSubText.length(), subTextBound);
                mSubTextXStart = mCenterX - subTextBound.width() / 2;
                GAP_FROM_MAIN_TEXT_BOTTOM_EDGE = (mRadius / 3);
                mSubTextYStart = mMainTextYStart + GAP_FROM_MAIN_TEXT_BOTTOM_EDGE;
            }
                break;

            case TEXT: {
                // when the content type is whole text, it should be within button boundry
                mPenMainText.setTextSize(mRadius * (float) 0.65);

                Rect mainTextBound = new Rect();
                mPenMainText.getTextBounds(mMainText, 0, mMainText.length(), mainTextBound);
                mMainTextXStart = mCenterX - mainTextBound.width() / 2;
                GAP_FROM_TOP_EDGE_OF_CIRCLE = (int) (mRadius / 4.0);
                mMainTextYStart = mCenterY + GAP_FROM_TOP_EDGE_OF_CIRCLE;
            }
                break;

            case IMAGE:{
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mouse1);
                int left = mCenterX - (mRadius / 2);
                int right = mCenterX + (mRadius / 2);
                int top = mCenterY - (mRadius / 2);
                int bottom = mCenterY + (mRadius / 2);

                mDrawableRect = new Rect(left, top, right, bottom);
                Rect subTextBound = new Rect();
                mPenSubText.setTextSize(mRadius / 3);
                mPenSubText.getTextBounds(mSubText, 0, mSubText.length(), subTextBound);
                mSubTextXStart = mCenterX - subTextBound.width() / 2;
                mSubTextYStart = bottom +  subTextBound.height();
            }
            break;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw CALLED:"+ mButtonState);
        if (!mAnimateFillColor){
            setPaintColors();
        }
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintButtonFill);
        //draw the main circular button boundry
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintButtonOutline);
        switch (mContentType) {
            case SINGLE_CHARACTER:
            case TEXT:
                //draw the main content inside the button
                canvas.drawText(mMainText, mMainTextXStart, mMainTextYStart, mPenMainText);
                //draw the sub content below the main content
                canvas.drawText(mSubText, mSubTextXStart, mSubTextYStart, mPenSubText);
                break;

            case IMAGE:
                canvas.drawBitmap(mBitmap, null, mDrawableRect, null);
                canvas.drawText(mSubText, mSubTextXStart, mSubTextYStart, mPenSubText);
                break;
        }


        //temporary drawing horizontal diameter
//        canvas.drawLine(mCenterX - mRadius, mCenterY, mCenterX + mRadius, mCenterY, mPaintButtonOutline);
//        //temporary drawing vertical diameter
//        canvas.drawLine(mCenterX , mCenterY - mRadius, mCenterX , mCenterY+ mRadius, mPaintButtonOutline);
//        animateThis();
    }

    public enum MAIN_CONTENT_TYPE {
        UNKNOWN(-1),
        SINGLE_CHARACTER(0),
        TEXT(1),
        IMAGE(2);

        private int mValue;

        MAIN_CONTENT_TYPE(int value) {
            mValue = value;
        }


        public static Map<Integer, MAIN_CONTENT_TYPE> map = new HashMap<Integer, MAIN_CONTENT_TYPE>();
        static{
            for (MAIN_CONTENT_TYPE type : MAIN_CONTENT_TYPE.values()) {
                map.put(type.mValue, type);
            }
        }



        public static MAIN_CONTENT_TYPE fromInt(int val) {
            MAIN_CONTENT_TYPE type = map.get(val);
            if (type == null) {
                type = UNKNOWN;
            }
            return type;
        }
    }

    public void setBackgroundColor(int color){
        Log.d(TAG, "setBackgroundColor called.."+color);
        mPaintButtonFill.setColor(color);
        invalidate();
    }

    private void animateThis() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ObjectAnimator anim = ObjectAnimator.ofArgb(RoundButton.this, "backgroundColor", mButtonFillColorNormal, mButtonFillColorSelected);
            anim.setDuration(1000);
            anim.start();

        }
    }
    /**
     * State of the button , different colours might be required to draw each state
     */
    private enum BUTTON_STATE{
        NORMAL,
        PRESSED,
        SELECTED
    }
    /**
     * Gesture detection class for simple gestures like single tap,double tap long press etc on view
     */
    private class GestureDetection extends GestureDetector.SimpleOnGestureListener {

        private final String TAG = this.getClass().getSimpleName();
        private RoundButton mButton;

        public GestureDetection(RoundButton button) {
            this.mButton = button;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown called");
//            mButtonState = BUTTON_STATE.PRESSED;
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed");
            //toggle the selection state for button
            mButtonState = (mButtonState.equals(BUTTON_STATE.SELECTED)
                    ? BUTTON_STATE.NORMAL
                    : BUTTON_STATE.SELECTED);
            mButton.invalidate();
            if (mAnimateFillColor) {
            animateThis();

            }
            return true;
        }
    }

}
