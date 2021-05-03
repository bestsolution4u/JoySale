package com.hitasoft.app.external.mpchart;



/**
 * Class to format all values before they are drawn as labels.
 */
public abstract class ValueFormatter implements IAxisValueFormatter, IValueFormatter{

    /**
     * <b>DO NOT USE</b>, only for backwards compatibility and will be removed in future versions.
     *
     * @param value the value to be formatted
     * @param axis  the axis the value belongs to
     * @return formatted string label
     */
    @Override
    @Deprecated
    public String getFormattedValue(float value, AxisBase axis) {
        return getFormattedValue(value);
    }

    /**
     * <b>DO NOT USE</b>, only for backwards compatibility and will be removed in future versions.
     * @param value           the value to be formatted
     * @param entry           the entry the value belongs to - in e.g. BarChart, this is of class BarEntry
     * @param dataSetIndex    the index of the DataSet the entry in focus belongs to
     * @param viewPortHandler provides information about the current chart state (scale, translation, ...)
     * @return formatted string label
     */
    @Override
    @Deprecated
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return getFormattedValue(value);
    }

    /**
     * Called when drawing any label, used to change numbers into formatted strings.
     *
     * @param value float to be formatted
     * @return formatted string label
     */
    public String getFormattedValue(float value) {
        return String.valueOf(value);
    }

    /**
     * Used to draw axis labels, calls {@link #getFormattedValue(float)} by default.
     *
     * @param value float to be formatted
     * @param axis  axis being labeled
     * @return formatted string label
     */
    public String getAxisLabel(float value, AxisBase axis) {
        return getFormattedValue(value);
    }


    /**
     * Used to draw line and scatter labels, calls {@link #getFormattedValue(float)} by default.
     *
     * @param entry point being labeled, contains X value
     * @return formatted string label
     */
    public String getPointLabel(Entry entry) {
        return getFormattedValue(entry.getY());
    }

}
