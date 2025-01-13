package org.sunny.deviceapi.repository;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.exception.DuplicateDeviceException;
import org.sunny.deviceapi.exception.UplinkDeviceNotFoundException;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.db.NetworkDeviceDeployment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private Jdbi jdbi;

    @BeforeEach
    void setUp() {
        jdbi.useHandle(handle -> handle.execute("DELETE FROM NETWORK_DEVICE_DEPLOYMENT"));
    }

    @Test
    void shouldRegisterToNetworkDeployment() throws DeviceNotFoundException {
        var networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        deviceRepository.registerToNetworkDeployment(networkDeviceDeployment);
        var savedDevice = deviceRepository.getNetworkDeviceDeploymentByMacAddress("00:11:22:33:44:01");
        assertNotNull(savedDevice);
        assertEquals("00:11:22:33:44:01", savedDevice.getMacAddress());
        assertEquals(DeviceType.Gateway, savedDevice.getDeviceType());
    }

    @Test
    void shouldThrowDuplicateDeviceExceptionWhenDeviceAlreadyExists() {
        var networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        deviceRepository.registerToNetworkDeployment(networkDeviceDeployment);
        var thrown = assertThrows(DuplicateDeviceException.class, () -> deviceRepository.registerToNetworkDeployment(networkDeviceDeployment));
        assertEquals("Device with the same MAC address; 00:11:22:33:44:01 already exists", thrown.getMessage());
    }

    @Test
    void shouldThrowUplinkDeviceNotFoundExceptionWhenUplinkDoesNotExist() {
        var networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:02", DeviceType.Switch, "00:11:22:33:44:01");
        var thrown = assertThrows(UplinkDeviceNotFoundException.class, () -> deviceRepository.registerToNetworkDeployment(networkDeviceDeployment));
        assertEquals("Device with the uplink MAC address: 00:11:22:33:44:01 does not exist", thrown.getMessage());
    }

    @Test
    void shouldGetAllNetworkDeviceDeployments() {
        var device1 = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        var device2 = new NetworkDeviceDeployment("00:11:22:33:44:02", DeviceType.Switch, "00:11:22:33:44:01");
        deviceRepository.registerToNetworkDeployment(device1);
        deviceRepository.registerToNetworkDeployment(device2);
        var result = deviceRepository.getAllNetworkDeviceDeployments(1, 10);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("00:11:22:33:44:01", result.get(0).getMacAddress());
        assertEquals("00:11:22:33:44:02", result.get(1).getMacAddress());
    }

    @Test
    void shouldGetTotalDeviceCount() {
        var device1 = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        var device2 = new NetworkDeviceDeployment("00:11:22:33:44:02", DeviceType.Switch, "00:11:22:33:44:01");
        deviceRepository.registerToNetworkDeployment(device1);
        deviceRepository.registerToNetworkDeployment(device2);
        var totalDevices = deviceRepository.getTotalDeviceCount();
        assertEquals(2, totalDevices);
    }

    @Test
    void shouldGetNetworkDeviceByMacAddress() throws DeviceNotFoundException {
        var networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        deviceRepository.registerToNetworkDeployment(networkDeviceDeployment);
        var result = deviceRepository.getNetworkDeviceDeploymentByMacAddress("00:11:22:33:44:01");
        assertNotNull(result);
        assertEquals("00:11:22:33:44:01", result.getMacAddress());
    }

    @Test
    void shouldThrowDeviceNotFoundExceptionWhenDeviceNotExist() {
        var thrown = assertThrows(DeviceNotFoundException.class, () -> deviceRepository.getNetworkDeviceDeploymentByMacAddress("00:11:22:33:44:99"));
        assertEquals("Device with mac address: 00:11:22:33:44:99 does not exist", thrown.getMessage());
    }

    @Test
    void shouldGetNetworkDeviceDeploymentTopology() {
        var device1 = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        var device2 = new NetworkDeviceDeployment("00:11:22:33:44:02", DeviceType.Switch, "00:11:22:33:44:01");
        var device3 = new NetworkDeviceDeployment("00:11:22:33:44:03", DeviceType.AccessPoint, "00:11:22:33:44:02");
        deviceRepository.registerToNetworkDeployment(device1);
        deviceRepository.registerToNetworkDeployment(device2);
        deviceRepository.registerToNetworkDeployment(device3);
        var result = deviceRepository.getNetworkDeviceDeploymentTopology();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("00:11:22:33:44:01", result.get(0).getMacAddress());
        assertEquals("00:11:22:33:44:02", result.get(1).getMacAddress());
        assertEquals("00:11:22:33:44:03", result.get(2).getMacAddress());
    }

    @Test
    void shouldGetNetworkDeviceDeploymentTopologyFromMacAddress() {
        var device1 = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        var device2 = new NetworkDeviceDeployment("00:11:22:33:44:02", DeviceType.Switch, "00:11:22:33:44:01");
        var device3 = new NetworkDeviceDeployment("00:11:22:33:44:03", DeviceType.AccessPoint, "00:11:22:33:44:02");
        deviceRepository.registerToNetworkDeployment(device1);
        deviceRepository.registerToNetworkDeployment(device2);
        deviceRepository.registerToNetworkDeployment(device3);
        var result = deviceRepository.getNetworkDeviceDeploymentTopologyByMacAddress("00:11:22:33:44:02");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("00:11:22:33:44:02", result.get(0).getMacAddress());
        assertEquals("00:11:22:33:44:03", result.get(1).getMacAddress());
    }
}
