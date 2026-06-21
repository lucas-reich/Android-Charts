package com.haushaltsmanager.charts;


import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.UUID;

/**
 * Klasse um die Datensets des Kreisdiagramms zu sortieren
 */
public class PieSlice {

    /**
     * Id, mit welcher ein Slice eindeutig identifizierbar ist
     */
    private String id;

    /**
     * Originaler vom User gegebener Wert
     */
    private float mAbsValue;

    /**
     * Basiert auf mAbsValue und ist zum animieren gedacht
     */
    private float mAnimValue;

    /**
     * Prozentualer Anteil am Kreis
     */
    private float mPercentValue;

    /**
     * Winkel ab dem der Bereich des Segments beginnt
     */
    private float mStartAngle;

    /**
     * Winkel ab dem der Bereich des Segments endet
     */
    private float mEndAngle;

    /**
     * Farbe des Kreissegments
     */
    private int mSliceColor;

    /**
     * Bezeichnung des Kreissegments
     */
    private String mSliceLabel;

    /**
     * Platz den das Kreissegment einnimt unterteilt in gewichte (Default 1).
     */
    private int mSliceWeight;

    PieSlice(float absValue, float percentValue, float startAngle) {

        constructor(absValue, percentValue, startAngle);
    }

    private void constructor(float absValue, float percentValue, float startAngle) {

        this.mAbsValue = absValue;
        this.mAnimValue = absValue;
        this.mPercentValue = percentValue;
        this.mStartAngle = startAngle;
        this.mEndAngle = mStartAngle + percentValue;
        this.mSliceColor = Color.WHITE;
        mSliceWeight = 0;
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setWeight(int weight) {

        this.mSliceWeight = weight;
    }

    public int getWeight() {

        return this.mSliceWeight;
    }

    public float getAbsValue() {

        return mAbsValue;
    }

    public float getAnimValue() {

        return this.mAnimValue;
    }

    public void setAnimValue(float animValue) {

        this.mAnimValue = animValue >= 0 ? animValue : 0f;
    }

    public int getColor() {

        return this.mSliceColor;
    }

    public void setSliceColor(@ColorInt int color) {

        this.mSliceColor = color;
    }

    @NonNull
    public String getLabel() {

        return this.mSliceLabel != null ? mSliceLabel : "";
    }

    public void setSliceLabel(@NonNull String label) {

        this.mSliceLabel = label;
    }

    public float getPercentValue() {

        return mPercentValue;
    }

    public void setPercentValue(float mCalcValue) {

        this.mPercentValue = mCalcValue;
        this.mEndAngle = this.mStartAngle + mCalcValue;
    }

    public float getStartAngle() {

        return mStartAngle;
    }

    public void setStartAngle(float mStartAngle) {

        this.mStartAngle = mStartAngle;
    }

    public float getEndAngle() {

        return mEndAngle;
    }
}
