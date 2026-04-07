-- 서비스별 논리 분리를 위한 스키마 생성
CREATE SCHEMA IF NOT EXISTS delivery;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS hub;
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS gateway;

-- =====================================================
-- delivery 스키마 테이블 DDL
-- =====================================================

CREATE TABLE IF NOT EXISTS delivery.p_delivery (
    id                         UUID                     NOT NULL,
    order_id                   UUID                     NOT NULL,
    source_hub_id              UUID,
    destination_hub_id         UUID,
    delivery_address           VARCHAR(255),
    recipient                  VARCHAR(100),
    recipient_slack_id         VARCHAR(100),
    vendor_delivery_manager_id UUID,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by                 UUID                     NOT NULL,
    updated_by                 UUID                     NOT NULL,
    deleted_at                 TIMESTAMP WITH TIME ZONE,
    deleted_by                 UUID,
    CONSTRAINT p_delivery_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS delivery.p_shipment (
    id                    UUID                     NOT NULL,
    delivery_id           UUID                     NOT NULL,
    sequence              INTEGER                  NOT NULL,
    status                VARCHAR(30)              NOT NULL,
    type                  VARCHAR(30)              NOT NULL,
    from_node_id          UUID                     NOT NULL,
    from_node_type        VARCHAR(30)              NOT NULL,
    from_node_name        VARCHAR(50)              NOT NULL,
    to_node_id            UUID                     NOT NULL,
    to_node_type          VARCHAR(30)              NOT NULL,
    to_node_name          VARCHAR(50)              NOT NULL,
    delivery_manager_id   UUID,
    delivery_manager_name VARCHAR(50),
    _version              INTEGER                  NOT NULL DEFAULT 0,
    estimated_distance    NUMERIC(38, 2),
    estimated_duration    NUMERIC(38, 2),
    actual_distance       NUMERIC(38, 2),
    actual_duration       NUMERIC(38, 2),
    shipped_at            TIMESTAMP WITH TIME ZONE,
    arrived_at            TIMESTAMP WITH TIME ZONE,
    completed_at          TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by            UUID                     NOT NULL,
    updated_by            UUID                     NOT NULL,
    deleted_at            TIMESTAMP WITH TIME ZONE,
    deleted_by            UUID,
    CONSTRAINT p_shipment_pkey PRIMARY KEY (id),
    CONSTRAINT p_shipment_delivery_id_sequence_key UNIQUE (delivery_id, sequence),
    CONSTRAINT fke2fwtye1djkmfn17m58aphq9b FOREIGN KEY (delivery_id) REFERENCES delivery.p_delivery (id),
    CONSTRAINT p_shipment_status_check CHECK (status IN ('PENDING', 'SHIPPED', 'IN_TRANSIT', 'ARRIVED', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT p_shipment_type_check CHECK (type IN ('HUB_TO_HUB', 'HUB_TO_VENDOR')),
    CONSTRAINT p_shipment_from_node_type_check CHECK (from_node_type IN ('HUB', 'VENDOR')),
    CONSTRAINT p_shipment_to_node_type_check CHECK (to_node_type IN ('HUB', 'VENDOR'))
);
