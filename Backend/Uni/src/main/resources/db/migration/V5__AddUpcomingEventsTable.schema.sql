create table if not exists
upcoming_events
(
    id          bigint auto_increment primary key,
    title       varchar(255)  not null,
    subtitle    varchar(500),
    event_date  datetime      not null,
    type        varchar(20)   not null comment 'HIGH_PRIORITY | EXAM | EVENT',
    user_id     bigint        not null,
    created_at  timestamp     not null default current_timestamp,

    constraint fk_upcoming_event_user
        foreign key (user_id) references users(id) on delete cascade,

    index idx_upcoming_event_date (event_date),
    index idx_upcoming_event_type (type),
    index idx_upcoming_event_user (user_id)
);
