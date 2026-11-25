package com.example.baitapsession12.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplyCreateRequest {
    @NotBlank(message = "Tên vật tư không được để trống")
    @Size(max = 255, message = "Tên vật tư tối đa 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Quy cách tối đa 1000 ký tự")
    private String specification;

    @NotBlank(message = "Nhà sản xuất không được để trống")
    @Size(max = 255, message = "Nhà sản xuất tối đa 255 ký tự")
    private String provider;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính tối đa 50 ký tự")
    private String unit;
}
