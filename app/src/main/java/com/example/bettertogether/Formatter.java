package com.example.bettertogether;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class Formatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        return "" + ((int) value);
    }
}
