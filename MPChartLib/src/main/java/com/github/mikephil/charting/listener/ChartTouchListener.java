package com.github.mikephil.charting.listener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.ArrayList;
import java.util.Arrays;

import static java.sql.Types.NULL;

/**
 * Created by philipp on 12/06/15.
 */
public abstract class ChartTouchListener<T extends Chart<?>> extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
    final String TAG = "ChartTouchListener";
    final boolean D = true;

    public enum ChartGesture {
        NONE, DRAG, X_ZOOM, Y_ZOOM, PINCH_ZOOM, ROTATE, SINGLE_TAP, DOUBLE_TAP, LONG_PRESS, FLING
    }

    /**
     * the last touch gesture that has been performed
     **/
    protected ChartGesture mLastGesture = ChartGesture.NONE;

    // states
    protected static final int NONE = 0;
    protected static final int DRAG = 1;
    protected static final int X_ZOOM = 2;
    protected static final int Y_ZOOM = 3;
    protected static final int PINCH_ZOOM = 4;
    protected static final int POST_ZOOM = 5;
    protected static final int ROTATE = 6;

    protected float min;
    protected int minDistanceIndex = -1;
    protected int mSelectedIndex = -1;
    protected Highlight selectedHighlight;
    protected Highlight dragHighlight;
    protected float touchDist = 100f;

    /**
     * integer field that holds the current touch-state
     */
    protected int mTouchMode = NONE;

    /**
     * the last highlighted object (via touch)
     */
    protected ArrayList<Highlight> mLastHighlighted = new ArrayList<>();

    /**
     * the gesturedetector used for detecting taps and longpresses, ...
     */
    protected GestureDetector mGestureDetector;

    /**
     * the chart the listener represents
     */
    protected T mChart;

    public ChartTouchListener(T chart) {
        this.mChart = chart;

        mGestureDetector = new GestureDetector(chart.getContext(), this);

        for (int i = 0; i < 5; i++) {

            mLastHighlighted.add(null);

        }
    }

    /**
     * Calls the OnChartGestureListener to do the start callback
     *
     * @param me
     */
    public void startAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureStart(me, mLastGesture);
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     *
     * @param me
     */
    public void endAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureEnd(me, mLastGesture);
    }

    /**
     * Sets the last value that was highlighted via touch.
     *
     * @param high
     */

    public void setLastHighlighted(Highlight high) {
        Log.e(TAG, "setLastHighlighted , mLastHighlighted.clear()");
        //mLastHighlighted.clear();
    }

    public void setLastHighlighted(ArrayList<Highlight> highs) {
        mLastHighlighted = highs;
    }

