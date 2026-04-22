create table feedbacks
(
    id          bigint auto_increment
        primary key,
    user_id     bigint not null,
    role        varchar(20) not null,
    comment     nvarchar(1000),
    created_at  timestamp default CURRENT_TIMESTAMP,
    updated_at  timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    foreign key (user_id) references users (id) on delete cascade
);