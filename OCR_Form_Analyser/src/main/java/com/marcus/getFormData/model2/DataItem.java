package com.marcus.getFormData.model2;

import com.marcus.getFormData.util.ClosestToMidpointComparator;
import com.marcus.getFormData.util.ClosestToMidpointComparatorY;
import com.marcus.getFormData.util.DataItemComparator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.marcus.getFormData.service.impl.DataServiceImpl.*;
import static java.lang.Integer.MAX_VALUE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataItem {
    // INDEX: 0:leftUp, 1:rightUp, 2:rightDown, 3:leftDown
    private List<BoxPoint> boxPoints;
    private String text;
    private double score;

    public void combine(DataItem nextElement) {
        this.setBoxPoints(this.boxPoints.get(0), this.boxPoints.get(1), nextElement.boxPoints.get(0), nextElement.boxPoints.get(0));
        this.setText(this.text + nextElement.getText());
        this.setScore((this.score + nextElement.score)/2);
    }

    private void setBoxPoints(BoxPoint a, BoxPoint b, BoxPoint c, BoxPoint d) {
        List<BoxPoint> boxPointList = new ArrayList<>();
        boxPointList.add(a);
        boxPointList.add(b);
        boxPointList.add(c);
        boxPointList.add(d);
        this.setBoxPoints(boxPointList);
    }

    public List<DataItem> getChildren(List<DataItem> dataItems, Double avgHorizontalSpace) {
        Double midpoint_X = this.getMidpointX() + 0.0;
        List<DataItem> output = new ArrayList<>();
        dataItems.sort(new ClosestToMidpointComparator(midpoint_X));
        //Arrays.sort(dataItems.toArray(new DataItem[0]), new ClosestToMidpointComparator(midpoint_X));
            for (DataItem item : dataItems)
                if (item != null)
                    if (item.getMidpointX() <= (midpoint_X + avgHorizontalSpace*1.7) &&
                            (item.getMidpointX() >= (midpoint_X - avgHorizontalSpace*1.7)))
                        output.add(item);
        Collections.sort(output, new DataItemComparator());
        //Arrays.sort(output.toArray(new DataItem[0]), new DataItemComparator());
        dataItems.sort(new DataItemComparator());
        //rrays.sort(dataItems.toArray(new DataItem[0]), new DataItemComparator());
        return output;
    }

    public List<DataItem> getChildrenEnhance(List<DataItem> dataItems) {
        Double midpoint_X = this.getMidpointX() + 0.0;
        List<DataItem> output = new ArrayList<>();
        dataItems.sort(new ClosestToMidpointComparator(midpoint_X));
        //Arrays.sort(dataItems.toArray(new DataItem[0]), new ClosestToMidpointComparator(midpoint_X));
        for (int i=0; i<allRowY.size(); i++) {
            DataItem item = dataItems.get(i);
            if (item != null)
                if (item.getMidpointX() <= (midpoint_X + avgHorizontalSpace * 1.7) &&
                        (item.getMidpointX() >= (midpoint_X - avgHorizontalSpace * 1.7)))
                    output.add(item);
        }
        Collections.sort(output, new DataItemComparator());
        //Arrays.sort(output.toArray(new DataItem[0]), new DataItemComparator());
        dataItems.sort(new DataItemComparator());
        //Arrays.sort(dataItems.toArray(new DataItem[0]), new DataItemComparator());
        return output;
    }

    public List<DataItem> getLine(List<DataItem> dataItems) {
        int midpoint_Y = this.getMidpointY();
        List<Integer> distances = new ArrayList<>();
        List<DataItem> output = new ArrayList<>();
        dataItems.sort(new ClosestToMidpointComparatorY(midpoint_Y));
        //Arrays.sort(dataItems.toArray(new DataItem[0]), new ClosestToMidpointComparatorY(midpoint_Y));
        //printList(dataItems);
        for (DataItem item : dataItems)
            if (item.getMidpointY() <= (this.getMidpointY() + avgVerticalMidpointSpace) &&
                (item.getMidpointY() >= (this.getMidpointY() - avgVerticalMidpointSpace)))
                output.add(item);
        /*for (DataItem item : dataItems) {
            if (this.getLineNumber(allRowY) == item.getLineNumber(allRowY))
                output.add(item);
        }*/
        /*for (DataItem item : dataItems) {
            if (Math.abs(item.getMidpointY() - this.getMidpointY()) <= avgVerticalMidpointSpace &&
                    Math.abs(item.getMidpointY() - this.getMidpointY()) <= avgVerticalSpace)
                output.add(item);
        }*/
        /*for (DataItem item : dataItems) {
            int height = Math.abs(item.getBoxPoints().get(0).getY() - item.getBoxPoints().get(3).getY());
            if (item.getMidpointY() <= (this.getMidpointY() + height/2) &&
                    (item.getMidpointY() >= (this.getMidpointY() - height/2)))
                output.add(item);
        }*/
        output.sort(new DataItemComparator());
        //Arrays.sort(output.toArray(new DataItem[0]), new DataItemComparator());
        dataItems.sort(new DataItemComparator());
        //Arrays.sort(dataItems.toArray(new DataItem[0]), new DataItemComparator());
        return output;
    }



    public DataItem getParent(List<DataItem> dataItems) {
        for (DataItem header : allHeaders)
            if (header.getChildren(dataItems, avgHorizontalSpace).contains(this))
                return header;
        return null;
    }

    public int getLineNumber(List<Integer> allRowY) {
        int min = MAX_VALUE;
        int index = 0;
        for (int i = 0; i<allRowY.size(); i++)
            if (Math.abs(this.getMidpointY() - allRowY.get(i)) < min) {
                min = Math.abs(this.getMidpointY() - allRowY.get(i));
                index = i;
            }
        return index + 1;
    }

    public int getMidpointX() {
        return this.getBoxPoints().get(0).getX() + ((this.getBoxPoints().get(1).getX() - this.getBoxPoints().get(0).getX())/2);
    }

    public int getMidpointY() {
        return this.getBoxPoints().get(0).getY() + ((this.getBoxPoints().get(3).getY() - this.getBoxPoints().get(0).getY())/2);
    }

    public DataItem next(List<DataItem> list) {
        try { return list.get(list.indexOf(this) + 1); }
        catch (Exception exception) { return null; }
    }

    public DataItem prev(List<DataItem> list) {
        try { return list.get(list.indexOf(this) - 1); }
        catch (Exception exception) { return null; }
    }
}
