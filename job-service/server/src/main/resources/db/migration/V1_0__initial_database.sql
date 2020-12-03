create table jobs
(
    id             uuid        not null
        constraint jobs_pk
            primary key,
    status         varchar(64) not null,
    created_at     timestamptz   not null,
    profile        varchar (64) not null
);

create table job_tasks
(
    job_id         uuid not null
        constraint job_tasks_fk_job_id
            references jobs
            on update cascade on delete cascade,
    file_id        uuid         not null,
    status         varchar(64) not null,
    error_type     varchar(64),
    error_message  varchar(64),
    result_file    uuid,
    PRIMARY KEY (file_id, job_id)
);
