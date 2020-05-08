create table files
(
    id           uuid not null constraint files_pk primary key,
    local_path   varchar(255),
    content_md5  varchar(32) not null,
    content_type varchar(128) not null,
    content_size bigint not null,
    file_name    text,
    created_at   timestamptz not null
);

create unique index files_local_path_uindex
    on files (local_path);

create index files_created_at_index
    on files (created_at);
