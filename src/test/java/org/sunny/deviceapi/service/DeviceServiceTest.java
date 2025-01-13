package org.sunny.deviceapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.exception.DuplicateDeviceException;
import org.sunny.deviceapi.exception.UplinkDeviceNotFoundException;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.db.NetworkDeviceDeployment;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;
import org.sunny.deviceapi.repository.DeviceRepository;
import org.sunny.deviceapi.service.mappers.DeviceMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    private NetworkDeviceDeploymentDto networkDeviceDeploymentDto;
    private DeviceTopologyDto deviceTopologyDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        networkDeviceDeploymentDto = new NetworkDeviceDeploymentDto("00:11:22:33:44:91", DeviceType.AccessPoint, null);
        deviceTopologyDto = new DeviceTopologyDto("00:11:22:33:44:91", "AccessPoint", null, 1, null);
    }

    @Test
    void shouldThrowDuplicateDeviceExceptionWhenDeviceAlreadyExists() {
        var networkDeviceDeployment = mock(NetworkDeviceDeployment.class);

        doThrow(new DuplicateDeviceException("Device with the same MAC address already exists", null))
                .when(deviceRepository)
                .registerToNetworkDeployment(networkDeviceDeployment);

        var deviceDto = mock(NetworkDeviceDeploymentDto.class);
        when(deviceMapper.map(deviceDto)).thenReturn(networkDeviceDeployment);

        var thrown = assertThrows(DuplicateDeviceException.class, () ->
                deviceService.registerToNetworkDeployment(deviceDto));

        assertEquals("Device with the same MAC address already exists", thrown.getMessage());
    }


    @Test
    void shouldHandleEmptyDeviceListWhenFetchingAllRegisteredDevices() {
        when(deviceRepository.getAllNetworkDeviceDeployments(1, 10)).thenReturn(List.of());
        when(deviceRepository.getTotalDeviceCount()).thenReturn(0L);

        var response = deviceService.getAllRegisteredDevices(1, 10);

        assertNotNull(response);
        assertEquals(0, response.getTotalRecords());
        assertTrue(response.getData().isEmpty());
        assertFalse(response.isHasNextPage());
    }

    @Test
    void shouldThrowUplinkDeviceNotFoundExceptionWhenUplinkDeviceIsMissing() {
        var networkDeviceDeployment = mock(NetworkDeviceDeployment.class);

        var uplinkMacAddress = "00:11:22:33:44:92";
        networkDeviceDeploymentDto.setUplinkMacAddress(uplinkMacAddress);

        doThrow(new UplinkDeviceNotFoundException("Uplink device not found", null))
                .when(deviceRepository)
                .registerToNetworkDeployment(networkDeviceDeployment);

        var deviceDto = mock(NetworkDeviceDeploymentDto.class);
        when(deviceMapper.map(deviceDto)).thenReturn(networkDeviceDeployment);

        var thrown = assertThrows(UplinkDeviceNotFoundException.class, () -> deviceService.registerToNetworkDeployment(deviceDto));

        assertEquals("Uplink device not found", thrown.getMessage());
    }

    @Test
    void shouldReturnEmptyTopologyWhenNoDevicesExist() {
        when(deviceRepository.getNetworkDeviceDeploymentTopology()).thenReturn(List.of());

        var topology = deviceService.getNetworkDeviceDeploymentTopology();

        assertNotNull(topology);
        assertTrue(topology.isEmpty());
    }

    @Test
    void shouldReturnEmptyDeviceTopologyFromMacAddressWhenNoDevicesExistForMac() {
        var macAddress = "00:11:22:33:44:91";
        when(deviceRepository.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress)).thenReturn(List.of());

        var topology = deviceService.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress);

        assertNotNull(topology);
        assertTrue(topology.isEmpty());
    }

    @Test
    void shouldReturnDeviceTopologyForGivenMacAddress() {
        var macAddress = "00:11:22:33:44:91";
        when(deviceRepository.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress)).thenReturn(List.of(deviceTopologyDto));

        var topology = deviceService.getNetworkDeviceDeploymentTopologyByMacAddress(macAddress);

        assertNotNull(topology);
        assertEquals(1, topology.size());
        assertEquals(deviceTopologyDto, topology.getFirst());
    }

    @Test
    void shouldReturnPaginatedDevicesCorrectlyForDifferentPageSizes() {
        var networkDevices = List.of(
                new NetworkDeviceDeployment("00:11:22:33:44:55", DeviceType.AccessPoint, null),
                new NetworkDeviceDeployment("00:11:22:33:44:56", DeviceType.Gateway, null)
        );

        when(deviceRepository.getAllNetworkDeviceDeployments(1, 1)).thenReturn(networkDevices.subList(0, 1));
        when(deviceRepository.getTotalDeviceCount()).thenReturn(2L);
        when(deviceMapper.map(any(NetworkDeviceDeployment.class))).thenCallRealMethod();

        var response = deviceService.getAllRegisteredDevices(1, 1);
        assertEquals(1, response.getData().size());
        assertEquals("00:11:22:33:44:55", response.getData().getFirst().getMacAddress());
        assertTrue(response.isHasNextPage());

        when(deviceRepository.getAllNetworkDeviceDeployments(2, 1)).thenReturn(networkDevices.subList(1, networkDevices.size()));
        response = deviceService.getAllRegisteredDevices(2, 1);
        assertEquals(1, response.getData().size());
        assertEquals("00:11:22:33:44:56", response.getData().getFirst().getMacAddress());
        assertFalse(response.isHasNextPage());
    }

    @Test
    void shouldThrowExceptionWhenFetchingNonExistingDeviceByMacAddress() throws DeviceNotFoundException {
        var macAddress = "00:11:22:33:44:91";
        when(deviceRepository.getNetworkDeviceDeploymentByMacAddress(macAddress))
                .thenThrow(new DeviceNotFoundException("Device not found"));

        var thrown = assertThrows(DeviceNotFoundException.class, () -> deviceService.getNetworkDeviceDeploymentByMacAddress(macAddress));

        assertEquals("Device not found", thrown.getMessage());
    }

    @Test
    void shouldHandleValidAndInvalidMacAddressesCorrectly() {
        var validDevice = new NetworkDeviceDeploymentDto("00:11:22:33:44:91", DeviceType.AccessPoint, null);
        var invalidDevice = new NetworkDeviceDeploymentDto("invalid-mac-address", DeviceType.Gateway, null);

        when(deviceMapper.map(validDevice)).thenReturn(new NetworkDeviceDeployment(validDevice.getMacAddress(), validDevice.getDeviceType(), validDevice.getUplinkMacAddress()));
        when(deviceMapper.map(invalidDevice)).thenThrow(new IllegalArgumentException("Invalid mac address format"));

        assertDoesNotThrow(() -> deviceService.registerToNetworkDeployment(validDevice));
        assertThrows(IllegalArgumentException.class, () -> deviceService.registerToNetworkDeployment(invalidDevice));
    }

    @Test
    void shouldReturnDevicesSortedByDeviceType() {
        var networkDevices = List.of(
                new NetworkDeviceDeployment("00:11:22:33:44:55", DeviceType.Gateway, null),
                new NetworkDeviceDeployment("00:11:22:33:44:56", DeviceType.Switch, null),
                new NetworkDeviceDeployment("00:11:22:33:44:57", DeviceType.AccessPoint, null)
        );

        when(deviceRepository.getAllNetworkDeviceDeployments(1, 3)).thenReturn(networkDevices);
        when(deviceRepository.getTotalDeviceCount()).thenReturn(3L);
        when(deviceMapper.map(any(NetworkDeviceDeployment.class))).thenCallRealMethod();

        var response = deviceService.getAllRegisteredDevices(1, 3);

        assertNotNull(response);
        assertEquals(3, response.getData().size());
        assertEquals("00:11:22:33:44:55", response.getData().get(0).getMacAddress());
        assertEquals("00:11:22:33:44:56", response.getData().get(1).getMacAddress());
        assertEquals("00:11:22:33:44:57", response.getData().get(2).getMacAddress());
    }
}
