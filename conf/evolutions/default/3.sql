
# --- !Ups

ALTER TABLE productextension DROP COLUMN tariff_included_id;
ALTER TABLE productextension DROP COLUMN tariff_optional_id;

create table tariff_extensions_included (
  tariff_id                     uuid not null,
  tariff_extension_id           uuid not null,
  constraint pk_tariff_extensions_included primary key (tariff_id,tariff_extension_id)
);

create table tariff_extensions_recommended (
  tariff_id                     uuid not null,
  tariff_extension_id           uuid not null,
  constraint pk_tariff_extensions_recommended primary key (tariff_id,tariff_extension_id)
);

create table tariffextension (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  order_position                integer,
  color                         varchar(255),
  type                          varchar(12),
  configuration                 TEXT,
  active                        boolean default false not null,
  deleted                       boolean default false not null,
  constraint ck_tariffextension_type check ( type in ('database','instance','log','rest_api','project','support','homer_server','participant')),
  constraint pk_tariffextension primary key (id)
);


# --- !Downs

ALTER TABLE if exists productextension ADD COLUMN tariff_included_id uuid;
ALTER TABLE if exists productextension ADD COLUMN tariff_optional_id uuid;

alter table if exists tariff_extensions_included drop constraint if exists fk_tariff_extensions_included_tariff;
drop index if exists ix_tariff_extensions_included_tariff;

alter table if exists tariff_extensions_included drop constraint if exists fk_tariff_extensions_included_tariffextension;
drop index if exists ix_tariff_extensions_included_tariffextension;

alter table if exists tariff_extensions_recommended drop constraint if exists fk_tariff_extensions_recommended_tariff;
drop index if exists ix_tariff_extensions_recommended_tariff;

alter table if exists tariff_extensions_recommended drop constraint if exists fk_tariff_extensions_recommended_tariffextension;
drop index if exists ix_tariff_extensions_recommended_tariffextension;

drop table if exists tariff_extensions_included cascade;

drop table if exists tariff_extensions_recommended cascade;

drop table if exists tariffextension cascade;