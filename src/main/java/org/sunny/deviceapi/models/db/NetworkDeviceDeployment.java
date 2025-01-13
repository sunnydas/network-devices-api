package org.sunny.deviceapi.models.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sunny.deviceapi.models.common.DeviceType;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkDeviceDeployment {
    private String macAddress;

    private DeviceType deviceType;

    private String uplinkMacAddress;
}
