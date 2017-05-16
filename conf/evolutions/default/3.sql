

# --- !Ups

alter table model_import_library
   ADD column IF NOT EXISTS project_id varchar(255),
   drop column IF EXISTS tag,
   ADD column IF NOT EXISTS markdown_description text,
   DROP column IF EXISTS state CASCADE,
   DROP column IF EXISTS long_description;

alter table IF EXISTS model_import_library rename to model_library;

drop table if exists model_import_library_model_type_ cascade;

create table model_library_model_type_of_b (
   model_library_id        varchar(255) not null,
   model_type_of_board_id  varchar(255) not null,
   constraint pk_model_library_model_type_ primary key (model_library_id, model_type_of_board_id))
;


# --- !Downs

alter table model_import_library
   DROP column IF EXISTS project_id,
   ADD column IF NOT EXISTS tag varchar(9),
   ADD constraint ck_model_import_library_tag check (tag in ('MATH','BLUETOOTH','AUDIO','SPI','WIFI')),
   DROP column IF EXISTS markdown_description,
   ADD column state VARCHAR(10),
   ADD constraint ck_model_import_library_state check (state in ('NEW','TESTED','DEPRECATED')),
   ADD column IF NOT EXISTS long_description varchar(255);

alter table IF EXISTS model_library rename to model_import_library;

drop table if exists model_library_model_type_of_b cascade;

create table model_import_library_model_type_ (
   model_import_library_id        varchar(255) not null,
   model_type_of_board_id         varchar(255) not null,
   constraint pk_model_import_library_model_type_ primary key (model_import_library_id, model_type_of_board_id))
;
