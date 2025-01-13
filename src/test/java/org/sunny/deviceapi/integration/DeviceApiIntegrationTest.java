package org.sunny.deviceapi.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.sunny.deviceapi.models.common.DeviceType;
import org.sunny.deviceapi.models.common.PaginatedResponse;
import org.sunny.deviceapi.models.dto.NetworkDeviceDeploymentDto;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeviceApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Jdbi jdbi;

    @BeforeEach
    void cleanDatabase() {
        jdbi.useHandle(handle -> handle.execute("DELETE FROM NETWORK_DEVICE_DEPLOYMENT"));
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void registerDevice(String macAddress, DeviceType deviceType, String uplinkMacAddress) {
        NetworkDeviceDeploymentDto networkDeviceDeploymentDto = new NetworkDeviceDeploymentDto(macAddress, deviceType, uplinkMacAddress);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(networkDeviceDeploymentDto, createJsonHeaders());
        ResponseEntity<NetworkDeviceDeploymentDto> response = restTemplate.postForEntity("/api/v1/network/deployments", request, NetworkDeviceDeploymentDto.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to register device");
    }

    @Test
    void shouldRegisterDevice() {
        registerDevice("00:11:22:33:44:55", DeviceType.Gateway, null);
    }

    @Test
    void shouldRegisterDeviceWithUplink() {
        registerDevice("00:11:22:33:44:55", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:56", DeviceType.Switch, "00:11:22:33:44:55");
    }

    @Test
    void shouldRejectDuplicateMacAddress() {
        registerDevice("00:11:22:33:44:57", DeviceType.AccessPoint, null);
        NetworkDeviceDeploymentDto duplicateDevice = new NetworkDeviceDeploymentDto("00:11:22:33:44:57", DeviceType.AccessPoint, null);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(duplicateDevice, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value(), "Duplicate MAC address should not be allowed");
    }

    @Test
    void shouldRejectInvalidMacAddressFormat() {
        NetworkDeviceDeploymentDto invalidDevice = new NetworkDeviceDeploymentDto("INVALID-MAC", DeviceType.Switch, null);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(invalidDevice, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value(), "Invalid MAC address format should not be accepted");
    }

    @Test
    void shouldGetAllRegisteredDevices() throws Exception {
        registerDevice("00:11:22:33:44:58", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:59", DeviceType.Switch, null);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments?page=1&size=10", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch devices");

        PaginatedResponse<NetworkDeviceDeploymentDto> paginatedResponse = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
                });

        assertNotNull(paginatedResponse, "Paginated response is null");
        assertNotNull(paginatedResponse.getData(), "Device list is null");
        assertFalse(paginatedResponse.getData().isEmpty(), "Device list is empty");

        NetworkDeviceDeploymentDto firstDevice = paginatedResponse.getData().getFirst();
        assertEquals("00:11:22:33:44:58", firstDevice.getMacAddress(), "First device MAC address mismatch");
        assertEquals(DeviceType.Gateway, firstDevice.getDeviceType(), "First device type mismatch");
    }


    @Test
    void shouldPaginateRegisteredDevices() throws Exception {
        for (int i = 0; i < 15; i++) {
            registerDevice(String.format("00:11:22:33:44:%02d", i), DeviceType.Switch, null);
        }

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments?page=1&size=10", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch devices");

        PaginatedResponse<NetworkDeviceDeploymentDto> paginatedResponse = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
                });

        assertNotNull(paginatedResponse, "Paginated response is null");
        assertNotNull(paginatedResponse.getData(), "Device list is null");
        assertEquals(10, paginatedResponse.getData().size(), "Pagination size mismatch");

        assertEquals(15, paginatedResponse.getTotalRecords(), "Total records mismatch");
        assertEquals(2, paginatedResponse.getTotalPages(), "Total pages mismatch");
    }


    @Test
    void shouldGetDeviceByMacAddress() {
        String macAddress = "00:11:22:33:44:60";
        registerDevice(macAddress, DeviceType.Gateway, null);

        ResponseEntity<NetworkDeviceDeploymentDto> response = restTemplate.getForEntity("/api/v1/network/deployments/" + macAddress, NetworkDeviceDeploymentDto.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch device by MAC address");
        assertNotNull(response.getBody(), "Response body is null");
        assertEquals(macAddress, response.getBody().getMacAddress(), "MAC Address mismatch");
    }

    @Test
    void shouldReturn404ForNonexistentDevice() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments/00:11:22:33:44:99", String.class);
        assertEquals(404, response.getStatusCode().value(), "Nonexistent device should return 404");
    }

    @Test
    void shouldGetDeviceTopology() throws Exception {
        registerDevice("00:11:22:33:44:61", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:62", DeviceType.Switch, "00:11:22:33:44:61");
        registerDevice("00:11:22:33:44:63", DeviceType.AccessPoint, "00:11:22:33:44:62");

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments/topology", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch device topology");

        List<DeviceTopologyDto> topology = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertNotNull(topology, "Topology is null");
        assertFalse(topology.isEmpty(), "Topology list is empty");
        assertEquals(3, topology.size(), "Topology size mismatch");
    }

    @Test
    void shouldGetDeviceTopologyFromSpecificMacAddress() throws Exception {
        registerDevice("00:11:22:33:44:64", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:65", DeviceType.Switch, "00:11:22:33:44:64");
        registerDevice("00:11:22:33:44:66", DeviceType.AccessPoint, "00:11:22:33:44:65");

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments/topology/00:11:22:33:44:65", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch device topology for MAC address");

        List<DeviceTopologyDto> topology = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertNotNull(topology, "Topology is null");
        assertEquals(2, topology.size(), "Topology size mismatch from specific MAC address");
    }

    @Test
    void shouldHandleLargeTopologyEfficiently() throws Exception {
        String rootMac = "00:11:22:33:44:70";
        registerDevice(rootMac, DeviceType.Gateway, null);
        for (int i = 71; i < 100; i++) {
            registerDevice(String.format("00:11:22:33:44:%02d", i), DeviceType.Switch, rootMac);
        }

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments/topology", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch large topology");

        List<DeviceTopologyDto> topology = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertEquals(30, topology.size(), "Large topology size mismatch");
    }

    @Test
    void shouldDetectCyclicTopology() {
        registerDevice("00:11:22:33:44:80", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:81", DeviceType.Switch, "00:11:22:33:44:80");
        registerDevice("00:11:22:33:44:82", DeviceType.AccessPoint, "00:11:22:33:44:81");

        NetworkDeviceDeploymentDto cyclicDevice = new NetworkDeviceDeploymentDto("00:11:22:33:44:80", DeviceType.Gateway, "00:11:22:33:44:82");
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(cyclicDevice, createJsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);

        assertEquals(400, response.getStatusCode().value(), "Cyclic topology should be rejected");
    }

    @Test
    void shouldHandleMissingUplinksGracefully() {
        NetworkDeviceDeploymentDto missingUplinkDevice = new NetworkDeviceDeploymentDto("00:11:22:33:44:90", DeviceType.Switch, "00:11:22:33:44:99"); // Uplink does not exist
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(missingUplinkDevice, createJsonHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);

        assertEquals(400, response.getStatusCode().value(), "Devices with missing uplinks should not be allowed");
    }

    @Test
    void shouldRejectInvalidDeviceType() {
        String macAddress = "00:11:22:33:44:95";
        NetworkDeviceDeploymentDto invalidDevice = new NetworkDeviceDeploymentDto(macAddress, null, null);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(invalidDevice, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectDeviceWithInvalidUplink() {
        String macAddress = "00:11:22:33:44:96";
        NetworkDeviceDeploymentDto deviceWithInvalidUplink = new NetworkDeviceDeploymentDto(macAddress, DeviceType.Switch, "00:11:22:33:44:99");
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(deviceWithInvalidUplink, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectDeviceWithEmptyMacAddress() {
        NetworkDeviceDeploymentDto deviceWithEmptyMac = new NetworkDeviceDeploymentDto("", DeviceType.Gateway, null);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(deviceWithEmptyMac, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectDeviceWithInvalidUplinkMacAddress() {
        NetworkDeviceDeploymentDto deviceWithInvalidUplinkFormat = new NetworkDeviceDeploymentDto("00:11:22:33:44:97", DeviceType.Switch, "INVALID-MAC");
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(deviceWithInvalidUplinkFormat, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectDeviceWithInvalidPaginationParameters() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments?page=-1&size=0", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldReturn400ForDeviceWithEmptyDeviceType() {
        String macAddress = "00:11:22:33:44:99";
        NetworkDeviceDeploymentDto deviceWithEmptyType = new NetworkDeviceDeploymentDto(macAddress, null, null);
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(deviceWithEmptyType, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldHandleInvalidPageRequest() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments?page=99999&size=10", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldRejectDuplicateDeviceRegistrationWithDifferentUplink() {
        registerDevice("00:11:22:33:44:10", DeviceType.AccessPoint, null);
        NetworkDeviceDeploymentDto duplicateDeviceWithDifferentUplink = new NetworkDeviceDeploymentDto("00:11:22:33:44:100", DeviceType.Switch, "00:11:22:33:44:99");
        HttpEntity<NetworkDeviceDeploymentDto> request = new HttpEntity<>(duplicateDeviceWithDifferentUplink, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/network/deployments", request, String.class);
        assertEquals(400, response.getStatusCode().value());
    }


    @Test
    void shouldSortDevicesByType() throws Exception {
        registerDevice("00:11:22:33:44:91", DeviceType.AccessPoint, null);
        registerDevice("00:11:22:33:44:92", DeviceType.Gateway, null);
        registerDevice("00:11:22:33:44:93", DeviceType.Switch, null);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments", String.class);
        assertEquals(200, response.getStatusCode().value(), "Failed to fetch sorted devices");

        PaginatedResponse<NetworkDeviceDeploymentDto> paginatedResponse = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {
                });

        assertNotNull(paginatedResponse, "Paginated response is null");
        assertNotNull(paginatedResponse.getData(), "Device list is null");
        assertEquals(3, paginatedResponse.getData().size(), "Device count mismatch");

        assertEquals(DeviceType.Gateway, paginatedResponse.getData().get(0).getDeviceType(), "First device type mismatch");
        assertEquals(DeviceType.Switch, paginatedResponse.getData().get(1).getDeviceType(), "Second device type mismatch");
        assertEquals(DeviceType.AccessPoint, paginatedResponse.getData().get(2).getDeviceType(), "Third device type mismatch");
    }

    @Test
    void shouldHandleMultiLevelTopology() throws Exception {
        registerDevice("00:11:22:33:50:01", DeviceType.Gateway, null); // Level 1
        registerDevice("00:11:22:33:50:02", DeviceType.Switch, "00:11:22:33:50:01"); // Level 2
        registerDevice("00:11:22:33:50:03", DeviceType.Switch, "00:11:22:33:50:02"); // Level 3
        registerDevice("00:11:22:33:50:04", DeviceType.AccessPoint, "00:11:22:33:50:03"); // Level 4

        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/network/deployments/topology", String.class);

        assertEquals(200, response.getStatusCode().value(), "Failed to fetch device topology");
        List<DeviceTopologyDto> topology = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertNotNull(topology, "Topology is null");
        assertEquals(4, topology.size(), "Incorrect topology size");

        assertEquals("00:11:22:33:50:01", topology.get(0).getMacAddress(), "Root device mismatch");
        assertEquals("00:11:22:33:50:02", topology.get(1).getMacAddress(), "Level 2 device mismatch");
        assertEquals("00:11:22:33:50:03", topology.get(2).getMacAddress(), "Level 3 device mismatch");
        assertEquals("00:11:22:33:50:04", topology.get(3).getMacAddress(), "Level 4 device mismatch");
    }

}
