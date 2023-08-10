package com.marcus.getFormData.controller;


import com.marcus.getFormData.model2.BoxPoint;
import com.marcus.getFormData.model2.InputData;
import com.marcus.getFormData.model2.DataItem;
import com.marcus.getFormData.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    @PostMapping("/processData/{strStream}")
    public String processInput(@RequestBody InputData inputData, @PathVariable String strStream) {
        boolean bResult = inputData.isB_result();
        String errMag = inputData.getErr_mag();
        String errCode = inputData.getErr_code();

        List<DataItem> dataItems = inputData.getData();
        for (DataItem dataItem : dataItems) {
            // INDEX: 0:leftUp, 1:rightUp, 2:rightDown, 3:leftDown
            List<BoxPoint> boxPointsItems = dataItem.getBoxPoints();
            String text = dataItem.getText();
            double score = dataItem.getScore();
        }

        List<String> headerNames = stream(strStream.split(",")).toList();
        //List<String> headerNames = List.of("位置", "品牌名称", "数量", "ERP生成总编号", "玻璃花纹", "产品型号");
        String key = headerNames.get(0);
        dataService.initData(dataItems, headerNames, key);
        String out = dataService.returnData();
        dataService.removeCache();
        return out;
    }
}
