package com.haushaltsmanager.charts.Legend;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.text.TextPaint;

public class LegendItem {

    public enum NumeratorStyles {
        CIRCLE, SQUARE
    }

    private NumeratorStyles mNumeratorStyle;

    private String mSliceId;
    private Rect mNumeratorBounds;
    private Paint mNumeratorPaint;
    private Rect mLabelBounds;
    private TextPaint mLabelPaint;
    private @ColorInt
    int mColor;
    private String mLabel;

    public LegendItem(@NonNull String label, @ColorInt int color, @NonNull NumeratorStyles style, int numeratorSize, String sliceId) {

        mColor = color;
        mNumeratorStyle = style;
        mLabel = label;
        mSliceId = sliceId;

        mNumeratorBounds = new Rect();
        mNumeratorBounds.set(0, 0, numeratorSize, numeratorSize);
        mNumeratorPaint = new Paint();
        mNumeratorPaint.setAntiAlias(true);
        mNumeratorPaint.setColor(color);
        mNumeratorPaint.setStyle(Paint.Style.FILL);

        setLabelBounds(label, Legend.legendFontSize);
        mLabelPaint = new TextPaint();
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setColor(Color.BLACK);
        mLabelPaint.setTextSize(Legend.legendFontSize);//todo mache die textgröße abhängig von der Bildschrimauflösung
        //Math.round(mNumeratorSize * 0.3 * getResources().getDisplayMetrics().scaledDensity)

        resolveNumeratorHeight();
    }

    /**
     * Methode um die Position des Numerators anzupassen, falls der Text größer sein sollte als der Numerator.
     */
    private void resolveNumeratorHeight() {

        if (getNumeratorHeight() < getLabelHeight()) {

            int offset = getLabelHeight() - getNumeratorHeight();
            mNumeratorBounds.top += offset;
            mNumeratorBounds.bottom += offset;
        }
    }

    public String getSliceId() {
        return mSliceId;
    }

    public Paint getNumeratorPaint() {
        return mNumeratorPaint;
    }

    public Rect getNumeratorBounds() {
        return mNumeratorBounds;
    }

    public int getNumeratorWidth() {
        return mNumeratorBounds.width();
    }

    public int getNumeratorHeight() {
        return mNumeratorBounds.width();
    }

    public Paint getLabelPaint() {
        return mLabelPaint;
    }

    public int getLabelWidth() {
        return mLabelBounds.width();
    }

    public int getLabelHeight() {
        return mLabelBounds.height();
    }

    public int getLabelX() {
        return mLabelBounds.left;
    }

    public int getLabelY() {
        return mLabelBounds.bottom;
    }

    public int getWidth() {
        return getNumeratorWidth() + getLabelWidth();
    }

    public int getHeight() {
        return Math.max(getNumeratorHeight(), getLabelHeight());
    }

    public int getStartX() {
        return mNumeratorBounds.left;
    }

    public int getEndX() {
        return mNumeratorBounds.left + getWidth();
    }

    public int getStartY() {
        return mNumeratorBounds.top;
    }

    public int getEndY() {
        return mNumeratorBounds.top + getHeight();
    }

    @ColorInt
    public int getColor() {
        return mColor;
    }

    @NonNull
    public String getLabel() {
        return mLabel;
    }

    @NonNull
    public NumeratorStyles getStyle() {
        return mNumeratorStyle;
    }

    /**
     * Methode um das Legendenelement um die angegebenen Werte zu verschieben
     *
     * @param xShift Angabe um wie viel das Legendenelement in der X-Achse verschoben werden soll
     * @param yShift Angabe um wie viel das Legendenelement in der Y-Achse verschoben werden soll
     */
    void shift(int xShift, int yShift) {
        mNumeratorBounds.left += xShift;
        mNumeratorBounds.top += yShift;
        mNumeratorBounds.right += xShift;
        mNumeratorBounds.bottom += yShift;

        mLabelBounds.left += xShift;
        mLabelBounds.top += yShift;
        mLabelBounds.right += xShift;
        mLabelBounds.bottom += yShift;
    }

    /**
     * Methode den von einem String eingenommenen Platz zu ermitteln
     * source: https://stackoverflow.com/a/4795393
     *
     * @param text Text dessen Größe bestimmt werden soll
     */
    private void setLabelBounds(String text, float textSize) {
        mLabelBounds = new Rect();

        Paint paint = new Paint();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, text.length(), mLabelBounds);

        mLabelBounds.right += mNumeratorBounds.width();
        mLabelBounds.left = mNumeratorBounds.right;
        mLabelBounds.bottom = getHeight();
        mLabelBounds.top = 0;
    }
}
