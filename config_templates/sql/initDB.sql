DROP TABLE IF EXISTS projects cascade;
DROP TABLE IF EXISTS groups cascade;
DROP TABLE IF EXISTS city cascade;
DROP TABLE IF EXISTS users cascade;
DROP TABLE IF EXISTS ref_user_group cascade;
DROP SEQUENCE IF EXISTS all_seq;
DROP TYPE IF EXISTS user_flag;
DROP TYPE IF EXISTS group_type;
DROP TYPE IF EXISTS project_type;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');

create type group_type as enum ('registered','current','finished');

CREATE SEQUENCE all_seq START 100000;



create table projects(
    id integer primary key default nextval('all_seq'),
    name text not null,
    description text
);

create unique index uidx_prjcts_name on projects(name);

create table groups
(
    id   integer primary key default nextval('all_seq'),
    name text       not null,
    type group_type not null,
    id_project integer,-- not null,
    constraint fk_project foreign key (id_project) references projects(id) on delete cascade
);

create unique index uidx_group_name on groups (name);

create table city
(
    id   integer primary key default nextval('all_seq'),
    name text not null,
    code text not null
);

create unique index uidx_city_code on city (code);

CREATE TABLE users
(
    id        INTEGER PRIMARY KEY DEFAULT nextval('all_seq'),
    full_name TEXT      NOT NULL,
    email     TEXT      NOT NULL,
    flag      user_flag NOT NULL,
    id_city   integer,--  not null,
    constraint fk_city foreign key (id_city) references city (id) on delete cascade
);

CREATE UNIQUE INDEX email_uidx ON users (email);

create table ref_user_group (
    id_user integer not null,
    id_group integer not null,
    constraint fk_users foreign key (id_user) references users(id) on delete cascade ,
    constraint fk_groups foreign key (id_group) references groups(id) on delete cascade
);

create unique index uidx_ref_u_g on ref_user_group(id_user, id_group);