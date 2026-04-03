-- 기존 데이터 초기화
-- =====================================================
--   psql -h localhost -U postgres -d delivery_db -f docs/mock-data.sql
-- =====================================================

DELETE
FROM p_shipment;
DELETE
FROM p_delivery;

-- =====================================================
-- 노드 UUID 정리
-- 허브: 서울(dddd-01), 부산(dddd-02), 대구(dddd-05)
-- 업체: 부산업체(dddd-03), 서울업체(dddd-04), 대구업체(dddd-06)
-- 배송담당자: 홍길동(eeee-01), 김철수(eeee-02), 이영희(eeee-03)
-- =====================================================

-- =====================================================
-- DELIVERY 1: READY | 서울→부산→부산업체 | 5일전 생성
-- 허브관리자(서울/부산), 업체담당자(부산업체) 조회 대상
-- =====================================================
INSERT INTO p_delivery (id, order_id, status, created_at, updated_at, created_by, updated_by)
VALUES ('aaaaaaaa-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000001',
        'READY', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000001', 'aaaaaaaa-0000-0000-0000-000000000001',
        1, 'PENDING', 'HUB_TO_HUB',
        'dddddddd-0000-0000-0000-000000000001', 'HUB', '서울 허브',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000002', 'aaaaaaaa-0000-0000-0000-000000000001',
        2, 'PENDING', 'HUB_TO_VENDOR',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'dddddddd-0000-0000-0000-000000000003', 'VENDOR', '부산 업체',
        NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

-- =====================================================
-- DELIVERY 2: IN_PROGRESS | 서울→서울업체 | 홍길동 배송중
-- 허브관리자(서울), 배송담당자(홍길동) 조회 대상
-- =====================================================
INSERT INTO p_delivery (id, order_id, status, created_at, updated_at, created_by, updated_by)
VALUES ('aaaaaaaa-0000-0000-0000-000000000002', 'bbbbbbbb-0000-0000-0000-000000000002',
        'IN_PROGRESS', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        delivery_manager_id, delivery_manager_name,
                        shipped_at,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000003', 'aaaaaaaa-0000-0000-0000-000000000002',
        1, 'SHIPPED', 'HUB_TO_VENDOR',
        'dddddddd-0000-0000-0000-000000000001', 'HUB', '서울 허브',
        'dddddddd-0000-0000-0000-000000000004', 'VENDOR', '서울 업체',
        'eeeeeeee-0000-0000-0000-000000000001', '홍길동',
        NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

-- =====================================================
-- DELIVERY 3: IN_PROGRESS | 서울→부산→대구→대구업체 | 홍길동 이동중 (1구간 완료)
-- 허브관리자(서울/부산/대구), 배송담당자(홍길동) 조회 대상
-- lastestProgressedNode = 부산 허브 (seq2 IN_TRANSIT의 FROM)
-- =====================================================
INSERT INTO p_delivery (id, order_id, status, created_at, updated_at, created_by, updated_by)
VALUES ('aaaaaaaa-0000-0000-0000-000000000003', 'bbbbbbbb-0000-0000-0000-000000000003',
        'IN_PROGRESS', NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        delivery_manager_id, delivery_manager_name,
                        shipped_at, arrived_at, completed_at,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000004', 'aaaaaaaa-0000-0000-0000-000000000003',
        1, 'COMPLETED', 'HUB_TO_HUB',
        'dddddddd-0000-0000-0000-000000000001', 'HUB', '서울 허브',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'eeeeeeee-0000-0000-0000-000000000001', '홍길동',
        NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days',
        NOW() - INTERVAL '4 days', NOW() - INTERVAL '2 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        delivery_manager_id, delivery_manager_name,
                        shipped_at,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000005', 'aaaaaaaa-0000-0000-0000-000000000003',
        2, 'IN_TRANSIT', 'HUB_TO_HUB',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'dddddddd-0000-0000-0000-000000000005', 'HUB', '대구 허브',
        'eeeeeeee-0000-0000-0000-000000000001', '홍길동',
        NOW() - INTERVAL '1 day',
        NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000006', 'aaaaaaaa-0000-0000-0000-000000000003',
        3, 'PENDING', 'HUB_TO_VENDOR',
        'dddddddd-0000-0000-0000-000000000005', 'HUB', '대구 허브',
        'dddddddd-0000-0000-0000-000000000006', 'VENDOR', '대구 업체',
        NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

-- =====================================================
-- DELIVERY 4: COMPLETED | 부산→부산업체 | 김철수 완료
-- 허브관리자(부산), 배송담당자(김철수), 업체담당자(부산업체) 조회 대상
-- =====================================================
INSERT INTO p_delivery (id, order_id, status, created_at, updated_at, created_by, updated_by)
VALUES ('aaaaaaaa-0000-0000-0000-000000000004', 'bbbbbbbb-0000-0000-0000-000000000004',
        'COMPLETED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '7 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        delivery_manager_id, delivery_manager_name,
                        shipped_at, arrived_at, completed_at,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000007', 'aaaaaaaa-0000-0000-0000-000000000004',
        1, 'COMPLETED', 'HUB_TO_VENDOR',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'dddddddd-0000-0000-0000-000000000003', 'VENDOR', '부산 업체',
        'eeeeeeee-0000-0000-0000-000000000002', '김철수',
        NOW() - INTERVAL '9 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days',
        NOW() - INTERVAL '10 days', NOW() - INTERVAL '7 days', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

-- =====================================================
-- DELIVERY 5: READY | 서울→부산→부산업체 | 이영희 배정 대기
-- 허브관리자(서울/부산), 배송담당자(이영희), 업체담당자(부산업체) 조회 대상
-- =====================================================
INSERT INTO p_delivery (id, order_id, status, created_at, updated_at, created_by, updated_by)
VALUES ('aaaaaaaa-0000-0000-0000-000000000005', 'bbbbbbbb-0000-0000-0000-000000000005',
        'READY', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        delivery_manager_id, delivery_manager_name,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000008', 'aaaaaaaa-0000-0000-0000-000000000005',
        1, 'PENDING', 'HUB_TO_HUB',
        'dddddddd-0000-0000-0000-000000000001', 'HUB', '서울 허브',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'eeeeeeee-0000-0000-0000-000000000003', '이영희',
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);

INSERT INTO p_shipment (id, delivery_id, sequence, status, type,
                        from_node_id, from_node_type, from_node_name,
                        to_node_id, to_node_type, to_node_name,
                        created_at, updated_at, created_by, updated_by, _version)
VALUES ('cccccccc-0000-0000-0000-000000000009', 'aaaaaaaa-0000-0000-0000-000000000005',
        2, 'PENDING', 'HUB_TO_VENDOR',
        'dddddddd-0000-0000-0000-000000000002', 'HUB', '부산 허브',
        'dddddddd-0000-0000-0000-000000000003', 'VENDOR', '부산 업체',
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 0);