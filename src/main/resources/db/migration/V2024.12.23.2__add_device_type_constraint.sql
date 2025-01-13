ALTER TABLE NETWORK_DEVICE_DEPLOYMENT
    ADD CONSTRAINT device_type_check
        CHECK (device_type IN ('Gateway', 'Switch', 'AccessPoint'));

CREATE INDEX idx_uplink_mac_address ON NETWORK_DEVICE_DEPLOYMENT (uplink_mac_address);