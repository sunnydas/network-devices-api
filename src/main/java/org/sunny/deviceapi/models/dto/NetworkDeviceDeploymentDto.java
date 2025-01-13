package org.sunny.deviceapi.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.dto.validator.ValidMacAddress;

@Data
@AllArgsConstructor
public class NetworkDeviceDeploymentDto {

    @NotBlank(message = "MAC Address cannot be null or empty")
    @ValidMacAddress(message = "Invalid mac address format")
    private String macAddress;

    @NotNull(message = "Device type cannot be null")
    private DeviceType deviceType;

    @ValidMacAddress(message = "Invalid mac address format")
    private String uplinkMacAddress;
}
