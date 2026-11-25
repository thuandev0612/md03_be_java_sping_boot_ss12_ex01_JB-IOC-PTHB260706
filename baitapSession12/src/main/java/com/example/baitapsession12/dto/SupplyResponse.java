package com.example.baitapsession12.dto;

import com.example.baitapsession12.model.Supply;

public record SupplyResponse(Long id, String name, String specification, String provider,
                             String unit, long quantity, boolean isDeleted) {
    public static SupplyResponse from(Supply supply) {
        return new SupplyResponse(supply.getId(), supply.getName(), supply.getSpecification(),
                supply.getProvider(), supply.getUnit(), supply.getQuantity(), supply.isDeleted());
    }
}
