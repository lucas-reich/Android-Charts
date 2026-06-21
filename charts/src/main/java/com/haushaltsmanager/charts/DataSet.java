package com.haushaltsmanager.charts;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class DataSet implements Comparable {

    @ColorInt
    private int color;
    private float value;
    private String label;

    public DataSet(float dataValue, @ColorInt int dataColor, @NonNull String dataLabel) {

        this.value = dataValue;
        this.color = dataColor;
        this.label = dataLabel;
    }

    public float getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    /**
     * Methode die das sortieren einer (Array-)Liste ermöglicht.
     * Die so sortierte Liste wird aufsteigend nach dem 'value' sortiert
     * source: https://beginnersbook.com/2013/12/java-arraylist-of-object-sort-example-comparable-and-comparator/
     *
     * @param o Zu Vergleichendes Objekt
     * @return int > 0, wenn dieses Objekt größer ist als 'o'
     */
    @Override
    public int compareTo(@NonNull Object o) {
        DataSet otherDataSet = (DataSet) o;

        return (int) (this.getValue() - otherDataSet.getValue());
    }
}
