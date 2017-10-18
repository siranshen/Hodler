package com.illegalsimon.hodler.data;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by shens on 10/15/2017.
 */

public class ChartDateFormatter implements IAxisValueFormatter {

    private SimpleDateFormat mDateFormat;
    private long mBase; //Base for zero-based timestamps

    public ChartDateFormatter(TimeRange timeRange, long base) {
        mBase = base;
        String format;
        switch (timeRange) {
            case ONE_HOUR:
            case ONE_DAY:
                format = "HH:mm";
                break;
            case ONE_WEEK:
                format = "EEE";
                break;
            default:
                format = "M/d";
        }
        mDateFormat = new SimpleDateFormat(format, Locale.US);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mDateFormat.format(new Date(((long) value + mBase) * 1000));
    }
}
