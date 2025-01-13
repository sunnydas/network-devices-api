package org.sunny.deviceapi.models.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DeviceTopologyDto {

    private String macAddress;
    private String deviceType;
    private String uplinkMacAddress;
    private int level;
    private String parentMacAddress;

}
