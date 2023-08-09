package com.marcus.getFormData.util;

import com.marcus.getFormData.model2.DataItem;
import com.marcus.getFormData.model2.BoxPoint;

import java.util.Comparator;

public class DataItemComparator implements Comparator<DataItem> {
    private static final Double Y_TOLERANCE = 20.0;

    @Override
    public int compare(DataItem item1, DataItem item2) {
        int minY1 = getMinY(item1);
        int minY2 = getMinY(item2);

        if (Math.abs(minY1 - minY2) < Y_TOLERANCE) {
            // If y-coordinates are close enough, compare by x-coordinate
            return Integer.compare(getMinX(item1), getMinX(item2));
        }

        // Compare by y-coordinate
        return Integer.compare(minY1, minY2);
    }

    private int getMinX(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getX).min().orElse(0);
    }

    private int getMinY(DataItem item) {
        return item.getBoxPoints().stream().mapToInt(BoxPoint::getY).min().orElse(0);
    }
}
