package org.sunny.deviceapi.service.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.db.NetworkDeviceDeployment;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceMapperTest {

    private DeviceMapper deviceMapper;

    @BeforeEach
    public void setUp() {
        deviceMapper = new DeviceMapper();
    }

    @Test
    public void testMapFromDeviceDtoToNetworkDeviceDeployment() {
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = new NetworkDeviceDeploymentDto("00:11:22:33:44:01", DeviceType.Gateway, "00:11:22:33:44:02");
        NetworkDeviceDeployment networkDeviceDeployment = deviceMapper.map(networkDeviceDeploymentDto);
        assertNotNull(networkDeviceDeployment);
        assertEquals(networkDeviceDeploymentDto.getMacAddress(), networkDeviceDeployment.getMacAddress());
        assertEquals(networkDeviceDeploymentDto.getDeviceType(), networkDeviceDeployment.getDeviceType());
        assertEquals(networkDeviceDeploymentDto.getUplinkMacAddress(), networkDeviceDeployment.getUplinkMacAddress());
    }

    @Test
    public void testMapFromDeviceDtoToNetworkDeviceDeployment_WithNullUplinkMacAddress() {
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = new NetworkDeviceDeploymentDto("00:11:22:33:44:01", DeviceType.Switch, null);
        NetworkDeviceDeployment networkDeviceDeployment = deviceMapper.map(networkDeviceDeploymentDto);
        assertNotNull(networkDeviceDeployment);
        assertEquals(networkDeviceDeploymentDto.getMacAddress(), networkDeviceDeployment.getMacAddress());
        assertEquals(networkDeviceDeploymentDto.getDeviceType(), networkDeviceDeployment.getDeviceType());
        assertNull(networkDeviceDeployment.getUplinkMacAddress());
    }

    @Test
    public void testMapFromNetworkDeviceDeploymentToDeviceDto() {
        NetworkDeviceDeployment networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.AccessPoint, "00:11:22:33:44:02");
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = deviceMapper.map(networkDeviceDeployment);
        assertNotNull(networkDeviceDeploymentDto);
        assertEquals(networkDeviceDeployment.getMacAddress(), networkDeviceDeploymentDto.getMacAddress());
        assertEquals(networkDeviceDeployment.getDeviceType(), networkDeviceDeploymentDto.getDeviceType());
        assertEquals(networkDeviceDeployment.getUplinkMacAddress(), networkDeviceDeploymentDto.getUplinkMacAddress());
    }

    @Test
    public void testMapFromNetworkDeviceDeploymentToDeviceDto_WithNullUplinkMacAddress() {
        NetworkDeviceDeployment networkDeviceDeployment = new NetworkDeviceDeployment("00:11:22:33:44:01", DeviceType.Gateway, null);
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = deviceMapper.map(networkDeviceDeployment);
        assertNotNull(networkDeviceDeploymentDto);
        assertEquals(networkDeviceDeployment.getMacAddress(), networkDeviceDeploymentDto.getMacAddress());
        assertEquals(networkDeviceDeployment.getDeviceType(), networkDeviceDeploymentDto.getDeviceType());
        assertNull(networkDeviceDeploymentDto.getUplinkMacAddress());
    }

    @Test
    public void testMapFromDeviceDtoToNetworkDeviceDeployment_WithInvalidMacAddress() {
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = new NetworkDeviceDeploymentDto("INVALID_MAC", DeviceType.Switch, null);
        NetworkDeviceDeployment networkDeviceDeployment = deviceMapper.map(networkDeviceDeploymentDto);
        assertNotNull(networkDeviceDeployment);
        assertEquals(networkDeviceDeploymentDto.getMacAddress(), networkDeviceDeployment.getMacAddress());
        assertEquals(networkDeviceDeploymentDto.getDeviceType(), networkDeviceDeployment.getDeviceType());
        assertNull(networkDeviceDeployment.getUplinkMacAddress());
    }
}
