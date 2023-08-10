package com.marcus.getFormData.service.impl;

import com.marcus.getFormData.model2.BoxPoint;
import com.marcus.getFormData.model2.DataItem;
import com.marcus.getFormData.model2.RowData;
import com.marcus.getFormData.service.DataService;
import com.marcus.getFormData.util.ClosestToMidpointComparator;
import com.marcus.getFormData.util.ClosestToMidpointComparatorY;
import com.marcus.getFormData.util.DataItemComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {
    public static List<DataItem> neededHeaders = new ArrayList<>();
    public static List<DataItem> allHeaders = new ArrayList<>();
    public static List<Integer> allRowY = new ArrayList<>();
    public List<DataItem> dataItems = new ArrayList<>();
    public static DataItem keyHeader = new DataItem();
    static int totalVerticalSpace = 0;
    static int totalHorizontalSpace = 0;
    static int numVerticalSpaces = 0;
    static int numHorizontalSpaces = 0;
    static int minY = MAX_VALUE;
    public static Double avgVerticalSpace = 0.0;
    public static Double avgHorizontalSpace = 0.0;
    public static int avgVerticalMidpointSpace = 0;
    public static int avgHorizontalMidpointSpace = 0;
    @Override
    public void initData(List<DataItem> allDataItems, List<String> headerNames, String key) {
        dataItems = allDataItems;
        dataItems.sort(new DataItemComparator());
        calculateRawSpaces(dataItems);
        trimList2(dataItems);
        findHeaders(dataItems, headerNames, key);
        log.info("avgVerticalMidpointSpace: {}, avgHorizontalMidpointSpace: {}",avgVerticalMidpointSpace ,avgHorizontalMidpointSpace);
        dataItems = getFormData(dataItems);
        processData(dataItems);
        outResult(dataItems);
        /*//Debug
        printList(dataItems.get(52).getLine(dataItems, avgVerticalMidpointSpace));
        printList(dataItems.get(10).getLine(dataItems, avgVerticalMidpointSpace));*/
    }

    private void calculateRawSpaces(List<DataItem> dataItems) {
        for (DataItem item : dataItems) {
            calculateSpaces(item);
        }
        //Calculate Avg
        avgVerticalSpace = totalVerticalSpace / numVerticalSpaces * 0.5;
        avgHorizontalSpace = totalHorizontalSpace / numHorizontalSpaces * 1.8;
        log.info("avgVerticalSpace: {}, avgHorizontalSpace: {}",avgVerticalSpace ,avgHorizontalSpace);
    }

    @Override
    public void outResult(List<DataItem> dataItems) {
        printList(dataItems);
        printResult(neededHeaders, dataItems);
    }

    @Override
    public void processData(List<DataItem> dataItems) {
        removeHeader(dataItems);
        trimList(dataItems);
        setAllRowY(dataItems);
        log.info("total row number: {}", getRowNumber(dataItems));
        log.info("Y value for each line: {}", allRowY);

        // Sorting based on horizontal and vertical positions
        dataItems.sort(new DataItemComparator());
    }

    @Override
    public void removeCache() {
        neededHeaders = new ArrayList<>();
        allHeaders = new ArrayList<>();
        allRowY = new ArrayList<>();
        keyHeader = new DataItem();
        totalVerticalSpace = 0;
        totalHorizontalSpace = 0;
        numVerticalSpaces = 0;
        numHorizontalSpaces = 0;
        minY = MAX_VALUE;
        avgVerticalSpace = 0.0;
        avgHorizontalSpace = 0.0;
        avgVerticalMidpointSpace = 0;
        avgHorizontalMidpointSpace = 0;
    }

    public String returnData() {
        String out = "";
        List<RowData> rowDataList = new ArrayList<>();
        List<List<String>> form = new ArrayList<>();
        for (DataItem header : neededHeaders) {
            form.add(returnDataBuilder(dataItems, header));
        }
        for (int i=0; i<form.get(0).size(); i++) {
            for (int j = 0; j < neededHeaders.size(); j++) {
                RowData rowData = RowData.builder()
                        .header(neededHeaders.get(j).getText())
                        .data(form.get(j).get(i))
                        .build();
                rowDataList.add(rowData);
            }
        }

        out += "[\n";
        for (int i=0; i<(rowDataList.size()/neededHeaders.size()); i++) {
            out += "\t{\n";
            for (int j=0; j<neededHeaders.size(); j++) {
                if (j < neededHeaders.size()-1)
                    out += "\t\t" + rowDataList.get((i*neededHeaders.size())+j).toString() + ",\n";
                else
                    out += "\t\t" + rowDataList.get((i*neededHeaders.size())+j).toString() + "\n";
            }
            if (i < rowDataList.size()-1)
                out += "\t},\n";
            else
                out += "\t}\n";
        }

        rowDataList.clear();
        return out += "]";
    }

    private List<String> returnDataBuilder(List<DataItem> dataItems, DataItem header) {
        List<String> output = new ArrayList<>();
        for (DataItem item : fillGap2(dataItems, header))
            output.add(item.getText());
        return output;
    }

    private void printResult(List<DataItem> neededHeaders, List<DataItem> dataItems) {
        for (DataItem header : neededHeaders)
            printList(fillGap2(dataItems, header));
    }

    public static void printList(List<DataItem> dataItems) {
        System.out.println("All Items:");
        for (DataItem item : dataItems) {
            System.out.println("Text: " + item.getText() + ", Position: " + item.getBoxPoints().toString());
        }
    }

    private static void removeHeader(List<DataItem> dataItems) {
        for (DataItem item : allHeaders)
            dataItems.remove(item);
    }

    private void trimList(List<DataItem> dataItems) {
        List<DataItem> removeList = new ArrayList<>();
        for (DataItem head : allHeaders)
            for (DataItem element1 : head.getChildren(dataItems, avgHorizontalSpace))
                for (DataItem element2 : head.getChildren(dataItems, avgHorizontalSpace)) {
                    if (!removeList.contains(element1) && !removeList.contains(element2) && !element1.equals(element2))
                        if (onTop(element1, element2) &&
                                inSameLine(element1, element2, dataItems)) {
                            /*log.info("START");
                            printList(element1.getLine(dataItems, avgVerticalMidpointSpace));
                            log.info("END");*/
                            element1.combine(element2);
                            removeList.add(element2);
                        }
                }
        for (DataItem toDelete : removeList)
            dataItems.remove(toDelete);
        dataItems.sort(new DataItemComparator());
    }

    private void trimList2(List<DataItem> dataItems) {
        List<DataItem> removeList = new ArrayList<>();
        for (DataItem element1 : dataItems) {
            for (DataItem element2 : dataItems) {
                if (!removeList.contains(element1) && !removeList.contains(element2) && !element1.equals(element2)) {
                    //DEBUG
                    if (element1.getText().equals("商品货号") && element2.getText().equals("（货号+颜色代码）"))
                        System.out.println("1");
                    if (onTop(element1, element2) && inSameRow(element1, element2, dataItems)) {
                        element1.combine(element2);
                        removeList.add(element2);
                    }
                }
            }
        }
        // Marked items for removal, now remove them
        dataItems.removeAll(removeList);
        dataItems.sort(new DataItemComparator());
    }


    private boolean inSameRow(DataItem element1, DataItem element2, List<DataItem> dataItems) { //临近分类算法
        List<DataItem> dummyList = new ArrayList<>(dataItems);
        dummyList.sort(new ClosestToMidpointComparator(element1.getMidpointX()));

        // 计算初始阈值
        List<Integer> distances = initDistances(3, dummyList); //变量size为取多少个数据为初始值 推荐为3
        double threshold = mean(distances) + 3;

        //动态学习阈值
        for (DataItem item : dummyList) {
            threshold = mean(distances) + 3;
            int distance = Math.abs(item.getMidpointX() - element1.getMidpointX());
            if (distance <= threshold)
                distances.add(distance);
        }
        return Math.abs(element1.getMidpointX() - element2.getMidpointX()) <= threshold;
    }

    private List<Integer> initDistances(int size, List<DataItem> inList) { //初始化 变量size为取多少个数据为初始值 推荐为3
        List<Integer> outList = new ArrayList<>();
        for (int i=1; i<=size; i++) {
            int distance = Math.abs(inList.get(i).getMidpointX() - inList.get(i-1).getMidpointX());
            outList.add(distance);
        }
        return outList;
    }

    private double mean(List<Integer> numbers) { //求平均
        return numbers.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private boolean inSameLine(DataItem element1, DataItem element2, List<DataItem> dataItems) {
        return element1.getLine(dataItems).contains(element2);
    }

    public static Boolean onTop(DataItem element1, DataItem element2) { //因为OCR模型识别出来的坐标点是不规则四边形，所以每个点都需要比较
        return Math.abs(element1.getBoxPoints().get(3).getY() - element2.getBoxPoints().get(0).getY()) < avgVerticalSpace || //左下比较左上
                Math.abs(element1.getBoxPoints().get(2).getY() - element2.getBoxPoints().get(1).getY()) < avgVerticalSpace || //右下比较右上
                Math.abs(element1.getBoxPoints().get(3).getY() - element2.getBoxPoints().get(1).getY()) < avgVerticalSpace || //左下比较右上
                Math.abs(element1.getBoxPoints().get(2).getY() - element2.getBoxPoints().get(0).getY()) < avgVerticalSpace; //右下比较左上
    }

    public static Boolean onBottom(DataItem element1, DataItem element2) {return onTop(element2, element1);}

    private void findHeaders(List<DataItem> dataItems, List<String> headerNames, String key) {
        Double yTotal = 0.0;
        Double yAvg = 0.0;
        //Find NeededHeaders
        for (String headerName : headerNames)
            for (DataItem item : dataItems) {
                calculateHorizontalMidpointSpaces(item, dataItems);
                if (item.getText().equals(headerName) && !neededHeaders.contains(item)) {
                    neededHeaders.add(item);
                    yTotal += item.getMidpointY();
                }
                if (item.getText().equals(key))
                    keyHeader = item;
            }
        neededHeaders.sort(new DataItemComparator());
        yAvg = yTotal / neededHeaders.size();
        log.info("Average Y value for headers: "+yAvg);

        //Find AllHeaders
        for (DataItem item : dataItems)
            if (Math.abs(item.getMidpointY() - yAvg) <= avgVerticalSpace*2)
                allHeaders.add(item);
        allHeaders.sort(new DataItemComparator());

        //Calculate avg vertical midpoint spaces
        calculateVerticalMidpointSpaces(dataItems);
        System.out.println("All Headers:");
        printList(allHeaders);
    }

    private void calculateVerticalMidpointSpaces(List<DataItem> dataItems) {
        List<DataItem> children = new ArrayList<>();
        int avgSpaceEachLine = 0;
        int total = 0;
        for (DataItem header : allHeaders) {
            children = header.getChildren(dataItems, avgHorizontalSpace);
            for (DataItem child : children) {
                if (child.next(children) != null) {
                    int space = Math.abs(child.next(children).getMidpointY() - child.getMidpointY());
                    if (avgSpaceEachLine == 0)
                        avgSpaceEachLine = space;
                    avgSpaceEachLine = (avgSpaceEachLine + space) / 2;
                }
            }
            total += avgSpaceEachLine;
            avgSpaceEachLine = 0;
        }
        if (allHeaders.size() == 0)
            avgVerticalMidpointSpace=0;
        else
            avgVerticalMidpointSpace = total/allHeaders.size();
    }

    private void calculateHorizontalMidpointSpaces(DataItem item, List<DataItem> dataItems) {
        DataItem prevItem = item.prev(dataItems);
        if (prevItem == null)
            return;
        int horizontalSpace = Math.abs(item.getMidpointX() - prevItem.getMidpointX());
        //int verticalSpace = Math.abs(item.getMidpointY() - prevItem.getMidpointY());
        avgHorizontalMidpointSpace = (avgHorizontalMidpointSpace + horizontalSpace) / 2;
        //avgVerticalMidpointSpace = (avgVerticalMidpointSpace + verticalSpace) / 2;
    }

    private void calculateSpaces(DataItem element) {
        int minY = MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minX = MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        for (BoxPoint point : element.getBoxPoints()) {
            int x = point.getX();
            int y = point.getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        totalVerticalSpace += maxY - minY;
        totalHorizontalSpace += maxX - minX;
        numVerticalSpaces += 3; // Since we have 4 points, but the vertical space is shared between adjacent points.
        numHorizontalSpaces += 3; // Since we have 4 points, but the horizontal space is shared between adjacent points.
    }

    // BEGIN - Get Form
    public List<DataItem> getFormData(List<DataItem> dataItems) {
        dataItems = removeTop(dataItems);
        dataItems = removeBottom(dataItems);
        dataItems = removeSides(dataItems);
        return dataItems;
    }



    private List<DataItem> removeTop(List<DataItem> dataItems) {
        List<DataItem> output = new ArrayList<>();
        int verticalIndex = findTopData(allHeaders).getBoxPoints().get(0).getY();
        for (DataItem data : dataItems)
            if (data.getMidpointY() >= verticalIndex)
                output.add(data);
        //printList(output);
        return output;
    }

    private List<DataItem> removeBottom(List<DataItem> dataItems) {
        List<DataItem> output = new ArrayList<>();
        List<DataItem> lastLine = keyHeader.getChildren(dataItems, avgHorizontalSpace).get(getRowNumber(dataItems)-1).getLine(dataItems);
        int verticalIndex = findBottomData(lastLine).getBoxPoints().get(3).getY();
        for (DataItem data : dataItems)
            if (data.getMidpointY() <= verticalIndex)
                output.add(data);
        return output;
    }

    private List<DataItem> removeSides(List<DataItem> dataItems) {
        List<DataItem> output = new ArrayList<>();
        int leftIndex = allHeaders.get(0).getMidpointX() - avgHorizontalMidpointSpace;
        int rightIndex = allHeaders.get(allHeaders.size()-1).getMidpointX() + avgHorizontalMidpointSpace;
        for (DataItem data : dataItems)
            if (data.getMidpointX() > leftIndex && data.getMidpointX() < rightIndex)
                output.add(data);
        output.sort(new DataItemComparator());
        return output;
    }

    private DataItem findTopData(List<DataItem> itemList) { //找到列表中最上方的元素
        itemList.sort(new ClosestToMidpointComparatorY(0.0));
        DataItem out = itemList.get(0);
        itemList.sort(new DataItemComparator());
        return out;
    }

    private DataItem findBottomData(List<DataItem> itemList) { //找到列表中最下方的元素
        itemList.sort(new ClosestToMidpointComparatorY(MAX_VALUE));
        DataItem out = itemList.get(0);
        itemList.sort(new DataItemComparator());
        return out;
    }
    // END - Get Form

    /*//旧的 已废弃
    public int getRowNumber(List<DataItem> dataItems) {
        dataItems.sort(new DataItemComparator());
        int rowNumber = 1;
        int thisY = 0;
        int rowElementCount = 0;
        int totalY = 0;
        for (DataItem data : dataItems) {

            if (data.prev(dataItems) == null)
                thisY = data.getMidpointY();
            if (!(data.getMidpointY() <= thisY + avgVerticalSpace*5 && data.getMidpointY() >= thisY - avgVerticalSpace*5)) {
                rowNumber += 1;
                if (!allRowY.contains(totalY / rowElementCount))
                    allRowY.add(totalY / rowElementCount);
                totalY = 0;
                rowElementCount = 0;
                thisY = data.getMidpointY();
            } else {
                totalY += data.getMidpointY();
                rowElementCount += 1;
            }

            if (data.next(dataItems) == null) //End
                if (!allRowY.contains(totalY / rowElementCount))
                    allRowY.add(totalY / rowElementCount);
        }
        return rowNumber;
    }*/

    public int getRowNumber(List<DataItem> dataItems) {
        return keyHeader.getChildren(dataItems, avgHorizontalSpace).size();
    }

    public void setAllRowY(List<DataItem> dataItems) {
        for (DataItem keyItem : keyHeader.getChildren(dataItems, avgHorizontalSpace)) { //由每个（品名/条码）来检索每一行
            /*//DEBUG
            if (keyItem.next(keyHeader.getChildren(dataItems, avgHorizontalSpace)) == null)
                System.out.println("1");*/
            int thisY = 0;
            int lineDataCount = 0;
            int total = 0;

            dataItems.sort(new ClosestToMidpointComparatorY(keyItem.getMidpointY())); //按照Y值距离当前（品名/条码）最近来排序
            for (DataItem item : dataItems) {
                if (item.prev(dataItems) == null) //如果是第一个
                    thisY = item.getMidpointY();
                if (Math.abs(item.getMidpointY() - thisY) <= Math.abs(item.getBoxPoints().get(0).getY() - item.getBoxPoints().get(3).getY())) { //如果当前Y值距离很近 那么就是同一行的
                    lineDataCount += 1;
                    total += item.getMidpointY();
                }
            }
            if (!allRowY.contains(total/lineDataCount))
                allRowY.add(total/lineDataCount);
        }
        dataItems.sort(new DataItemComparator());
    }

    public List<DataItem> fillGap2(List<DataItem> dataItems, DataItem header) {
        List<DataItem> children = header.getChildrenEnhance(dataItems);
        List<DataItem> output = new ArrayList<>();
        int rowNumber = getRowNumber(dataItems);
        for (int y : allRowY) {
            int count = 0;
            for (DataItem child : children) {
                if (Math.abs(child.getMidpointY() - y) <= Math.abs(child.getBoxPoints().get(0).getY() - child.getBoxPoints().get(3).getY())) {
                    count ++;
                    if (!output.contains(child))
                        output.add(child);
                }
            }
            if (count == 0)
                output.add(createEmptyItem(children.get(0), y));
        }
        return output;
    }

    private DataItem createEmptyItem(DataItem item, int newY) { //生成空值
        DataItem newItem = new DataItem();
        List<BoxPoint> boxPoints = new ArrayList<>();
        boxPoints.add(new BoxPoint(item.getBoxPoints().get(0).getX(), newY-5));
        boxPoints.add(new BoxPoint(item.getBoxPoints().get(1).getX(), newY-5));
        boxPoints.add(new BoxPoint(item.getBoxPoints().get(1).getX(), newY+5));
        boxPoints.add(new BoxPoint(item.getBoxPoints().get(0).getX(), newY+5));
        newItem.setText("空");
        newItem.setBoxPoints(boxPoints);
        return newItem;
    }
}
