package com.hitasoft.app.external.mpchart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;

/**
 * Baseclass of all Chart-Views.
 *
 * @author Philipp Jahoda
 */
public abstract class Chart<T extends ChartData<? extends IDataSet<? extends Entry>>> extends
        ViewGroup
        implements ChartInterface {

    public static final String LOG_TAG = "MPAndroidChart";

    /**
     * flag that indicates if logging is enabled or not
     */
    protected boolean mLogEnabled = false;

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    protected T mData = null;

    /**
     * Flag that indicates if highlighting per tap (touch) is enabled
     */
    protected boolean mHighLightPerTapEnabled = true;

    /**
     * If set to true, chart continues to scroll after touch up
     */
    private boolean mDragDecelerationEnabled = true;

    /**
     * Deceleration friction coefficient in [0 ; 1] interval, higher values
     * indicate that speed will decrease slowly, for example if it set to 0, it
     * will stop immediately. 1 is an invalid value, and will be converted to
     * 0.999f automatically.
     */
    private float mDragDecelerationFrictionCoef = 0.9f;

    /**
     * default value-formatter, number of digits depends on provided chart-data
     */
    protected DefaultValueFormatter mDefaultValueFormatter = new DefaultValueFormatter(0);

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected Paint mDescPaint;

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected Paint mInfoPaint;

    /**
     * the object representing the labels on the x-axis
     */
    protected XAxis mXAxis;

    /**
     * if true, touch gestures are enabled on the chart
     */
    protected boolean mTouchEnabled = true;

    /**
     * the object responsible for representing the description text
     */
    protected Description mDescription;

    /**
     * the legend object containing all data associated with the legend
     */
    protected Legend mLegend;


    /**
     * text that is displayed when the chart is empty
     */
    private String mNoDataText = "No chart data available.";

    protected LegendRenderer mLegendRenderer;

    /**
     * object responsible for rendering the data
     */
    protected DataRenderer mRenderer;

    /**
     * object that manages the bounds and drawing constraints of the chart
     */
    protected ViewPortHandler mViewPortHandler = new ViewPortHandler();

    /**
     * object responsible for animations
     */
    protected ChartAnimator mAnimator;

    /**
     * Extra offsets to be appended to the viewport
     */
    private float mExtraTopOffset = 0.f,
            mExtraRightOffset = 0.f,
            mExtraBottomOffset = 0.f,
            mExtraLeftOffset = 0.f;

    /**
     * default constructor for initialization in code
     */
    public Chart(Context context) {
        super(context);
        init();
    }

    /**
     * constructor for initialization in xml
     */
    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * even more awesome constructor
     */
    public Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize all paints and stuff
     */
    protected void init() {

        setWillNotDraw(false);
        // setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mAnimator = new ChartAnimator(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // ViewCompat.postInvalidateOnAnimation(Chart.this);
                postInvalidate();
            }
        });

        // initialize the utils
        Utils.init(getContext());
        mMaxHighlightDistance = Utils.convertDpToPixel(500f);

        mDescription = new Description();
        mLegend = new Legend();

        mLegendRenderer = new LegendRenderer(mViewPortHandler, mLegend);

        mXAxis = new XAxis();

        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(Color.rgb(247, 189, 51)); // orange
        mInfoPaint.setTextAlign(Align.CENTER);
        mInfoPaint.setTextSize(Utils.convertDpToPixel(12f));

        if (mLogEnabled)
            Log.i("", "Chart.init()");
    }

    // public void initWithDummyData() {
    // ColorTemplate template = new ColorTemplate();
    // template.addColorsForDataSets(ColorTemplate.COLORFUL_COLORS,
    // getContext());
    //
    // setColorTemplate(template);
    // setDrawYValues(false);
    //
    // ArrayList<String> xVals = new ArrayList<String>();
    // Calendar calendar = Calendar.getInstance();
    // for (int i = 0; i < 12; i++) {
    // xVals.add(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
    // Locale.getDefault()));
    // }
    //
    // ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
    // for (int i = 0; i < 3; i++) {
    //
    // ArrayList<Entry> yVals = new ArrayList<Entry>();
    //
    // for (int j = 0; j < 12; j++) {
    // float val = (float) (Math.random() * 100);
    // yVals.add(new Entry(val, j));
    // }
    //
    // DataSet set = new DataSet(yVals, "DataSet " + i);
    // dataSets.add(set); // add the datasets
    // }
    // // create a data object with the datasets
    // ChartData data = new ChartData(xVals, dataSets);
    // setData(data);
    // invalidate();
    // }

    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     *
     * @param data
     */
    public void setData(T data) {

        mData = data;
        mOffsetsCalculated = false;

        if (data == null) {
            return;
        }

        // calculate how many digits are needed
        setupDefaultFormatter(data.getYMin(), data.getYMax());

        for (IDataSet set : mData.getDataSets()) {
            if (set.needsFormatter() || set.getValueFormatter() == mDefaultValueFormatter)
                set.setValueFormatter(mDefaultValueFormatter);
        }

        // let the chart know there is new data
        notifyDataSetChanged();

        if (mLogEnabled)
            Log.i(LOG_TAG, "Data is set.");
    }

    /**
     * Clears the chart from all data (sets it to null) and refreshes it (by
     * calling invalidate()).
     */
    public void clear() {
        mData = null;
        mOffsetsCalculated = false;
        invalidate();
    }

    /**
     * Removes all DataSets (and thereby Entries) from the chart. Does not set the data object to null. Also refreshes the
     * chart by calling invalidate().
     */
    public void clearValues() {
        mData.clearValues();
        invalidate();
    }

    /**
     * Returns true if the chart is empty (meaning it's data object is either
     * null or contains no entries).
     *
     * @return
     */
    public boolean isEmpty() {

        if (mData == null)
            return true;
        else {

            if (mData.getEntryCount() <= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * Lets the chart know its underlying data has changed and performs all
     * necessary recalculations. It is crucial that this method is called
     * everytime data is changed dynamically. Not calling this method can lead
     * to crashes or unexpected behaviour.
     */
    public abstract void notifyDataSetChanged();

    /**
     * Calculates the offsets of the chart to the border depending on the
     * position of an eventual legend or depending on the length of the y-axis
     * and x-axis labels and their position
     */
    protected abstract void calculateOffsets();

    /**
     * Calculates the y-min and y-max value and the y-delta and x-delta value
     */
    protected abstract void calcMinMax();

    /**
     * Calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled), and creates the default-value-formatter
     */
    protected void setupDefaultFormatter(float min, float max) {

        float reference = 0f;

        if (mData == null || mData.getEntryCount() < 2) {

            reference = Math.max(Math.abs(min), Math.abs(max));
        } else {
            reference = Math.abs(max - min);
        }

        int digits = Utils.getDecimals(reference);

        // setup the formatter with a new number of digits
        mDefaultValueFormatter.setup(digits);
    }

    /**
     * flag that indicates if offsets calculation has already been done or not
     */
    private boolean mOffsetsCalculated = false;

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);

        if (mData == null) {

            boolean hasText = !TextUtils.isEmpty(mNoDataText);

            if (hasText) {
                MPPointF c = getCenter();
                canvas.drawText(mNoDataText, c.x, c.y, mInfoPaint);
            }

            return;
        }

        if (!mOffsetsCalculated) {

            calculateOffsets();
            mOffsetsCalculated = true;
        }
    }

    /**
     * Draws the description text in the bottom right corner of the chart (per default)
     */
    protected void drawDescription(Canvas c) {

        // check if description should be drawn
        if (mDescription != null && mDescription.isEnabled()) {

            MPPointF position = mDescription.getPosition();

            mDescPaint.setTypeface(mDescription.getTypeface());
            mDescPaint.setTextSize(mDescription.getTextSize());
            mDescPaint.setColor(mDescription.getTextColor());
            mDescPaint.setTextAlign(mDescription.getTextAlign());

            float x, y;

            // if no position specified, draw on default position
            if (position == null) {
                x = getWidth() - mViewPortHandler.offsetRight() - mDescription.getXOffset();
                y = getHeight() - mViewPortHandler.offsetBottom() - mDescription.getYOffset();
            } else {
                x = position.x;
                y = position.y;
            }

            c.drawText(mDescription.getText(), x, y, mDescPaint);
        }
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS CODE FOR HIGHLIGHTING */


    /**
     * The maximum distance in dp away from an entry causing it to highlight.
     */
    protected float mMaxHighlightDistance = 0f;

    @Override
    public float getMaxHighlightDistance() {
        return mMaxHighlightDistance;
    }

    /**
     * Sets the maximum distance in screen dp a touch can be away from an entry to cause it to get highlighted.
     * Default: 500dp
     *
     * @param distDp
     */
    public void setMaxHighlightDistance(float distDp) {
        mMaxHighlightDistance = Utils.convertDpToPixel(distDp);
    }

    /**
     * Returns true if values can be highlighted via tap gesture, false if not.
     *
     * @return
     */
    public boolean isHighlightPerTapEnabled() {
        return mHighLightPerTapEnabled;
    }

    /**
     * Set this to false to prevent values from being highlighted by tap gesture.
     * Values can still be highlighted via drag or programmatically. Default: true
     *
     * @param enabled
     */
    public void setHighlightPerTapEnabled(boolean enabled) {
        mHighLightPerTapEnabled = enabled;
    }


    /**
     * Highlights the value at the given x-value and y-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     * @param x The x-value to highlight
     * @param y The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     */

    /**
     * ################ ################ ################ ################
     */
    /** BELOW CODE IS FOR THE MARKER VIEW */

    /**
     * if set to true, the marker view is drawn when a value is clicked
     */
    protected boolean mDrawMarkers = true;

    /**
     * If set to true, chart continues to scroll after touch up default: true
     */
    public boolean isDragDecelerationEnabled() {
        return mDragDecelerationEnabled;
    }

    /**
     * If set to true, chart continues to scroll after touch up. Default: true.
     *
     * @param enabled
     */
    public void setDragDecelerationEnabled(boolean enabled) {
        mDragDecelerationEnabled = enabled;
    }

    /**
     * Returns drag deceleration friction coefficient
     *
     * @return
     */
    public float getDragDecelerationFrictionCoef() {
        return mDragDecelerationFrictionCoef;
    }

    /**
     * Deceleration friction coefficient in [0 ; 1] interval, higher values
     * indicate that speed will decrease slowly, for example if it set to 0, it
     * will stop immediately. 1 is an invalid value, and will be converted to
     * 0.999f automatically.
     *
     * @param newValue
     */
    public void setDragDecelerationFrictionCoef(float newValue) {

        if (newValue < 0.f)
            newValue = 0.f;

        if (newValue >= 1f)
            newValue = 0.999f;

        mDragDecelerationFrictionCoef = newValue;
    }

    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR PROVIDING EASING FUNCTIONS */


    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR PREDEFINED EASING OPTIONS */

    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR ANIMATIONS WITHOUT EASING */


    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS ONLY GETTERS AND SETTERS */


    /**
     * Returns the object representing all x-labels, this method can be used to
     * acquire the XAxis object and modify it (e.g. change the position of the
     * labels, styling, etc.)
     *
     * @return
     */
    public XAxis getXAxis() {
        return mXAxis;
    }

    /**
     * Returns the default IValueFormatter that has been determined by the chart
     * considering the provided minimum and maximum values.
     *
     * @return
     */
    public ValueFormatter getDefaultValueFormatter() {
        return mDefaultValueFormatter;
    }

    /**
     * /**
     * returns the current y-max value across all DataSets
     *
     * @return
     */
    public float getYMax() {
        return mData.getYMax();
    }

    /**
     * returns the current y-min value across all DataSets
     *
     * @return
     */
    public float getYMin() {
        return mData.getYMin();
    }

    @Override
    public float getXChartMax() {
        return mXAxis.mAxisMaximum;
    }

    @Override
    public float getXChartMin() {
        return mXAxis.mAxisMinimum;
    }

    @Override
    public float getXRange() {
        return mXAxis.mAxisRange;
    }

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the center point of the chart (the whole View) in pixels.
     *
     * @return
     */
    public MPPointF getCenter() {
        return MPPointF.getInstance(getWidth() / 2f, getHeight() / 2f);
    }

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     *
     * @return
     */
    @Override
    public MPPointF getCenterOffsets() {
        return mViewPortHandler.getContentCenter();
    }

    /**
     * Sets extra offsets (around the chart view) to be appended to the
     * auto-calculated offsets.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setExtraOffsets(float left, float top, float right, float bottom) {
        setExtraLeftOffset(left);
        setExtraTopOffset(top);
        setExtraRightOffset(right);
        setExtraBottomOffset(bottom);
    }

    /**
     * Set an extra offset to be appended to the viewport's top
     */
    public void setExtraTopOffset(float offset) {
        mExtraTopOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's top
     */
    public float getExtraTopOffset() {
        return mExtraTopOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's right
     */
    public void setExtraRightOffset(float offset) {
        mExtraRightOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's right
     */
    public float getExtraRightOffset() {
        return mExtraRightOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's bottom
     */
    public void setExtraBottomOffset(float offset) {
        mExtraBottomOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's bottom
     */
    public float getExtraBottomOffset() {
        return mExtraBottomOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's left
     */
    public void setExtraLeftOffset(float offset) {
        mExtraLeftOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's left
     */
    public float getExtraLeftOffset() {
        return mExtraLeftOffset;
    }

    /**
     * Set this to true to enable logcat outputs for the chart. Beware that
     * logcat output decreases rendering performance. Default: disabled.
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        mLogEnabled = enabled;
    }

    /**
     * Returns true if log-output is enabled for the chart, fals if not.
     *
     * @return
     */
    public boolean isLogEnabled() {
        return mLogEnabled;
    }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     *
     * @param text
     */
    public void setNoDataText(String text) {
        mNoDataText = text;
    }

    /**
     * Sets the color of the no data text.
     *
     * @param color
     */
    public void setNoDataTextColor(int color) {
        mInfoPaint.setColor(color);
    }

    /**
     * Sets the typeface to be used for the no data text.
     *
     * @param tf
     */
    public void setNoDataTextTypeface(Typeface tf) {
        mInfoPaint.setTypeface(tf);
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     *
     * @param enabled
     */
    public void setTouchEnabled(boolean enabled) {
        this.mTouchEnabled = enabled;
    }

    /**
     * Sets a new Description object for the chart.
     *
     * @param desc
     */
    public void setDescription(Description desc) {
        this.mDescription = desc;
    }

    /**
     * Returns the Description object of the chart that is responsible for holding all information related
     * to the description text that is displayed in the bottom right corner of the chart (by default).
     *
     * @return
     */
    public Description getDescription() {
        return mDescription;
    }

    /**
     * Returns the Legend object of the chart. This method can be used to get an
     * instance of the legend in order to customize the automatically generated
     * Legend.
     *
     * @return
     */
    public Legend getLegend() {
        return mLegend;
    }


    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     *
     * @return
     */
    @Override
    public RectF getContentRect() {
        return mViewPortHandler.getContentRect();
    }

    /**
     * disables intercept touchevents
     */
    public void disableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }

    /**
     * enables intercept touchevents
     */
    public void enableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(false);
    }

    /**
     * paint for the grid background (only line and barchart)
     */
    public static final int PAINT_GRID_BACKGROUND = 4;

    /**
     * paint for the info text that is displayed when there are no values in the
     * chart
     */
    public static final int PAINT_INFO = 7;

    /**
     * paint for the description text in the bottom right corner
     */
    public static final int PAINT_DESCRIPTION = 11;

    /**
     * paint for the hole in the middle of the pie chart
     */
    public static final int PAINT_HOLE = 13;

    /**
     * paint for the text in the middle of the pie chart
     */
    public static final int PAINT_CENTER_TEXT = 14;

    /**
     * paint used for the legend
     */
    public static final int PAINT_LEGEND_LABEL = 18;

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p     the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     *              ...
     */
    public void setPaint(Paint p, int which) {

        switch (which) {
            case PAINT_INFO:
                mInfoPaint = p;
                break;
            case PAINT_DESCRIPTION:
                mDescPaint = p;
                break;
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     * @return
     */
    public Paint getPaint(int which) {
        switch (which) {
            case PAINT_INFO:
                return mInfoPaint;
            case PAINT_DESCRIPTION:
                return mDescPaint;
        }

        return null;
    }

    @Deprecated
    public boolean isDrawMarkerViewsEnabled() {
        return isDrawMarkersEnabled();
    }

    @Deprecated
    public void setDrawMarkerViews(boolean enabled) {
        setDrawMarkers(enabled);
    }

    /**
     * returns true if drawing the marker is enabled when tapping on values
     * (use the setMarker(IMarker marker) method to specify a marker)
     *
     * @return
     */
    public boolean isDrawMarkersEnabled() {
        return mDrawMarkers;
    }

    /**
     * Set this to true to draw a user specified marker when tapping on
     * chart values (use the setMarker(IMarker marker) method to specify a
     * marker). Default: true
     *
     * @param enabled
     */
    public void setDrawMarkers(boolean enabled) {
        mDrawMarkers = enabled;
    }

    /**
     * Returns the ChartData object that has been set for the chart.
     *
     * @return
     */
    public T getData() {
        return mData;
    }

    /**
     * Returns the ViewPortHandler of the chart that is responsible for the
     * content area of the chart and its offsets and dimensions.
     *
     * @return
     */
    public ViewPortHandler getViewPortHandler() {
        return mViewPortHandler;
    }

    /**
     * Returns a recyclable MPPointF instance.
     *
     * @return
     */
    @Override
    public MPPointF getCenterOfView() {
        return getCenter();
    }

    /**
     * Returns the bitmap that represents the chart.
     *
     * @return
     */
    public Bitmap getChartBitmap() {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on
            // the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }

    /**
     * tasks to be done after the view is setup
     */
    protected ArrayList<Runnable> mJobs = new ArrayList<Runnable>();

    public void removeViewportJob(Runnable job) {
        mJobs.remove(job);
    }

    public void clearAllViewportJobs() {
        mJobs.clear();
    }

    /**
     * Either posts a job immediately if the chart has already setup it's
     * dimensions or adds the job to the execution queue.
     *
     * @param job
     */
    public void addViewportJob(Runnable job) {

        if (mViewPortHandler.hasChartDimens()) {
            post(job);
        } else {
            mJobs.add(job);
        }
    }

    /**
     * Returns all jobs that are scheduled to be executed after
     * onSizeChanged(...).
     *
     * @return
     */
    public ArrayList<Runnable> getJobs() {
        return mJobs;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) Utils.convertDpToPixel(50f);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(size,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(size,
                                heightMeasureSpec)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mLogEnabled)
            Log.i(LOG_TAG, "OnSizeChanged()");

        if (w > 0 && h > 0 && w < 10000 && h < 10000) {
            if (mLogEnabled)
                Log.i(LOG_TAG, "Setting chart dimens, width: " + w + ", height: " + h);
            mViewPortHandler.setChartDimens(w, h);
        } else {
            if (mLogEnabled)
                Log.w(LOG_TAG, "*Avoiding* setting chart dimens! width: " + w + ", height: " + h);
        }

        // This may cause the chart view to mutate properties affecting the view port --
        //   lets do this before we try to run any pending jobs on the view port itself
        notifyDataSetChanged();

        for (Runnable r : mJobs) {
            post(r);
        }

        mJobs.clear();

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Setting this to true will set the layer-type HARDWARE for the view, false
     * will set layer-type SOFTWARE.
     *
     * @param enabled
     */
    public void setHardwareAccelerationEnabled(boolean enabled) {

        if (enabled)
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        else
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //Log.i(LOG_TAG, "Detaching...");

        if (mUnbind)
            unbindDrawables(this);
    }

    /**
     * unbind flag
     */
    private boolean mUnbind = false;

    /**
     * Unbind all drawables to avoid memory leaks.
     * Link: http://stackoverflow.com/a/6779164/1590502
     *
     * @param view
     */
    private void unbindDrawables(View view) {

        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    /**
     * Set this to true to enable "unbinding" of drawables. When a View is detached
     * from a window. This helps avoid memory leaks.
     * Default: false
     * Link: http://stackoverflow.com/a/6779164/1590502
     *
     * @param enabled
     */
    public void setUnbindEnabled(boolean enabled) {
        this.mUnbind = enabled;
    }
}