package org.sunny.deviceapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;

import java.util.*;

@RestController
@RequestMapping("/api/v1/visualization/network")
public class NetworkTopologyVisualizer {

    @PostMapping("/tree")
    public String getNetworkTopologyTree(@RequestBody List<DeviceTopologyDto> deviceTopologyDtoList) {
        var tree = buildDeviceTree(deviceTopologyDtoList);
        return tree.toString();
    }

    public StringBuilder buildDeviceTree(List<DeviceTopologyDto> deviceTopologyDtoList) {
        var childMap = new HashMap<String, List<DeviceTopologyDto>>();
        for (var device : deviceTopologyDtoList) {
            if (device.getUplinkMacAddress() != null) {
                childMap.computeIfAbsent(device.getUplinkMacAddress(), k -> new ArrayList<>()).add(device);
            }
        }

        var tree = new StringBuilder();
        for (var device : deviceTopologyDtoList) {
            if (device.getUplinkMacAddress() == null) {
                buildTreeRecursive(device, childMap, tree, "");
            }
        }

        return tree;
    }

    private void buildTreeRecursive(DeviceTopologyDto device, Map<String, List<DeviceTopologyDto>> childMap, StringBuilder tree, String indent) {
        tree.append(indent)
                .append(device.getDeviceType())
                .append(" (")
                .append(device.getMacAddress())
                .append(")\n");

        var children = childMap.getOrDefault(device.getMacAddress(), new ArrayList<>());
        children.sort(Comparator.comparing(DeviceTopologyDto::getDeviceType));

        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            buildTreeRecursive(child, childMap, tree, indent + (i == children.size() - 1 ? "└── " : "├── "));
        }
    }
}
