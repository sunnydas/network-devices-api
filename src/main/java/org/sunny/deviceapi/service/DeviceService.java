package org.sunny.deviceapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.models.common.PaginatedResponse;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;
import org.sunny.deviceapi.repository.DeviceRepository;
import org.sunny.deviceapi.service.mappers.DeviceMapper;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    public void registerToNetworkDeployment(final NetworkDeviceDeploymentDto networkDeviceDeploymentDto) {
        deviceRepository.registerToNetworkDeployment(deviceMapper.map(networkDeviceDeploymentDto));
    }

    public PaginatedResponse<NetworkDeviceDeploymentDto> getAllRegisteredDevices(int page, int size) {
        var networkDevices = deviceRepository.getAllNetworkDeviceDeployments(page, size);
        var deviceDtos = networkDevices.stream()
                .map(deviceMapper::map)
                .toList();

        var totalRecords = deviceRepository.getTotalDeviceCount();
        var totalPages = (totalRecords + size - 1) / size;
        var hasNextPage = page < totalPages;

        return new PaginatedResponse<>(deviceDtos, page, size, totalRecords, totalPages, hasNextPage);
    }

    public NetworkDeviceDeploymentDto getNetworkDeviceDeploymentByMacAddress(String macAddress) throws DeviceNotFoundException {
        return deviceMapper.map(deviceRepository.getNetworkDeviceDeploymentByMacAddress(macAddress));
    }

    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopology() {
        return deviceRepository.getNetworkDeviceDeploymentTopology();
    }

    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopologyByMacAddress(String macAddress) {
        return deviceRepository.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress);
    }
}
