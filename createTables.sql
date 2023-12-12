-- auto-generated definition
create table answer
(
    id               varchar(20) not null
        primary key,
    last_active_date datetime    null,
    down_vote        int         null,
    up_vote          int         null,
    content          text        null,
    score            int         null,
    title            text        null,
    is_accepted      tinyint(1)  null,
    question_id      varchar(20) null,
    owner_id         varchar(20) null
);

-- auto-generated definition
create table answer_tag_relation
(
    answer_id varchar(20) not null,
    tag_id    int         not null
);

create index answer_tag_relation_answer_id_fk
    on answer_tag_relation (answer_id);

create index answer_tag_relation_tag_id_fk
    on answer_tag_relation (tag_id);

-- auto-generated definition
create table comment
(
    id            varchar(20) not null
        primary key,
    owner_id      varchar(20) null,
    content       text        null,
    score         int         null,
    creation_date datetime    null,
    post_id       varchar(20) null
);

-- auto-generated definition
create table owner
(
    id         varchar(20) not null
        primary key,
    name       text        null,
    reputation int         null
);

-- auto-generated definition
create table question
(
    id               varchar(20) not null
        primary key,
    owner_id         varchar(20) null,
    view_count       int         null,
    favorite_count   int         null,
    up_vote          int         null,
    down_vote        int         null,
    closed_date      datetime    null,
    score            int         null,
    last_active_date datetime    null,
    creation_date    datetime    null,
    title            text        null,
    content          text        null
);

-- auto-generated definition
create table question_tag_relation
(
    question_id varchar(20) null,
    tag_id      int         null
);

create index question_tag_relation_question_id_fk
    on question_tag_relation (question_id);

create index question_tag_relation_tag_id_fk
    on question_tag_relation (tag_id);

-- auto-generated definition
create table tag
(
    id       int auto_increment
        primary key,
    tag_name varchar(100) null,
    constraint tag_pk
        unique (tag_name)
);

