create table  if not exists 
announcements
(
    id          bigint auto_increment primary key,
    course_id   bigint        not null,
    title       varchar(255)  not null,
    description varchar(1000) not null,
    created_at  timestamp     not null default current_timestamp,

    constraint fk_announcement_course 
        foreign key (course_id) references courses(id) on delete cascade,
    
    index idx_announcement_course (course_id),
    index idx_announcement_created (created_at)
);
