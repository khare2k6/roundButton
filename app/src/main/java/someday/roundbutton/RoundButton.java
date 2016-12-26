package someday.roundbutton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Round button with possible main content being :
 * 1. Single character + subtext
 * 2. Small text
 * 3. Drawable with subtext
 * Transition animation for selected state and normal state
 */

public class RoundButton extends View {

    private final String TAG = RoundButton.class.getSimpleName();
    //paint object for drawing edge of rounded button
    private Paint mPaintButtonOutline, mPaintButtonFill, mPaintPenMainText, mPaintPenSubText;
    private boolean mAnimateFillColorEnabled = false;
    //colour of rounded button's edge.
    private int mButtonOutlineColor,
            mButtonFillColorNormal,
            mMainTextColorNormal,
            mSubTextColorNormal,

            mButtonFillColorSelected,
            mMainTextColorSelected,
            mSubTextColorSelected;
    //size of diameter of round
    private float mRadius,mCenterX,mCenterY,mMainTextXStart,mMainTextYStart, mSubTextXStart, mSubTextYStart;
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
    private Drawable mDrwable;
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
            mSubTextColorSelected = a.getColor(R.styleable.RoundButton_fontColorSubTextSelected, Color.BLACK);
            mDrwable = a.getDrawable(R.styleable.RoundButton_drawable);

            mAnimateFillColorEnabled = a.getBoolean(R.styleable.RoundButton_tranisitionColorBackground, false);
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

        mPaintPenMainText = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaintPenSubText = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintColors();
    }

    private void setPaintColors() {
        switch (mButtonState) {
            case NORMAL:
                mPaintButtonFill.setColor(mButtonFillColorNormal);
                mPaintPenMainText.setColor(mMainTextColorNormal);
                mPaintPenSubText.setColor(mSubTextColorNormal);
                break;

            case SELECTED:
                mPaintButtonFill.setColor(mButtonFillColorSelected);
                mPaintPenMainText.setColor(mMainTextColorSelected);
                mPaintPenSubText.setColor(mSubTextColorSelected);
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
        float GAP_FROM_TOP_EDGE_OF_CIRCLE ;
        float GAP_FROM_MAIN_TEXT_BOTTOM_EDGE ;
        mCenterX = w / 2;
        mCenterY = h / 2;
        switch (mContentType) {
            case SINGLE_CHARACTER: {
                //starting position of text which is supposed to be inside the button
                mPaintPenMainText.setTextSize(mRadius * (float) 1.5);

                Rect mainTextBound = new Rect();
                mPaintPenMainText.getTextBounds(mMainText, 0, mMainText.length(), mainTextBound);
                mMainTextXStart =  mCenterX - mainTextBound.width() / 2;
                GAP_FROM_TOP_EDGE_OF_CIRCLE = (int) (mRadius / 2);
                mMainTextYStart = mCenterY + GAP_FROM_TOP_EDGE_OF_CIRCLE;

                Rect subTextBound = new Rect();
                mPaintPenSubText.setTextSize(mRadius / 3);
                mPaintPenSubText.getTextBounds(mSubText, 0, mSubText.length(), subTextBound);
                mSubTextXStart =  mCenterX - subTextBound.width() / 2;
                GAP_FROM_MAIN_TEXT_BOTTOM_EDGE = (mRadius / 3);
                mSubTextYStart = mMainTextYStart + GAP_FROM_MAIN_TEXT_BOTTOM_EDGE;
            }
                break;

            case TEXT: {
                // when the content type is whole text, it should be within button boundry
                mPaintPenMainText.setTextSize(mRadius * (float) 0.65);

                Rect mainTextBound = new Rect();
                mPaintPenMainText.getTextBounds(mMainText, 0, mMainText.length(), mainTextBound);
                mMainTextXStart = mCenterX - mainTextBound.width() / 2;
                GAP_FROM_TOP_EDGE_OF_CIRCLE = (int) (mRadius / 4.0);
                mMainTextYStart = mCenterY + GAP_FROM_TOP_EDGE_OF_CIRCLE;
            }
                break;

            case IMAGE:{
//                mBitmap = BitmapFactory.decodeResource(getResources(), mDrwable.);
                mBitmap = ((BitmapDrawable) mDrwable).getBitmap();
                int left = (int) (mCenterX - (mRadius / 2));
                int right = (int) (mCenterX + (mRadius / 2));
                int top = (int) (mCenterY - (mRadius / 2));
                int bottom = (int) (mCenterY + (mRadius / 2));

                mDrawableRect = new Rect(left, top, right, bottom);
                Rect subTextBound = new Rect();
                mPaintPenSubText.setTextSize(mRadius / 3);
                mPaintPenSubText.getTextBounds(mSubText, 0, mSubText.length(), subTextBound);
                mSubTextXStart = mCenterX - subTextBound.width() / 2;
                mSubTextYStart = bottom +  subTextBound.height();
            }
            break;
        }
    }


    @Override
    public void invalidate() {
        Log.d(TAG, "invalidate called");
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw CALLED: "+ mButtonState);
        if (!mAnimateFillColorEnabled){
            setPaintColors();
        }
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintButtonFill);
        //draw the main circular button boundry
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaintButtonOutline);


        switch (mContentType) {
            case SINGLE_CHARACTER:
            case TEXT:
                //draw the main content inside the button
                canvas.drawText(mMainText, mMainTextXStart, mMainTextYStart, mPaintPenMainText);
                //draw the sub content below the main content
                canvas.drawText(mSubText, mSubTextXStart, mSubTextYStart, mPaintPenSubText);
                break;

            case IMAGE:
                canvas.drawBitmap(mBitmap, null, mDrawableRect, null);
                canvas.drawText(mSubText, mSubTextXStart, mSubTextYStart, mPaintPenSubText);
                break;
        }

        //temp
