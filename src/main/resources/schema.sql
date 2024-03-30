create table customer (
    id bigint not null,
    name varchar(255) not null,
    primary key (id)
);
create table contract (
    id bigint not null,
    number varchar(255) not null,
    customer_id bigint not null,
    create_date timestamp not null,
    primary key (id)
);