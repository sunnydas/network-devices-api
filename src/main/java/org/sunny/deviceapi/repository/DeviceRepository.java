package org.sunny.deviceapi.repository;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.sunny.deviceapi.exception.DeviceNotFoundException;
import org.sunny.deviceapi.exception.DuplicateDeviceException;
import org.sunny.deviceapi.exception.UplinkDeviceNotFoundException;
import org.sunny.deviceapi.models.db.NetworkDeviceDeployment;
import org.sunny.deviceapi.models.dto.DeviceTopologyDto;

import java.sql.SQLException;
import java.util.List;

@Repository
public class DeviceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRepository.class);

    private final Jdbi jdbi;

    public DeviceRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void registerToNetworkDeployment(final NetworkDeviceDeployment networkDeviceDeployment) {
        try {
            jdbi.useHandle(handle ->
                    handle.createUpdate("""
                                    INSERT INTO NETWORK_DEVICE_DEPLOYMENT (mac_address, device_type, uplink_mac_address)
                                    VALUES (:macAddress, :deviceType, :uplinkMacAddress)
                                    """)
                            .bind("macAddress", networkDeviceDeployment.getMacAddress())
                            .bind("deviceType", networkDeviceDeployment.getDeviceType().name())
                            .bind("uplinkMacAddress", networkDeviceDeployment.getUplinkMacAddress())
                            .execute()
            );
        } catch (UnableToExecuteStatementException e) {
            handleStatementExecutionFailure(networkDeviceDeployment, e);
        }
    }

    private static void handleStatementExecutionFailure(NetworkDeviceDeployment networkDeviceDeployment, UnableToExecuteStatementException e) {
        var cause = e.getCause();
        LOGGER.error("Error while registering networkDeviceDeployment: {}", networkDeviceDeployment, cause);
        if (cause instanceof SQLException sqlException) {
            if ("23505".equals(sqlException.getSQLState()) || sqlException.getMessage().contains("duplicate key")) {
                throw new DuplicateDeviceException(String.format("Device with the same MAC address; %s already exists", networkDeviceDeployment.getMacAddress()), e);
            }
            if ("23506".equals(sqlException.getSQLState()) || sqlException.getMessage().contains("Referential integrity constraint violation")) {
                throw new UplinkDeviceNotFoundException(String.format("Device with the uplink MAC address: %s does not exist", networkDeviceDeployment.getUplinkMacAddress()), e);
            }
        }
        throw e;
    }


    public List<NetworkDeviceDeployment> getAllNetworkDeviceDeployments(int page, int size) {
        int offset = (page - 1) * size;
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT mac_address, device_type, uplink_mac_address
                                FROM NETWORK_DEVICE_DEPLOYMENT
                                ORDER BY CASE device_type
                                    WHEN 'Gateway' THEN 1
                                    WHEN 'Switch' THEN 2
                                    WHEN 'AccessPoint' THEN 3
                                END
                                LIMIT :limit OFFSET :offset
                                """)
                        .bind("limit", size)
                        .bind("offset", offset)
                        .mapToBean(NetworkDeviceDeployment.class)
                        .list()
        );
    }

    public long getTotalDeviceCount() {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT COUNT(*)
                                FROM NETWORK_DEVICE_DEPLOYMENT
                                """)
                        .mapTo(Long.class)
                        .findFirst()
                        .orElse(0L)
        );
    }

    public NetworkDeviceDeployment getNetworkDeviceDeploymentByMacAddress(String macAddress) throws DeviceNotFoundException {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT mac_address, device_type, uplink_mac_address
                                FROM NETWORK_DEVICE_DEPLOYMENT
                                WHERE mac_address = :macAddress
                                """)
                        .bind("macAddress", macAddress)
                        .mapToBean(NetworkDeviceDeployment.class)
                        .findOne()
                        .orElseThrow(() -> new DeviceNotFoundException(
                                String.format("Device with mac address: %s does not exist", macAddress)))
        );
    }

    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopology() {
        return getNetworkDeviceDeploymentTopology(null);
    }

    public List<DeviceTopologyDto> getNetworkDeviceDeploymentTopologyByMacAddress(String macAddress) {
        return getNetworkDeviceDeploymentTopology(macAddress);
    }

    private List<DeviceTopologyDto> getNetworkDeviceDeploymentTopology(String macAddress) {
        return jdbi.withHandle(handle -> {
            String query = """
                    WITH RECURSIVE DeviceTree(mac_address, device_type, uplink_mac_address, level, parent_mac_address) AS (
                        SELECT mac_address,
                               device_type,
                               uplink_mac_address,
                               1 AS level,
                               NULL AS parent_mac_address
                        FROM NETWORK_DEVICE_DEPLOYMENT
                        WHERE %s
                    
                        UNION ALL
                    
                        SELECT ndd.mac_address,
                               ndd.device_type,
                               ndd.uplink_mac_address,
                               dt.level + 1 AS level,
                               dt.mac_address AS parent_mac_address
                        FROM NETWORK_DEVICE_DEPLOYMENT ndd
                        INNER JOIN DeviceTree dt ON ndd.uplink_mac_address = dt.mac_address
                    )
                    SELECT mac_address,
                           device_type,
                           uplink_mac_address,
                           level,
                           parent_mac_address
                    FROM DeviceTree
                    ORDER BY level, parent_mac_address, mac_address
                    """;

            if (macAddress == null) {
                query = String.format(query, "uplink_mac_address IS NULL");
                return handle.createQuery(query)
                        .mapToBean(DeviceTopologyDto.class)
                        .list();
            } else {
                query = String.format(query, "mac_address = :macAddress");
                return handle.createQuery(query)
                        .bind("macAddress", macAddress)
                        .mapToBean(DeviceTopologyDto.class)
                        .list();
            }
        });
    }


}