package com.haushaltsmanager.charts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.haushaltsmanager.charts.Legend.Legend;
import com.haushaltsmanager.charts.Legend.LegendItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PieChart extends ViewUtils {
    private final static String TAG = PieChart.class.getSimpleName();

    private Legend mLegend;
    private boolean mDrawLegend;

    /**
     * Flag die angibt, ob die Labels neben den Legendenelementen angezeigt werden sollen oder nicht.
     */
    private boolean mDrawLegendLabels;
    private Paint mNumeratorPaint;

    /**
     * Legendencontainer
     */
    private Rect mLegendBounds;

    private LegendItem.NumeratorStyles mNumeratorStyle;

    private LegendPositions mLegendPosition;

    public enum LegendPositions {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private LegendDirections mLegendDirection;

    public enum LegendDirections {
        LEFT_TO_RIGHT, TOP_TO_BOTTOM
    }

    private List<List<PieSlice>> mPieData = new ArrayList<>();
    private float mDataTotal;
    private Paint mSlicePaint;
    private RectF mPieChartBounds;
    private String mNoDataText;
    private boolean mPercentageValues = false;
    private boolean mSliceMargin;
    private boolean mDrawCenterText = false;
    private String mCenterText = "";
    private float mFontSize = 35f;
    private Paint mFontPaint;
    private Boolean mTouchEnabled = false;

    private boolean mDrawHole;
    private float mHoleRadius;
    private double mHoleSize;
    private Paint mHolePaint;

    private boolean mCompressed;
    private int mLayer;
    private int mVisibleLayer = 1;

    private boolean mTransparentCircle;
    private Paint mTransparentPaint;

    private ValueAnimator mAnimator;

    public PieChart(Context context) {
        super(context);
        init(context, null, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Methode um die View klasse zu initialisieren
     *
     * @param context      Context
     * @param attrs        Attribute, die im XML Dokument gesetzt wurden
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PieChart,
                defStyleAttr,
                0);

        int holeColor;
        int numeratorColor;

        try {
            mDrawHole = a.getBoolean(R.styleable.PieChart_draw_hole, false);
            mHoleSize = a.getInteger(R.styleable.PieChart_hole_size, 50);//todo hole size kann nur zwischen 1 und 100 liegen, wenn andere werte angegeben werden sollte eine Exception (InvalidArgumentException) geworfen werden
            holeColor = a.getColor(R.styleable.PieChart_hole_color, Color.WHITE);
            mNoDataText = a.getString(R.styleable.PieChart_no_data_text);
            mLegendPosition = LegendPositions.TOP_LEFT;//todo legende kann noch nicht rechts platziert werden
            mDrawLegend = a.getBoolean(R.styleable.PieChart_use_legend, false);
            mNumeratorStyle = LegendItem.NumeratorStyles.SQUARE;
            mTransparentCircle = a.getBoolean(R.styleable.PieChart_use_transparent_circle, false);
            mCompressed = a.getBoolean(R.styleable.PieChart_use_compressed, false);
            mLayer = a.getInt(R.styleable.PieChart_layer_depth, 3);
            mSliceMargin = a.getBoolean(R.styleable.PieChart_draw_slice_margin, false);
            mCenterText = a.getString(R.styleable.PieChart_center_text);
            numeratorColor = a.getColor(R.styleable.PieChart_numerator_color, Color.BLACK);
            mLegendDirection = LegendDirections.LEFT_TO_RIGHT;//todo wenn die legende vertikal ist, klickt man kurz nach dem letzten element auf den chart, stürzt die app ab
        } finally {
            a.recycle();
        }

        if (mDrawHole) {

            mHolePaint = new Paint();
            mHolePaint.setAntiAlias(true);
            mHolePaint.setStyle(Paint.Style.FILL);
            mHolePaint.setColor(holeColor);
        }

        if (mTransparentCircle && mDrawHole) {

            mTransparentPaint = new Paint();
            mTransparentPaint.setAntiAlias(true);
            mTransparentPaint.setStyle(Paint.Style.FILL);
            mTransparentPaint.setColor(holeColor);
            mTransparentPaint.setAlpha(100);
        }

        if (mDrawLegend) {

            mNumeratorPaint = new Paint();
            mNumeratorPaint.setAntiAlias(true);
            mNumeratorPaint.setStyle(Paint.Style.FILL);
            mNumeratorPaint.setColor(numeratorColor);
        }

        if (mNoDataText == null)
            mNoDataText = getResources().getString(R.string.no_data);

        mSlicePaint = new Paint();
        mSlicePaint.setAntiAlias(true);
        mSlicePaint.setDither(true);
        mSlicePaint.setStyle(Paint.Style.FILL);

        mViewBounds = new RectF();
        mPieChartBounds = new RectF();
        mLegendBounds = new Rect();

        mFontPaint = new Paint();
        mFontPaint.setAntiAlias(true);
        mFontPaint.setColor(getContext().getResources().getColor(R.color.alert_text_color));
        mFontPaint.setTextSize(mFontSize);

        mLegend = new Legend(mLegendDirection);
    }

    public void setHoleColor(@ColorInt int color) {
        mHolePaint.setColor(color);
    }

    public int getHoleColor() {

        return mHolePaint.getColor();
    }

    public void useCompressedChart(boolean value) {

        this.mCompressed = value;
        invalidate();
        requestLayout();
    }

    public boolean isCompressed() {
        return this.mCompressed;
    }

    public boolean isHoleEnabled() {
        return mDrawHole;
    }

    public void setNumeratorColor(@ColorInt int color) {
        mNumeratorPaint.setColor(color);
        invalidate();
    }

    public int getNumeratorColor() {
        return this.mNumeratorPaint.getColor();
    }

    public void drawHole(boolean drawHole) {

        if (drawHole != mDrawHole) {

            mDrawHole = drawHole;
            invalidate();
            requestLayout();
        }
    }

    public void setCenterText(@NonNull String centerText) {
        mCenterText = centerText;
    }

    @NonNull
    public String getCenterText() {
        return mCenterText;
    }

    public void enableCenterText() {
        mDrawCenterText = true;
    }

    public void disableCenterText() {
        mDrawCenterText = false;
    }

    public boolean isCenterTextDrawingEnabled() {
        return mDrawCenterText;
    }

    public void setCenterTextColor(@ColorInt int textColor) {
        mFontPaint.setColor(textColor);
    }

    public void setNoDataText(@NonNull String noDataText) {
        mNoDataText = noDataText;
    }

    public void setNoDataText(@StringRes int noDataText) {
        mNoDataText = getContext().getResources().getString(noDataText);
    }


    //Ab hier werden neue PieDaten gesetzt


    /**
     * Methode um den PieChart mit einem neuen Datensatz zu befüllen
     *
     * @param pieData Liste mit Datensätzen
     */
    public void setPieData(List<DataSet> pieData) {
        if (pieData.size() == 0) {
            clearData();
            invalidate();
            return;
        }

        mTouchEnabled = true;

        mDataTotal = getTotal(pieData);
        preparePieData(pieData);
        convertToSlices(pieData);

        if (mCompressed)
            createCompressedSlices(getTotal(pieData));

        createLegendItems(getVisibleSlices());
        invalidate();
    }

    private void clearData() {
        mDataTotal = 0;
        mPieData.clear();
    }

    /**
     * Methode um das Legendenelement mit LegendenItems zu befüllen
     */
    private void createLegendItems(List<PieSlice> visiblePieSlices) {
        mLegend.clear();

        for (PieSlice slice : visiblePieSlices) {
            mLegend.addItem(
                    slice.getLabel(),
                    slice.getColor(),
                    mNumeratorStyle,
                    slice.getId()
            );
        }
    }

    /**
     * Methode um bereits existierende Daten zu löschen und die vom User gegebene Liste aufsteigend zu sortieren
     *
     * @param dataSets Vom User gegebene Liste
     */
    private void preparePieData(List<DataSet> dataSets) {
        mPieData.clear();
        Collections.sort(dataSets);
    }

    /**
     * Methode um die vom User gegebenen Datensätze in PieSlice Objekte umzuwandeln
     *
     * @param dataSets Vom User gegebene Daten
     */
    private void convertToSlices(List<DataSet> dataSets) {
        float startAngle = 0;
        List<PieSlice> defaultLayer = new ArrayList<>();
        for (DataSet dataSet : dataSets) {

            float percentValue = getPieArea(mDataTotal, dataSet.getValue());
            PieSlice slice = new PieSlice(dataSet.getValue(), percentValue, startAngle);
            slice.setWeight(mLayer);
            slice.setSliceColor(dataSet.getColor());
            slice.setSliceLabel(dataSet.getLabel());

            defaultLayer.add(slice);
            startAngle += percentValue;
        }

        mPieData.add(defaultLayer);
    }

    /**
     * Methode um einen Prozentualen Wert zu einem absoluten Wert zu bekommen
     *
     * @param total     Totale Kreisfläche
     * @param dataValue Wert welcher auf die Kreisfläche umgerechnet werden soll
     * @return Eingenommener Wert in Prozent
     */
    private float getPieArea(float total, float dataValue) {

        return dataValue / total * 360f;
    }

    /**
     * Methode um den PieSlices ein passendes Gewicht zuzuordnen. Dabei werden in einem Layer ca. 2/3 des Gesamtvolumens angezeigt.
     * Außerdem werden die Slices basierend auf ihrem Gewicht in die passende liste in mPieData eingeordnet.
     */
    private void createCompressedSlices(float total) {

        //todo gewichts zuordnung noch einmal überarbeiten

        //die layer information wird mit leeren listen befüllt die dann später die slices der verschiedenen layer enthalten
        for (int i = 0; i < mLayer; i++) {

            mPieData.add(new ArrayList<PieSlice>());
        }

        //hier werden den slices passende gewichte zugeordnet und außerdem werden sie gleich richtig sortiert
        float tempTotal = 0;
        int currentWeight = mLayer;
        for (PieSlice slice : mPieData.get(0)) {

            if (slice.getAbsValue() + tempTotal <= total * Math.pow(0.33, currentWeight - 1))
                slice.setWeight(currentWeight);
            else
                slice.setWeight(--currentWeight);

            mPieData.get(slice.getWeight()).add(slice);
            tempTotal += slice.getAbsValue();
        }
        mPieData.remove(0);

        //den Slices muss basierend auf ihrem gewicht ein neuer Bereich zugewiesen werden, außerdem muss für jedes außer das letzte layer ein platzhalter mit eingefügt werden
        tempTotal = total;
        for (List<PieSlice> layerSlicesList : mPieData) {

            float startAngle = 0, currentTotal = 0;

            for (PieSlice slice : layerSlicesList) {

                slice.setStartAngle(startAngle);
                slice.setPercentValue(getPieArea(tempTotal, slice.getAbsValue()));

                startAngle += slice.getPercentValue();
                currentTotal += slice.getAbsValue();
            }

            if (mPieData.size() - 1 != mPieData.indexOf(layerSlicesList)) {

                PieSlice slice = new PieSlice(tempTotal - currentTotal, getPieArea(tempTotal, tempTotal - currentTotal), startAngle);
                slice.setSliceLabel(getResources().getString(R.string.others));
                layerSlicesList.add(slice);
            }

            tempTotal -= currentTotal;
        }
    }

    /**
     * Methode um den aufsummierten Wert der dataSets zu erhalten.
     *
     * @param dataSets Aufzusummierende Datensätze
     * @return Aufummierter Wert
     */
    private float getTotal(List<DataSet> dataSets) {
        float total = 0;
        for (DataSet dataSet : dataSets)
            total += dataSet.getValue();

        return total;
    }


    //ab hier wird die Größe des PieCharts bestimmt


    /**
     * Methode um den minimalen Platz des PieCharts in pixeln zu ermitteln
     *
     * @return PieChartbounds
     */
    private Rect getChartDesiredSize() {

        return new Rect(0, 0, dpToPx(200), dpToPx(200));
    }

    /**
     * Methode um die größe der View zu ermitteln
     *
     * @param widthMeasureSpec  Breiteninformationen
     * @param heightMeasureSpec Höheninformationen
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMaxViewBounds(widthMeasureSpec, heightMeasureSpec);

        //padding von der maximalen größe abziehen
        int widthPadding = getPaddingRight() + getPaddingLeft();
        int heightPadding = getPaddingTop() + getPaddingBottom();
        Rect sizeWithPadding = new Rect(0, 0, (int) mViewBounds.width() - widthPadding, (int) mViewBounds.height() - heightPadding);


        //herausfinden wie groß die legende sein kann
        int minChartSize = dpToPx(200);
        resolveLegendSize(sizeWithPadding, minChartSize);
        mPieChartBounds = resolveChartSize(mLegendBounds, sizeWithPadding);

        applyPaddingToChartAndLegend(mLegendBounds, mPieChartBounds);
        setMeasuredDimension((int) mViewBounds.width(), (int) mViewBounds.height());
    }

    /**
     * Methode um die maximale Größe der View zu ermitteln
     *
     * @param widthMeasureSpec  Breiteninformationen
     * @param heightMeasureSpec Höheninformationen
     */
    private void setMaxViewBounds(int widthMeasureSpec, int heightMeasureSpec) {

        mViewBounds.set(0, 0, 0, 0);
        if (isLegendBottom() || isLegendTop()) {

            mViewBounds.right = reconcileSize(Math.max(getChartDesiredSize().width(), mLegend.getDesiredSize().width()), widthMeasureSpec);
            mViewBounds.bottom = reconcileSize(getChartDesiredSize().height() + mLegend.getDesiredSize().height(), heightMeasureSpec);
        } else {

            mViewBounds.right = reconcileSize(getChartDesiredSize().width() + mLegend.getDesiredSize().width(), widthMeasureSpec);
            mViewBounds.bottom = reconcileSize(Math.max(getChartDesiredSize().height(), mLegend.getDesiredSize().height()), heightMeasureSpec);
        }
    }


    /**
     * Methode um die größe der Legende zu bestimmen.
     * Ist zu wenig platz werden zuerst die Label entfernt,
     * ist immer noch zu wenig platz werden auch die Numeratoren nicht mehr angezeigt.
     * Wenn dann immer noch zu wenig platz sein sollte wird ein leeres Quadrat zurückgegeben.
     *
     * @param availableSpace Maximal verfügbarer Platz
     * @param minChartSize   Größe die der Chart mindestens einnehmen wird
     */
    private void resolveLegendSize(Rect availableSpace, int minChartSize) {
        Rect availableLegendSpace = getAvailableLegendSpace(availableSpace, minChartSize);

        if (availableLegendSpace.contains(mLegend.getDesiredSize())) {
            mLegendBounds = mLegend.getDesiredSize();
            mDrawLegendLabels = true;
        } else if (availableLegendSpace.contains(mLegend.getMinimumSize())) {
            mLegendBounds = mLegend.getMinimumSize();
            mDrawLegendLabels = false;
        } else {
            mLegendBounds = new Rect();
            mDrawLegendLabels = false;
        }
    }

    /**
     * Methode um die Größe des PieCharts zu bestimmen.
     * Ist der PieChart nach der bestimmung nicht mehr quadratisch, wird dies gemacht.
     *
     * @param legendBounds   Größe der Legende
     * @param availableSpace Verfügbarer platz
     * @return PieChart bounds mit angepasster Größe
     */
    private RectF resolveChartSize(Rect legendBounds, Rect availableSpace) {

        mPieChartBounds.left = 0;
        mPieChartBounds.top = 0;
        if (isLegendBottom() || isLegendTop()) {

            //ist die breite größer als die höhe
            mPieChartBounds.right = availableSpace.width();
            mPieChartBounds.bottom = availableSpace.height() - legendBounds.height();
        } else {

            mPieChartBounds.right = availableSpace.width() - legendBounds.width();
            mPieChartBounds.bottom = availableSpace.height();
        }

        //den chart wieder quadratisch machen
        if (mPieChartBounds.width() != mPieChartBounds.height()) {

            float smallerSide = Math.min(mPieChartBounds.width(), mPieChartBounds.height());
            mPieChartBounds.right = smallerSide;
            mPieChartBounds.bottom = smallerSide;
        }

        return mPieChartBounds;
    }

    /**
     * Methode um die Legende und den PieChart in den ViewBounds zu platzieren.
     *
     * @param legendBounds   Den von der Legende eingenommenen Platz
     * @param pieChartBounds Den von dem PieChart eingenommenen Platz
     */
    private void applyPaddingToChartAndLegend(Rect legendBounds, RectF pieChartBounds) {

        if (!legendBounds.isEmpty()) {
            if (isLegendLeft()) {

                legendBounds.left = getPaddingLeft();
                legendBounds.top = getPaddingTop();
                legendBounds.right += legendBounds.left;
                legendBounds.bottom += legendBounds.top;

                pieChartBounds.left = legendBounds.right;
                pieChartBounds.top += getPaddingTop();
                pieChartBounds.right += legendBounds.width();
            } else if (isLegendTop()) {

                legendBounds.left = getPaddingLeft();
                legendBounds.top = getPaddingTop();
                legendBounds.right += legendBounds.left;
                legendBounds.bottom += legendBounds.top;

                pieChartBounds.left = getPaddingLeft();
                pieChartBounds.top = legendBounds.bottom;
                pieChartBounds.bottom += legendBounds.height();
            } else if (isLegendRight()) {

                pieChartBounds.left = getPaddingLeft();
                pieChartBounds.top = getPaddingTop();

                legendBounds.left = (int) pieChartBounds.right;
                legendBounds.top = getPaddingTop();
                legendBounds.right += legendBounds.left;
                legendBounds.bottom += legendBounds.top;
            } else if (isLegendBottom()) {

                pieChartBounds.left = getPaddingLeft();
                pieChartBounds.top = getPaddingTop();

                legendBounds.left = getPaddingLeft();
                legendBounds.top = (int) pieChartBounds.bottom;
                legendBounds.right += legendBounds.left;
                legendBounds.bottom += legendBounds.top;
            }
        }
    }

    /**
     * Methode um den Platz zu ermitteln, welcher der Legende zur verfügung steht,
     * basierend auf dem Maximalen Platz und einem weitern Objekt, welches bereits in den Bounds ist
     *
     * @param bounds       Maximaler zur verfügung stehender Platz
     * @param minChartSize Weiteres Objekt, welches in den bounds platziert werden soll
     * @return Verfügbarer Legendenplatz
     */
    private Rect getAvailableLegendSpace(Rect bounds, int minChartSize) {

        Rect availableSpace = new Rect(bounds);
        if (mLegendDirection.equals(LegendDirections.LEFT_TO_RIGHT)) {
            //finde heraus wie viel platz über bzw. unter dem piechart noch zur verfügung steht
            if (isLegendTop()) {
                //finde heraus wie viel platz über dem piechart noch zur verfügung steht
                availableSpace.bottom = bounds.height() - minChartSize;
            } else if (isLegendBottom()) {
                //finde heraus wie viel plart unter dem piechart noch zur verfügung steht
                availableSpace.top = bounds.top + minChartSize;
            }
        } else if (mLegendDirection.equals(LegendDirections.TOP_TO_BOTTOM)) {
            //finde heraus auf welcher seite die legende sein soll
            if (isLegendLeft()) {
                //finde heraus wie viel platz links neben dem piechart noch zur verfügung steht
                availableSpace.right = bounds.width() - minChartSize;
            } else if (isLegendRight()) {
                //finde heraus wie viel platz rechts neben dem piechart noch zur verfügung steht
                availableSpace.left = bounds.left + minChartSize;
            }
        }

        return availableSpace;
    }

    private boolean isLegendTop() {

        return (mLegendPosition.equals(LegendPositions.TOP_LEFT) || mLegendPosition.equals(LegendPositions.TOP_CENTER) || mLegendPosition.equals(LegendPositions.TOP_RIGHT)) && mLegendDirection.equals(LegendDirections.LEFT_TO_RIGHT);
    }

    private boolean isLegendBottom() {

        return (mLegendPosition.equals(LegendPositions.BOTTOM_LEFT) || mLegendPosition.equals(LegendPositions.BOTTOM_CENTER) || mLegendPosition.equals(LegendPositions.BOTTOM_RIGHT)) && mLegendDirection.equals(LegendDirections.LEFT_TO_RIGHT);
    }

    private boolean isLegendLeft() {

        return (mLegendPosition.equals(LegendPositions.TOP_LEFT) || mLegendPosition.equals(LegendPositions.BOTTOM_LEFT)) && mLegendDirection.equals(LegendDirections.TOP_TO_BOTTOM);
    }

    private boolean isLegendRight() {

        return (mLegendPosition.equals(LegendPositions.TOP_RIGHT) || mLegendPosition.equals(LegendPositions.BOTTOM_RIGHT)) && mLegendDirection.equals(LegendDirections.TOP_TO_BOTTOM);
    }

    //ab hier werden visuelle sachen behandelt

    /**
     * Methode um die momentan sichtbaren PieSlices aus dem gesamten Datensatz zu erhalten.
     *
     * @return Momentan sichtbare PieSlices
     */
    private List<PieSlice> getVisibleSlices() {
        if (mPieData.isEmpty())
            return new ArrayList<>();
        else
            return mPieData.get(mVisibleLayer - 1);
    }

    /**
     * Methode um das Kreisdiagramm zu zeichnen
     *
     * @param canvas Zeichen Objekt auf dem der Kreis gezeichet wird
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (mPieData.size() == 0) {
            drawNoDataText(canvas);
            return;
        }

        for (PieSlice slice : getVisibleSlices()) {

            mSlicePaint.setColor(slice.getColor());
            if (mSliceMargin)
                canvas.drawArc(mPieChartBounds, slice.getStartAngle(), slice.getPercentValue() - 2f, true, mSlicePaint);
            else
                canvas.drawArc(mPieChartBounds, slice.getStartAngle(), slice.getPercentValue(), true, mSlicePaint);
        }

        if (mDrawHole)
            drawHole(canvas);

        if (!isLegendHidden())
            drawLegend(canvas);

        if (mDrawCenterText)
            drawCenterText(canvas, mCenterText);
    }

    /**
     * Method um innnerhalb des Kreisdiagramms ein Loch einzufügen, sodass das Diagramm wie eine "Donut" aussieht
     *
     * @param canvas Das Object auf dem gezeichnet wurde
     */
    private void drawHole(Canvas canvas) {
        mHoleRadius = (float) (mPieChartBounds.width() / 2 * (mHoleSize / 100));

        canvas.drawCircle(mPieChartBounds.centerX(), mPieChartBounds.centerY(), mHoleRadius, mHolePaint);

        if (mTransparentCircle)
            canvas.drawCircle(mPieChartBounds.centerX(), mPieChartBounds.centerY(), mHoleRadius + (mHoleRadius * 0.25f), mTransparentPaint);
    }

    /**
     * Methode um einen Default text anzeigen zu lassen, wenn dem Kreisdiagramm keine Daten zugrunde legen.
     *
     * @param canvas Objekt, auf dem der Text angezeigt werden soll.
     */
    private void drawNoDataText(Canvas canvas) {

        drawCenterText(canvas, mNoDataText);
    }

    /**
     * Methode um innerhalb des Kreises einen Text anzeigen zu lassen.
     * VORSICHT: Der angezeigt Text wird mittig platziert und wird nicht weitergehend platziert.
     * Ist der Text zu lang kann er in das Diagramm hineinragen oder gar aus der View hinaus.
     *
     * @param canvas        Canvas auf der der Text angezeigt wird
     * @param displayString Text der angezeigt werden soll
     *///todo Der angezeigte Text muss sich dem verfügbaren Platz anpassen (Zeilenumbruch)
    private void drawCenterText(Canvas canvas, String displayString) {

        Rect textBounds = getTextBounds(displayString, mFontSize);
        canvas.drawText(displayString, mViewBounds.centerX() - textBounds.width() / 2, mViewBounds.centerY() - textBounds.height() / 2, mFontPaint);
    }

    /**
     * Methode um Legendenelemente eines PieCharts anzeigen zu lassen.
     *
     * @param canvas Canvas auf dem die Legende sichtbar sein soll
     */
    private void drawLegend(Canvas canvas) {
        if (isLegendHidden())
            return;

        for (LegendItem legendItem : mLegend.getLegendItems()) {
            drawLegendItem(canvas, legendItem);
        }
    }

    /**
     * Methode um ein einzelnes Legendenelement zu zeichnen.
     *
     * @param canvas     Canvas auf dem das Legendelement sichtbar sein soll
     * @param legendItem Legendenelement, welches auf die Canvas gezeichnet werden soll
     */
    private void drawLegendItem(Canvas canvas, LegendItem legendItem) {

        canvas.drawRect(legendItem.getNumeratorBounds(), legendItem.getNumeratorPaint());
        if (mDrawLegendLabels)
            canvas.drawText(legendItem.getLabel(), legendItem.getLabelX(), legendItem.getLabelY(), legendItem.getLabelPaint());
    }

    /**
     * Methode die überprüft, ob die Legende sichtbar ist oder nicht.
     * Dabei wird der tatsächlich ausgerechnete Platz der Legende als Referenz genommen.
     *
     * @return Boolean
     */
    private boolean isLegendHidden() {
        return mLegendBounds.isEmpty() || !mDrawLegend;
    }

    //ab hier werden klick Ereignisse behandelt

    /**
     * Methode um Touch aktionen des Users abzufangen
     *
     * @param event Interaktionsart des Users
     * @return Boolean ob das event gehandelt wurde
     *///todo die funktion perfomClick einfügen und die click logik dorthin verlagern, da klicks auch anders ausgelöst werden können
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mTouchEnabled)
            return true;

        Point click = new Point((int) Math.floor(event.getX()), (int) Math.floor(event.getY()));

        //User nimmt den Finger vom Display
        if (event.getAction() == MotionEvent.ACTION_UP) {

            switch (clickedArea(click)) {

                case 0://PieChart
                    Log.d(TAG, "onTouchEvent: Du hast den PieChart angeklickt!");

                    float pointAngle = getAngleForPoint(click);

                    for (PieSlice slice : getVisibleSlices()) {

                        if (pointAngle > slice.getStartAngle() && pointAngle < slice.getEndAngle()) {

                            Log.d(TAG, "Du bist auf Ebene " + mVisibleLayer + " und hast gerade in den Bereich mit dem Wert " + slice.getAbsValue() + " geklickt!");

                            if (slice.getWeight() != mVisibleLayer && mCompressed) {
                                mVisibleLayer++;
                                createLegendItems(getVisibleSlices());
                            }
                            break;
                        }
                    }
                    break;
                case 1://Legend
                    LegendItem item = mLegend.getItemForPoint(click);
                    PieSlice slice = getSliceWithId(item.getSliceId());
                    Log.d(TAG, "Du hast auf das Legendenelement " + item.getLabel() + " geklickt!");

                    fadeSliceInOut(slice);

                    break;
                default://nothing
                    break;
            }
            invalidate();
        }

        return true;
    }

    /**
     * Methode die einen PieSlice aus den aktuell sichtbaren PieSlices mit einer bestimmten id holt
     *
     * @param sliceId Id des zu findenden slices
     * @return PieSlice mit der angegebenen Id oder null, falls keiner existiert
     */
    private PieSlice getSliceWithId(String sliceId) {
        for (PieSlice slice : getVisibleSlices()) {
            if (slice.getId().equals(sliceId))
                return slice;
        }
        return null;
    }

    /**
     * Methode gibt einen code für den angeklickten Punkt zurück
     *
     * @param click Punkt auf den gecklickt wurde
     * @return 0 wenn auf den PieChart geklickt wurde, 1 wenn auf die Legende geklickt wurde, -1 wenn "nichts" angeklickt wurde
     */
    private int clickedArea(Point click) {

        if (mDrawHole && Math.sqrt(Math.pow((mPieChartBounds.centerX() - click.x), 2) + Math.pow((mPieChartBounds.centerY() - click.y), 2)) <= mHoleRadius)
            return -1;//hole click

        if (mLegendBounds.contains(click.x, click.y))
            return 1;//mLegend click

        if (Math.sqrt(Math.pow((mPieChartBounds.centerX() - click.x), 2) + Math.pow((mPieChartBounds.centerY() - click.y), 2)) <= mPieChartBounds.width() / 2)
            return 0;//chart click

        return -1;//nothing click
    }

    /**
     * Methode um den Winkel eines gegebenen Punktes im bezug zum Mittelpunkt zu berechnen, jedoch nur wenn sich der Punkt innerhalb des Kreises befindet
     *
     * @param point Punkt auf den gecklickt wurde
     * @return Winkel eines Punktes bezogen auf den Mittelpunkt oder -1 falls der Punkt nicht innerhalb der Kreises liegt
     */
    private float getAngleForPoint(Point point) {

        double tx = point.x - mPieChartBounds.centerX(), ty = point.y - mPieChartBounds.centerY();
        double length = Math.sqrt(tx * tx + ty * ty);
        double r = Math.acos(ty / length);

        float angle = (float) Math.toDegrees(r);

        if (point.x > mPieChartBounds.centerX())
            angle = 360f - angle;

        angle += 90f;

        if (angle > 360f)
            angle -= 360f;

        return angle;
    }

    /**
     * Methode um den totalen Wert der aktuell sichtbaren PieSlices zu errechnen.
     * Diese Methode wird zum animieren benutzt und nutzt den mAnimValue des PieSlices.
     *
     * @return LayerTotal wert
     */
    private float getLayerTotal() {

        float total = 0;

        for (PieSlice slice : getVisibleSlices()) {

            total += slice.getAnimValue();
        }

        return total;
    }

    /**
     * Methode um die SliceSize des aktuell sichtbarem layers im PieChart zu errechnen.
     * Diese Methode wird zum animieren benutzt und nutzt den mAnimValue des PieSlices.
     */
    private void resolveSliceSize() {

        float total = getLayerTotal();
        int startAngle = 0;

        for (PieSlice slice : getVisibleSlices()) {

            slice.setPercentValue(getPieArea(total, slice.getAnimValue()));
            slice.setStartAngle(startAngle);

            startAngle += slice.getPercentValue();
        }
    }

    /**
     * Mit dieser Methode werden die einzelnen PieSlices animiert.
     *
     * @param slice PieSlice der animiert weden soll
     */
    private void fadeSliceInOut(final PieSlice slice) {

        //todo wenn nur noch ein slice zu sehen ist, kann dieser nicht mehr versteck werden!
        //todo die Slices werden beim aus und einklappen nicht richtig berechnet, sie kommen nicht auf 360°
        stopOngoingAnimations();

        if (slice.getPercentValue() > 0f)
            mAnimator = ValueAnimator.ofFloat(slice.getAbsValue(), 0f);
        else
            mAnimator = ValueAnimator.ofFloat(0f, slice.getAbsValue());

        mAnimator.setDuration(1000); // 1 sekunde
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value = (float) animation.getAnimatedValue();
                slice.setAnimValue(value);

                resolveSliceSize();

                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * Methode um alle laufenden Animationen anzuhalten
     */
    private void stopOngoingAnimations() {
        if (mAnimator != null)
            mAnimator.end();
    }
}
