INSERT INTO customers (name, email) VALUES
('Mariana Souza', 'mariana@exemplo.com'),
('Carlos Lima', 'carlos@exemplo.com'),
('Fernanda Oliveira', 'fernanda@exemplo.com'),
('Bruno Costa', 'bruno@exemplo.com'),
('Juliana Martins', 'juliana@exemplo.com');

INSERT INTO orders (order_number, customer_id, total, status, tracking_code, created_at) VALUES
('RC10652', 1, 3499.90, 'OUT_FOR_DELIVERY', 'RC-2026-SP-8F29A73', NOW() - INTERVAL '2 days'),
('RC10651', 2, 799.90, 'IN_TRANSIT', 'RC-2026-SP-7C91822', NOW() - INTERVAL '2 days'),
('RC10650', 3, 219.90, 'PICKING', 'RC-2026-RJ-4D72E18', NOW() - INTERVAL '1 day'),
('RC10649', 4, 129.90, 'DELIVERED', 'RC-2026-SP-6A34F91', NOW() - INTERVAL '1 day'),
('RC10648', 5, 459.90, 'OUT_FOR_DELIVERY', 'RC-2026-MG-2B5CC09', NOW());

INSERT INTO tracking_events (order_id, status, location, event_time) VALUES
(1, 'ORDER_CREATED', 'Loja virtual', NOW() - INTERVAL '2 days'),
(1, 'PAYMENT_APPROVED', 'Pagamento aprovado', NOW() - INTERVAL '47 hours'),
(1, 'PICKING', 'Centro de distribuição - São Paulo/SP', NOW() - INTERVAL '44 hours'),
(1, 'PACKING', 'Centro de distribuição - São Paulo/SP', NOW() - INTERVAL '42 hours'),
(1, 'IN_TRANSIT', 'Hub logístico - São Paulo/SP', NOW() - INTERVAL '8 hours'),
(1, 'OUT_FOR_DELIVERY', 'São Paulo/SP', NOW() - INTERVAL '2 hours'),
(2, 'ORDER_CREATED', 'Loja virtual', NOW() - INTERVAL '2 days'),
(2, 'PAYMENT_APPROVED', 'Pagamento aprovado', NOW() - INTERVAL '46 hours'),
(2, 'IN_TRANSIT', 'Hub logístico - Campinas/SP', NOW() - INTERVAL '4 hours'),
(3, 'ORDER_CREATED', 'Loja virtual', NOW() - INTERVAL '1 day'),
(3, 'PAYMENT_APPROVED', 'Pagamento aprovado', NOW() - INTERVAL '22 hours'),
(3, 'PICKING', 'Centro de distribuição - Rio de Janeiro/RJ', NOW() - INTERVAL '3 hours'),
(4, 'ORDER_CREATED', 'Loja virtual', NOW() - INTERVAL '1 day'),
(4, 'DELIVERED', 'São Paulo/SP', NOW() - INTERVAL '1 hour'),
(5, 'ORDER_CREATED', 'Loja virtual', NOW() - INTERVAL '10 hours'),
(5, 'OUT_FOR_DELIVERY', 'Belo Horizonte/MG', NOW() - INTERVAL '1 hour');
