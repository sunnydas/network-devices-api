package org.sunny.deviceapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.models.common.PaginatedResponse;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;
import org.sunny.deviceapi.service.DeviceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/deployments")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public NetworkDeviceDeploymentDto registerToNetworkDeployment(@Valid @RequestBody NetworkDeviceDeploymentDto networkDeviceDeploymentDto) {
        deviceService.registerToNetworkDeployment(networkDeviceDeploymentDto);
        return networkDeviceDeploymentDto;
    }

    @GetMapping
    public PaginatedResponse<NetworkDeviceDeploymentDto> getAllRegisteredDevices(
            @RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(10000) int size) {
        return deviceService.getAllRegisteredDevices(page, size);
    }

    @GetMapping("/{macAddress}")
    public NetworkDeviceDeploymentDto getNetworkDeviceDeployment(@PathVariable("macAddress") String macAddress) throws DeviceNotFoundException {
        return deviceService.getNetworkDeviceDeploymentByMacAddress(macAddress);
    }

    @GetMapping("/topology")
    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopology() {
        return deviceService.getNetworkDeviceDeploymentTopology();
    }

    @GetMapping("/topology/{macAddress}")
    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopologyFromMacAddress(@PathVariable("macAddress") String macAddress) {
        return deviceService.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress);
    }
}