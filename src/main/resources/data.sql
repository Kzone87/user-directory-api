INSERT INTO users (name, email) VALUES
('Kim Developer', 'kim@example.com'),
('Lee Engineer', 'lee@example.org'),
('Park Backend', 'park@example.com');

INSERT INTO customers (company_name, contact_name, email, phone, status, memo) VALUES
('Alpha Trading', 'Alex Kim', 'alpha@example.com', '010-1111-1111', 'ACTIVE', 'Fictional demo customer'),
('Beta Tech', 'Blair Lee', 'beta@example.com', '010-2222-2222', 'ACTIVE', 'Fictional demo customer'),
('Gamma Retail', 'Casey Park', 'gamma@example.com', '02-3333-3333', 'ACTIVE', 'Fictional demo customer'),
('Delta Solutions', 'Drew Choi', 'delta@example.com', '031-444-4444', 'INACTIVE', 'Fictional demo customer'),
('Echo Studio', 'Evan Jung', 'echo@example.com', '051-555-5555', 'LEAD', 'Fictional demo lead');

INSERT INTO work_orders (title, customer_id, assignee, status, priority, due_date) VALUES
('Account setup request', 1, 'Kim Developer', 'RECEIVED', 'URGENT', DATE '2026-09-05'),
('Monthly data cleanup', 2, 'Lee Engineer', 'IN_PROGRESS', 'HIGH', DATE '2026-09-04'),
('Excel export review', 3, 'Park Backend', 'DONE', 'NORMAL', DATE '2026-09-03'),
('Duplicate data review', 4, 'Kim Developer', 'CANCELLED', 'LOW', NULL);

INSERT INTO work_order_activities (work_order_id, actor, action, from_status, to_status, detail) VALUES
(1, 'demo-admin', 'CREATED', NULL, 'RECEIVED', 'Work order was created.'),
(2, 'demo-admin', 'CREATED', NULL, 'RECEIVED', 'Work order was created.'),
(2, 'demo-staff', 'STATUS_CHANGED', 'RECEIVED', 'IN_PROGRESS', 'Status changed from RECEIVED to IN_PROGRESS.'),
(3, 'demo-admin', 'CREATED', NULL, 'RECEIVED', 'Work order was created.'),
(3, 'demo-staff', 'STATUS_CHANGED', 'RECEIVED', 'IN_PROGRESS', 'Status changed from RECEIVED to IN_PROGRESS.'),
(3, 'demo-admin', 'STATUS_CHANGED', 'IN_PROGRESS', 'DONE', 'Status changed from IN_PROGRESS to DONE.'),
(4, 'demo-admin', 'CREATED', NULL, 'RECEIVED', 'Work order was created.'),
(4, 'demo-admin', 'STATUS_CHANGED', 'RECEIVED', 'CANCELLED', 'Status changed from RECEIVED to CANCELLED.');
