INSERT INTO users (name, email) VALUES
('Kim Developer', 'kim@example.com'),
('Lee Engineer', 'lee@example.org'),
('Park Backend', 'park@example.com');

INSERT INTO work_orders (title, customer_name, assignee, status) VALUES
('신규 계정 등록 요청', '새한상사', 'Kim Developer', 'RECEIVED'),
('월간 데이터 정리', '에이스테크', 'Lee Engineer', 'IN_PROGRESS'),
('Excel export 검수', '미래유통', 'Park Backend', 'DONE'),
('중복 고객 데이터 확인', '대한솔루션', 'Kim Developer', 'CANCELLED');

INSERT INTO work_order_activities (work_order_id, actor, action, from_status, to_status, detail) VALUES
(1, 'demo-admin', 'CREATED', NULL, 'RECEIVED', '업무가 접수되었습니다.'),
(2, 'demo-admin', 'CREATED', NULL, 'RECEIVED', '업무가 접수되었습니다.'),
(2, 'demo-staff', 'STATUS_CHANGED', 'RECEIVED', 'IN_PROGRESS', '업무 상태가 RECEIVED에서 IN_PROGRESS로 변경되었습니다.'),
(3, 'demo-admin', 'CREATED', NULL, 'RECEIVED', '업무가 접수되었습니다.'),
(3, 'demo-staff', 'STATUS_CHANGED', 'RECEIVED', 'IN_PROGRESS', '업무 상태가 RECEIVED에서 IN_PROGRESS로 변경되었습니다.'),
(3, 'demo-admin', 'STATUS_CHANGED', 'IN_PROGRESS', 'DONE', '업무 상태가 IN_PROGRESS에서 DONE으로 변경되었습니다.'),
(4, 'demo-admin', 'CREATED', NULL, 'RECEIVED', '업무가 접수되었습니다.'),
(4, 'demo-admin', 'STATUS_CHANGED', 'RECEIVED', 'CANCELLED', '업무 상태가 RECEIVED에서 CANCELLED로 변경되었습니다.');
