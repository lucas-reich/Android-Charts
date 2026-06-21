package com.haushaltsmanager.charts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class ViewUtils extends View {

    protected RectF mViewBounds;

    public ViewUtils(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ViewUtils(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ViewUtils(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected abstract void init(Context context, AttributeSet attrs, int defStyleAttr);

    /**
     * Methode um herauszufinden wie viel Platz maximal zur verfügung steht
     *
     * @param desiredSize Optimale Größe
     * @param measureSpec Kombinierter Wert aus Platz und Layout verhalten
     * @return Int
     */
    protected int reconcileSize(int desiredSize, int measureSpec) {

        int mode = MeasureSpec.getMode(measureSpec);
        int sizeInPx = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                return sizeInPx;
            case MeasureSpec.AT_MOST:
                return Math.min(sizeInPx, desiredSize);
            case MeasureSpec.UNSPECIFIED:
                return desiredSize;
            default:
                return -1;
        }
    }

    /**
     * Methode um Pixel in DensityPixel umzuwandeln.
     * source: https://stackoverflow.com/a/19953871/9376633
     *
     * @param px Zu konvertierende pixel
     * @return In Dp konvertierte pixel
     */
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Methode um DensityPixel in Pixel umzuwandeln.
     * source: https://stackoverflow.com/a/19953871/9376633
     *
     * @param dp Zu konvertierende dp
     * @return In px konvertierte dp
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Methode um die Helligkeit einer Farbe zu ermitteln.
     * Der Rückgabewert liegt zwischen 0 und 1, wobei 1 für weiß steht und 0 für schwarz
     * Quelle: https://www.w3.org/TR/2006/WD-WCAG20-20060427/appendixA.html#luminosity-contrastdef
     *
     * @param color Farbe
     * @return Helligkeit der Farbe (schwarz = 0, weiß = 1)
     */
    public static double getColorBrightness(String color) {
        int red = Color.red(Color.parseColor(color));
        int green = Color.green(Color.parseColor(color));
        int blue = Color.blue(Color.parseColor(color));

        return (0.2126 * Math.pow((red / 255), 2.2)) + (0.7152 * Math.pow((green / 255), 2.2)) + (0.0722 * Math.pow((blue / 255), 2.2));
    }

    /**
     * Methode den von einem String eingenommenen Platz zu ermitteln
     * source: https://stackoverflow.com/a/4795393
     *
     * @param text Text dessen Größe bestimmt werden soll
     * @return Textgröße in einem Rect gespeichert
     */
    protected Rect getTextBounds(String text, float textSize) {

        Rect textBounds = new Rect();

        Paint paint = new Paint();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, text.length(), textBounds);

        return textBounds;
    }
}
