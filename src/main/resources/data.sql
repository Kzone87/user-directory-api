INSERT INTO users (name, email) VALUES
('Kim Developer', 'kim@example.com'),
('Lee Engineer', 'lee@example.org'),
('Park Backend', 'park@example.com');

INSERT INTO work_orders (title, customer_name, assignee, status) VALUES
('신규 계정 등록 요청', '새한상사', 'Kim Developer', 'RECEIVED'),
('월간 데이터 정리', '에이스테크', 'Lee Engineer', 'IN_PROGRESS'),
('Excel export 검수', '미래유통', 'Park Backend', 'DONE'),
('중복 고객 데이터 확인', '대한솔루션', 'Kim Developer', 'CANCELLED');
