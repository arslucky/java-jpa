insert into customer(id, name) values
(1, 'customer1'),
(2, 'customer2');
insert into contract(id, number, customer_id, create_date) values
(1, '001', 1, '2024-03-30 09:01:00'),
(2, '002', 1, '2024-03-30 09:02:00'),
(3, '003', 1, '2024-03-30 09:03:00'),
(4, '001', 2, '2024-03-30 09:04:00'),
(5, '002', 2, '2024-03-30 09:05:00');