//    public void setLastHighlighted(int index, Highlight high) {
//        if(index < mLastHighlighted.size())
//            mLastHighlighted.set(index, high);
//        else Log.d("setLastHighlighted", "OutOfIndex");
//    }

    public void setOffSelectHighlight() {
        selectedHighlight = null;
        minDistanceIndex = -1;
    }

    public int getMinDistanceIndex() {
        return minDistanceIndex;
    }

    public void addLastHighlighted(Highlight high) {
        if (mLastHighlighted == null)
            mLastHighlighted = new ArrayList<>();
        mLastHighlighted.add(high);
    }

    public void addLastHighlighted(int idx, Highlight high) {
        if (mLastHighlighted == null) {
            mLastHighlighted = new ArrayList<>();
        }

        Log.d("addLastHighlighted", mLastHighlighted.size() + "");
        if (mLastHighlighted.size() == 0) return;
        if (mLastHighlighted.size() <= idx) {
            return;
        }

        mSelectedIndex = idx;
        mLastHighlighted.set(idx, high);
    }

    public void removeHighlightByIndex(int idx) {
        if (D) Log.d(TAG, "removeHighlightByIndex = " + idx);

        if (mLastHighlighted != null && mLastHighlighted.size() >= idx && idx >= 0) {
            mLastHighlighted.set(idx, null);
        }

        if (isAllOffHighlights()) mSelectedIndex = -1;
        else {

            for (int i = mLastHighlighted.size() - 1; i >= 0; i--) {

                if (mLastHighlighted.get(i) != null) {
                    mSelectedIndex = i;
                    break;
                }

            }

        }

    }

    public void removeAllHighlights() {
        if (D) Log.d(TAG, "removeAllHighlights");

        for (int i = 0; i < mLastHighlighted.size(); i++) {
            mLastHighlighted.set(i, null);
        }

        mSelectedIndex = -1;

    }

    public boolean isAllOffHighlights() {

        boolean isOff = true;
        for (int i = 0; i < mLastHighlighted.size(); i++) {

            if (mLastHighlighted.get(i) != null) isOff = false;
        }

        return isOff;
    }

    public int getXByIndex(int index) {
        if (mLastHighlighted != null && mLastHighlighted.size() != 0 && index < mLastHighlighted.size() && mLastHighlighted.get(index).getX() != NULL)
            return (int) mLastHighlighted.get(index).getX();
        else if (mLastHighlighted != null && index < mLastHighlighted.size() && mLastHighlighted.get(index).getX() == NULL)
            return 0;
        else return 0;
    }

    public ArrayList<Highlight> getLastHighlighted() {
        return mLastHighlighted;
    }

    public void resetSelectedMarker() {

        mSelectedIndex = -1;

    }

    public void clearLastHighlighted() {
        mLastHighlighted = null;
    }

    /**
     * returns the touch mode the listener is currently in
     *
     * @return
     */
    public int getTouchMode() {
        return mTouchMode;
    }

    /**
     * Returns the last gesture that has been performed on the chart.
     *
     * @return
     */
    public ChartGesture getLastGesture() {
        return mLastGesture;
    }


    /**
     * Perform a highlight operation.
     *
     * @param e
     */
    protected void performHighlight(Highlight h, MotionEvent e) {

        if (mLastHighlighted.size() == 0) mLastHighlighted.add(dragHighlight);
        if (mLastHighlighted.get(0) == null) return;

//        dragHighlight = mChart.getHighlightByTouchPoint(e.getX(), e.getY());

        min = Math.abs(mLastHighlighted.get(0).getDrawX() - e.getX());
        minDistanceIndex = 0;

        Log.d("mLastHighlighted", "mLastHighlighted size : " + mLastHighlighted.size());

        Log.d("mLastHighlighted", "getDrawX() : " + mLastHighlighted.get(0).getDrawX());
        Log.d("mLastHighlighted", "index : " + 0 + " dist : " + Math.abs(mLastHighlighted.get(0).getDrawX() - e.getX()));

        try {
            for (int i = 1; i < 5; i++) {

                if (mLastHighlighted.get(i) == null) continue;
                if (mLastHighlighted.get(i).isFixed()) continue;

                Log.d("mLastHighlighted", "getDrawX() : " + mLastHighlighted.get(i).getDrawX());
                Log.d("mLastHighlighted", "index : " + i + " dist : " + Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX()));

                if (mLastHighlighted.get(i) != null && Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX()) < min) {
                    min = Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX());
                    minDistanceIndex = i;
                    Log.d("performHighlight", minDistanceIndex + " " + mSelectedIndex);
                } else if (Math.abs(mLastHighlighted.get(i).getDrawX() - e.getX()) == min && i == mSelectedIndex) {
                    Log.d("performHighlight", minDistanceIndex + " " + mSelectedIndex + " " + i);
                    minDistanceIndex = i;
                }
            }

            if (min < touchDist && minDistanceIndex != -1) {
                mSelectedIndex = minDistanceIndex;
                dragHighlight = mLastHighlighted.get(mSelectedIndex);

            } else mSelectedIndex = -1;

            Log.d("PerformClick", mSelectedIndex + "");

        } catch (NullPointerException ee) {
            ee.printStackTrace();
        }


        if (dragHighlight != null && mSelectedIndex != -1 && mLastHighlighted.get(mSelectedIndex) != null &&
                !dragHighlight.equalTo(mLastHighlighted.get(mSelectedIndex)) && min < touchDist) {

//            addLastHighlighted(mSelectedIndex, dragHighlight);
//            mChart.highlightValue(dragHighlight, mSelectedIndex, true);
            selectedHighlight = mLastHighlighted.get(mSelectedIndex);
//            Log.d("getDataIndex", "Selected Data Index : " + mLastHighlighted.get(mSelectedIndex).getX());

        } else if (dragHighlight != null && mSelectedIndex != -1 && mLastHighlighted.get(mSelectedIndex) != null &&
                dragHighlight.equalTo(mLastHighlighted.get(mSelectedIndex))
                && min < touchDist) {

            selectedHighlight = mLastHighlighted.get(mSelectedIndex);
//            Log.d("getDataIndex", "Selected Data Index : " + mLastHighlighted.get(mSelectedIndex).getX());

        }

//        if(mLastHighlighted == null && !h.equalTo(minHighlight)) {
//            mChart.highlightValue(h, true);
//            mLastHighlighted.set(minDistanceIndex, h);
//        }

//        if (h == null || h.equalTo(mLastHighlighted)) {
//            mChart.highlightValue(null, true);
//            mLastHighlighted = null;
//
//        } else {
//            mChart.highlightValue(h, true);
//            mLastHighlighted = h;
//        }
    }

    /**
     * returns the distance between two points
     *
     * @param eventX
     * @param startX
     * @param eventY
     * @param startY
     * @return
     */
    protected static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public int getSelectedHighlightIndex() {
        return mSelectedIndex;
    }

    public void setSelectedHighlightIndex(int minIndex) {
        mSelectedIndex = minIndex;
    }

    public Highlight getDraggingHighlight() {
        return dragHighlight;
    }

}
