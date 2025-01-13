package org.sunny.deviceapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.common.PaginatedResponse;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;
import org.sunny.deviceapi.service.DeviceService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    void shouldRegisterToNetworkDeployment() throws Exception {
        NetworkDeviceDeploymentDto device = new NetworkDeviceDeploymentDto("00:11:22:33:44:55", DeviceType.Gateway, null);

        mockMvc.perform(post("/api/v1/network/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deviceType": "Gateway",
                                    "macAddress": "00:11:22:33:44:55",
                                    "uplinkMacAddress": null
                                }
                                """))
                .andExpect(status().isOk());

        verify(deviceService, times(1)).registerToNetworkDeployment(device);
    }

    @Test
    void shouldGetAllDevices() throws Exception {
        when(deviceService.getAllRegisteredDevices(1, 10))
                .thenReturn(new PaginatedResponse<>(List.of(new NetworkDeviceDeploymentDto("00:11:22:33:44:55", DeviceType.Gateway, null)), 1, 1, 1, 1, false));

        mockMvc.perform(get("/api/v1/network/deployments")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].deviceType").value("Gateway"))
                .andExpect(jsonPath("$.data[0].macAddress").value("00:11:22:33:44:55"));
    }

    @Test
    void shouldGetDeviceByMacAddress() throws Exception {
        String macAddress = "00:11:22:33:44:55";
        NetworkDeviceDeploymentDto device = new NetworkDeviceDeploymentDto(macAddress, DeviceType.Gateway, null);

        when(deviceService.getNetworkDeviceDeploymentByMacAddress(macAddress))
                .thenReturn(device);

        mockMvc.perform(get("/api/v1/network/deployments/{macAddress}", macAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.macAddress").value(macAddress))
                .andExpect(jsonPath("$.deviceType").value("Gateway"));
    }

    @Test
    void shouldReturnNotFoundWhenDeviceDoesNotExist() throws Exception {
        String macAddress = "00:11:22:33:44:55";

        when(deviceService.getNetworkDeviceDeploymentByMacAddress(macAddress))
                .thenThrow(new DeviceNotFoundException("Device not found"));

        mockMvc.perform(get("/api/v1/network/deployments/{macAddress}", macAddress))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetNetworkDeviceDeploymentTopology() throws Exception {
        List<DeviceTopologyDto> topology = List.of(
                new DeviceTopologyDto("00:11:22:33:44:55", "Gateway", null, 1, null)
        );

        when(deviceService.getNetworkDeviceDeploymentTopology())
                .thenReturn(topology);

        mockMvc.perform(get("/api/v1/network/deployments/topology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].macAddress").value("00:11:22:33:44:55"))
                .andExpect(jsonPath("$[0].deviceType").value("Gateway"));
    }

    @Test
    void shouldGetNetworkDeviceDeploymentTopologyFromMacAddress() throws Exception {
        String macAddress = "00:11:22:33:44:55";
        List<DeviceTopologyDto> topology = List.of(
                new DeviceTopologyDto(macAddress, "Gateway", null, 1, null)
        );

        when(deviceService.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress))
                .thenReturn(topology);

        mockMvc.perform(get("/api/v1/network/deployments/topology/{macAddress}", macAddress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].macAddress").value(macAddress))
                .andExpect(jsonPath("$[0].deviceType").value("Gateway"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidMacAddress() throws Exception {
        mockMvc.perform(post("/api/v1/network/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deviceType": "Gateway",
                                    "macAddress": "invalid-mac-address",
                                    "uplinkMacAddress": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesRegistered() throws Exception {
        when(deviceService.getAllRegisteredDevices(1, 10))
                .thenReturn(new PaginatedResponse<>(List.of(), 1, 10, 0, 0, false));

        mockMvc.perform(get("/api/v1/network/deployments")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturnPaginatedListOfDevices() throws Exception {
        List<NetworkDeviceDeploymentDto> devices = List.of(
                new NetworkDeviceDeploymentDto("00:11:22:33:44:55", DeviceType.Gateway, null),
                new NetworkDeviceDeploymentDto("00:11:22:33:44:56", DeviceType.Switch, "00:11:22:33:44:55")
        );

        when(deviceService.getAllRegisteredDevices(1, 10))
                .thenReturn(new PaginatedResponse<>(devices, 1, 10, 2, 1, false));

        mockMvc.perform(get("/api/v1/network/deployments")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].macAddress").value("00:11:22:33:44:55"))
                .andExpect(jsonPath("$.data[1].macAddress").value("00:11:22:33:44:56"));
    }

    @Test
    void shouldReturnBadRequestWhenDeviceTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/network/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "macAddress": "00:11:22:33:44:55",
                                    "uplinkMacAddress": null
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
