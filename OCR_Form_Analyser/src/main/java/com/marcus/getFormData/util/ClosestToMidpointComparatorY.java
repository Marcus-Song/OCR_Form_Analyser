package com.marcus.getFormData.util;

import com.marcus.getFormData.model2.BoxPoint;
import com.marcus.getFormData.model2.DataItem;

import java.util.Comparator;

public class ClosestToMidpointComparatorY implements Comparator<DataItem> {
    private double midpoint;

    public ClosestToMidpointComparatorY(double midpoint) {
        this.midpoint = midpoint;
    }

    @Override
    public int compare(DataItem item1, DataItem item2) {
        int midpoint_Y1 = item1.getMidpointY();
        int midpoint_Y2 = item2.getMidpointY();

        // Calculate the absolute differences between each item and the midpoint_X
        double diffA = Math.abs(midpoint_Y1 - midpoint);
        double diffB = Math.abs(midpoint_Y2 - midpoint);

        // Compare the absolute differences and return the result
        // (Lower difference means the item is closer to the midpoint)
        return Double.compare(diffA, diffB);
    }

    private int getMinY(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getY).min().orElse(0);
    }

    private int getMaxY(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getY).max().orElse(0);
    }
}
