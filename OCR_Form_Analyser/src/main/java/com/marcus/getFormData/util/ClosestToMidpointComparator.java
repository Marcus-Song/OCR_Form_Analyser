package com.marcus.getFormData.util;

import com.marcus.getFormData.model2.BoxPoint;
import com.marcus.getFormData.model2.DataItem;

import java.util.Comparator;

public class ClosestToMidpointComparator implements Comparator<DataItem> {
    private double midpoint;

    public ClosestToMidpointComparator(double midpoint) {
        this.midpoint = midpoint;
    }

    @Override
    public int compare(DataItem item1, DataItem item2) {
        int midpoint_X1 = item1.getMidpointX();
        int midpoint_X2 = item2.getMidpointX();

        // Calculate the absolute differences between each item and the midpoint_X
        double diffA = Math.abs(midpoint_X1 - midpoint);
        double diffB = Math.abs(midpoint_X2 - midpoint);

        // Compare the absolute differences and return the result
        // (Lower difference means the item is closer to the midpoint)
        return Double.compare(diffA, diffB);
    }

    private int getMinX(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getX).min().orElse(0);
    }

    private int getMaxX(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getX).max().orElse(0);
    }
}
