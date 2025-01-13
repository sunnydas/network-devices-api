package org.sunny.deviceapi.models.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {

    @Min(value = 1, message = "Page must be greater than or equal to 1")
    @Max(value = 10000, message = "Page must be less than or equal to 10000")
    private int page = 1;

    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 500, message = "Size must be less than or equal to 500")
    private int size = 10;
}
