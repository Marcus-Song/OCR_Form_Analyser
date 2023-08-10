package com.marcus.getFormData.model2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RowData {
    private String header;
    private String data;

    @Override
    public String toString() {
        return "\"" + header + "\": \"" + data + "\"";
    }
}
