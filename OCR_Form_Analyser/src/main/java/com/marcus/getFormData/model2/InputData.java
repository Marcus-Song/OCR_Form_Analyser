package com.marcus.getFormData.model2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputData {
    private boolean b_result;
    private String err_mag;
    private String err_code;
    private List<DataItem> data;
}
