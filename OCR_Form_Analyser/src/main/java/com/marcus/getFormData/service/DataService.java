package com.marcus.getFormData.service;

import com.marcus.getFormData.model2.DataItem;

import java.util.List;

public interface DataService {

    void initData(List<DataItem> dataItems, List<String> headerNames, String key);
    void outResult(List<DataItem> dataItems);
    void processData(List<DataItem> dataItems);
}
