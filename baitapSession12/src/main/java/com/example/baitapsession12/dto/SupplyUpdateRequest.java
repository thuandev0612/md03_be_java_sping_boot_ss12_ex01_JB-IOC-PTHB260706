package com.example.baitapsession12.dto;

import com.example.baitapsession12.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class SupplyUpdateRequest {
    @NotBlank(message = "Tên vật tư không được để trống")
    @Size(max = 255, message = "Tên vật tư tối đa 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Quy cách tối đa 1000 ký tự")
    private String specification;

    @NotBlank(message = "Nhà sản xuất không được để trống")
    @Size(max = 255, message = "Nhà sản xuất tối đa 255 ký tự")
    private String provider;

    @JsonAnySetter
    public void rejectForbiddenField(String field, Object value) {
        log.warn("Client cố gửi trường không được phép cập nhật vật tư: {}", field);
        throw new ApiException(HttpStatus.BAD_REQUEST,
                "Không được gửi trường " + field + "; chỉ cho phép name, specification, provider");
    }
}
