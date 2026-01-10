package com.example.backendplantshop.dto.response;

import com.example.backendplantshop.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageDtoResponse {
    private List<Products> products;
    private int page;
    private int limit;
    private int total;
}
