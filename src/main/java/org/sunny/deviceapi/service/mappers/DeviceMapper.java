package org.sunny.deviceapi.service.mappers;

import org.springframework.stereotype.Component;
import org.sunny.deviceapi.models.db.NetworkDeviceDeployment;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;

@Component
public class DeviceMapper {

    public NetworkDeviceDeployment map(final NetworkDeviceDeploymentDto networkDeviceDeploymentDto) {
        return new NetworkDeviceDeployment(networkDeviceDeploymentDto.getMacAddress(), networkDeviceDeploymentDto.getDeviceType(), networkDeviceDeploymentDto.getUplinkMacAddress());
    }

    public NetworkDeviceDeploymentDto map(final NetworkDeviceDeployment networkDeviceDeployment) {
        return new NetworkDeviceDeploymentDto(networkDeviceDeployment.getMacAddress(), networkDeviceDeployment.getDeviceType(), networkDeviceDeployment.getUplinkMacAddress());
    }

}