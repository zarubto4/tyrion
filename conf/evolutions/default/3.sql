
# --- !Ups

alter table model_import_library
  add column project_id varchar(255),
  drop column if exists tag cascade,
  drop column if exists state cascade,
  drop constraint if exists ck_model_import_library_tag,
  drop constraint if exists ck_model_import_library_state,
  drop constraint if exists pk_model_import_library cascade,
  add constraint pk_model_library primary key (id);

alter table model_import_library rename column long_description to markdown_description;

alter table model_import_library rename to model_library;

alter table model_import_library_model_type_ rename column model_import_library_id to model_library_id;

alter table model_import_library_model_type_
  drop constraint if exists pk_model_import_library_model_type_,
  add constraint pk_model_library_model_type_of_boar primary key (model_library_id, model_type_of_board_id);

alter table model_import_library_model_type_ rename to model_library_model_type_of_boar;

alter table model_version_object
  drop constraint if exists fk_model_version_object_libra_75,
  add constraint fk_model_version_object_libra_75 foreign key (library_id) references model_library (id);


alter table model_library_model_type_of_boar
  drop constraint if exists fk_model_import_library_model_01,
  drop constraint if exists fk_model_import_library_model_02,
  add constraint fk_model_library_model_type_o_01 foreign key (model_library_id) references model_library (id),
  add constraint fk_model_library_model_type_o_02 foreign key (model_type_of_board_id) references model_type_of_board (id);


# --- !Downs

alter table model_library rename to model_import_library;

alter table model_import_library rename column markdown_description to long_description;

alter table model_import_library
  drop column if exists project_id,
  add column tag varchar(9),
  add column state VARCHAR(10),
  add constraint ck_model_import_library_tag check (tag in ('MATH','BLUETOOTH','AUDIO','SPI','WIFI')),
  add constraint ck_model_import_library_state check (state in ('NEW','TESTED','DEPRECATED')),
  drop constraint if exists pk_model_library cascade,
  add constraint pk_model_import_library primary key (id);

alter table model_library_model_type_of_boar rename to model_import_library_model_type_;

alter table model_import_library_model_type_ rename column model_library_id to model_import_library_id;

alter table model_import_library_model_type_
  drop constraint if exists pk_model_library_model_type_of_boar,
  add constraint pk_model_import_library_model_type_ primary key (model_import_library_id, model_type_of_board_id);

alter table model_version_object
  drop constraint if exists fk_model_version_object_libra_75,
  add constraint fk_model_version_object_libra_75 foreign key (library_id) references model_import_library (id);

alter table model_import_library_model_type_
  drop constraint if exists fk_model_library_model_type_o_01,
  drop constraint if exists fk_model_library_model_type_o_02,
  add constraint fk_model_import_library_model_01 foreign key (model_import_library_id) references model_import_library (id),
  add constraint fk_model_import_library_model_02 foreign key (model_type_of_board_id) references model_type_of_board (id);
