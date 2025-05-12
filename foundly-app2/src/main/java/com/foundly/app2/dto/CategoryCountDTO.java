package com.foundly.app2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryCountDTO {
    private String category;
    private Long count;
}
