package com.github.mikephil.charting.components;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.lang.ref.WeakReference;

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide custom layouts for your
 * markers.
 *
 * @author Philipp Jahoda
 */
public class MarkerView extends RelativeLayout implements IMarker {

    private MPPointF mOffset = new MPPointF();
    private MPPointF mOffset2 = new MPPointF();
    private WeakReference<Chart> mWeakChart;
    protected Context mContext;

    private float mResWidth = 0f;
    private float mResHeight = 0f;

    protected int mNormalResource, mContinuousResource, mSemMark, mRightArrow, mSelectMarkRes, mFixedMarkRes;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public MarkerView(Context context, int layoutResource) {
        super(context);

        mNormalResource = layoutResource;
        mContext = context;

        setupLayoutResource(layoutResource, "");
    }

    public MarkerView(Context context, int normalResource, int continuousResource) {
        super(context);
        mContext = context;
        mNormalResource = normalResource;
        mContinuousResource = continuousResource;

        setupLayoutResource(normalResource, "");
    }

    public MarkerView(Context context, int normalResource, int selectRes, int fixedRes, int semMark) {
        super(context);

        mContext = context;
        mNormalResource = normalResource;
        mSelectMarkRes = selectRes;
        mFixedMarkRes = fixedRes;
        mSemMark = semMark;

        setupLayoutResource(normalResource, "");
    }


    /**
     * Sets the layout resource for a custom MarkerView.
     *
     * @param layoutResource
     */
    public void setupLayoutResource(int layoutResource, String msg) {

        View inflated = LayoutInflater.from(getContext()).inflate(layoutResource, this);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        inflated.setLayoutParams(params);
        inflated.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        inflated.layout(0, 0, inflated.getMeasuredWidth(), inflated.getMeasuredHeight());

        mResWidth = inflated.getMeasuredWidth();
        mResHeight = inflated.getMeasuredHeight();
    }

    public void setOffset(MPPointF offset) {
        mOffset = offset;

        if (mOffset == null) {
            mOffset = new MPPointF();
        }
    }

    public void setOffset(float offsetX, float offsetY) {
        mOffset.x = offsetX;
        mOffset.y = offsetY;
    }

    @Override
    public MPPointF getOffset() {
        return mOffset;
    }

    public void setChartView(Chart chart) {
        mWeakChart = new WeakReference<>(chart);
    }

    public Chart getChartView() {
        return mWeakChart == null ? null : mWeakChart.get();
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {

        MPPointF offset = getOffset();
        mOffset2.x = offset.x;
        mOffset2.y = offset.y;

        Chart chart = getChartView();

        float width = getWidth();
        float height = getHeight();

        if (posX + mOffset2.x < 0) {
            mOffset2.x = - posX;
        } else if (chart != null && posX + width + mOffset2.x > chart.getWidth()) {
            mOffset2.x = chart.getWidth() - posX - width;
        }

        if (posY + mOffset2.y < 0) {
            mOffset2.y = - posY;
        } else if (chart != null && posY + height + mOffset2.y > chart.getHeight()) {
            mOffset2.y = chart.getHeight() - posY - height;
        }

        return mOffset2;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        layout(0, 0, getMeasuredWidth(), getMeasuredHeight());

    }

    @Override
    public void drawNormalMark(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mNormalResource, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    @Override
    public void drawSelectMark(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mSelectMarkRes, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);

    }

    @Override
    public void drawFixedMark(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mFixedMarkRes, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);

    }

    public void drawForContinuous(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mContinuousResource, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    @Override
    public void drawSemMark(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mSemMark, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX - offset.x/4, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);

    }

    @Override
    public void drawRightArrow(Canvas canvas, float posX, float posY, String msg) {

        removeAllViews();

        setupLayoutResource(mRightArrow, msg);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and drawNormalMark
        canvas.translate(posX + (offset.x * 1.3f), posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);

    }

}
