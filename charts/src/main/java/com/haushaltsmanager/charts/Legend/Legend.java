package com.haushaltsmanager.charts.Legend;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.ColorInt;

import com.haushaltsmanager.charts.PieChart;

import java.util.ArrayList;
import java.util.List;

public class Legend {
    private String TAG = Legend.class.getSimpleName();

    private PieChart.LegendDirections mLegendDirection;
    private Rect mDesiredSize;
    private Rect mMinimumSize;
    private List<LegendItem> mLegendItems;
    private int mNumeratorItemOffset = dpToPx(5);
    private int mNumeratorChartPadding = dpToPx(10);
    public static float legendFontSize = 50f;

    public Legend(PieChart.LegendDirections legendDirection) {

        mLegendDirection = legendDirection;
        mDesiredSize = new Rect();
        mLegendItems = new ArrayList<>();
        mMinimumSize = new Rect();
    }

    public Rect getDesiredSize() {
        return mDesiredSize;
    }

    public Rect getMinimumSize() {
        return mMinimumSize;
    }

    public void addItem(LegendItem item) {
        shiftNewItem(item);
        mLegendItems.add(item);
        addItemSize(item);
    }

    public void addItem(String label, @ColorInt int color, LegendItem.NumeratorStyles style, String sliceId) {

        LegendItem item = new LegendItem(label, color, style, (int) legendFontSize, sliceId);
        shiftNewItem(item);
        mLegendItems.add(item);
        addItemSize(item);
    }

    public LegendItem getItemForPoint(Point point) {
        for (LegendItem item : mLegendItems) {
            if (point.x >= item.getStartX() && point.x <= item.getEndX())
                if (point.y >= item.getStartY() && point.y <= item.getEndY())
                    return item;
        }

        return null;
    }

    public void clear() {
        mLegendItems.clear();
    }

    /**
     * Methode um einem neu Hinzugefügtes Legendenelement die korrekte Position zu geben.
     *
     * @param item Legendenelement, welchem die Position zugeprdnet werdern soll
     */
    private void shiftNewItem(LegendItem item) {
        if (mLegendItems.size() == 0)
            return;

        LegendItem previousItem = mLegendItems.get(mLegendItems.size() - 1);
        if (mLegendDirection == PieChart.LegendDirections.LEFT_TO_RIGHT) {

            item.shift(previousItem.getEndX(), 0);
        } else {

            item.shift(0, previousItem.getEndY());
        }
    }

    public List<LegendItem> getLegendItems() {
        return mLegendItems;
    }

    /**
     * Methode um die gesamte Legendengröße zu erhöhen, wenn ein neues Legendenelement hinzukommt.
     *
     * @param item Neues LegendenElement
     */
    private void addItemSize(LegendItem item) {

        if (mLegendDirection == PieChart.LegendDirections.LEFT_TO_RIGHT) {

            mDesiredSize.right += item.getWidth() + mNumeratorItemOffset;
            mDesiredSize.bottom = Math.max(mDesiredSize.height(), item.getHeight() + mNumeratorChartPadding);

            mMinimumSize.right += item.getNumeratorWidth() + mNumeratorItemOffset;
            mMinimumSize.bottom = Math.max(mMinimumSize.height(), item.getNumeratorHeight() + mNumeratorChartPadding);
        } else {

            mDesiredSize.right = Math.max(mDesiredSize.width(), item.getWidth() + mNumeratorChartPadding);
            mDesiredSize.bottom += item.getHeight() + mNumeratorItemOffset;

            mMinimumSize.right = Math.max(mMinimumSize.width(), item.getNumeratorWidth() + mNumeratorChartPadding);
            mMinimumSize.bottom += item.getNumeratorHeight() + mNumeratorItemOffset;
        }
    }

    /**
     * Methode um DensityPixel in Pixel umzuwandeln.
     * source: https://stackoverflow.com/a/19953871/9376633
     *
     * @param dp Zu konvertierende dp
     * @return In px konvertierte dp
     */
    private static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}