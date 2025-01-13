CREATE TABLE NETWORK_DEVICE_DEPLOYMENT
(
    mac_address        VARCHAR(17) PRIMARY KEY,
    device_type        VARCHAR(64) NOT NULL,
    uplink_mac_address VARCHAR(17),
    FOREIGN KEY (uplink_mac_address) REFERENCES NETWORK_DEVICE_DEPLOYMENT (mac_address) ON DELETE SET NULL
);