//        Rect rect = new Rect();
//        mPaintPenMainText.getTextBounds(mMainText, 0, mMainText.length(), rect);
//        canvas.drawRect(new Rect((int)mMainTextXStart,(int)mMainTextYStart - rect.height(),(int)mMainTextXStart+rect.width(),(int)mMainTextYStart), mPaintButtonOutline);
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


        public static Map<Integer, MAIN_CONTENT_TYPE> map = new HashMap<>();
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

    public void setContentTextColor(int color){
        if (mContentType.equals(MAIN_CONTENT_TYPE.SINGLE_CHARACTER) || mContentType.equals(MAIN_CONTENT_TYPE.TEXT)) {
            mPaintPenMainText.setColor(color);
            mPaintPenSubText.setColor(color);
//            invalidate();
        }
    }

    private void animateThis() {
        int animPeriod = 1000;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int buttonFillColorFrom = Color.WHITE,buttonFillColorTo = Color.WHITE;
            int textColorFrom = Color.BLACK,textColorTo = Color.WHITE;
            switch (mButtonState) {
                case SELECTED:
                    buttonFillColorFrom = mButtonFillColorNormal;
                    buttonFillColorTo = mButtonFillColorSelected;

                    textColorFrom = mMainTextColorNormal;
                    textColorTo = mMainTextColorSelected;
                    break;

                case NORMAL:
                    buttonFillColorFrom = mButtonFillColorSelected;
                    buttonFillColorTo = mButtonFillColorNormal;

                    textColorFrom = mMainTextColorSelected;
                    textColorTo = mMainTextColorNormal;
                    break;

            }
            ObjectAnimator buttonFillColorAnimation = ObjectAnimator.ofArgb(RoundButton.this, "backgroundColor", buttonFillColorFrom, buttonFillColorTo);
            ObjectAnimator fontColorAnimation = ObjectAnimator.ofArgb(RoundButton.this, "contentTextColor", textColorFrom, textColorTo);

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(animPeriod);
            animSet.playTogether(buttonFillColorAnimation, fontColorAnimation);
            animSet.start();
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

        protected GestureDetection(RoundButton button) {
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
            if (mAnimateFillColorEnabled) {
                animateThis();
            }else{
                mButton.invalidate();
            }
            return true;
        }
    }

}
