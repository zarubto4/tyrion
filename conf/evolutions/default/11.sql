
# --- !Ups

alter table model_board
  add column IF NOT EXISTS developer_kit BOOLEAN;

update model_board set developer_kit = false where developer_kit isnull;

drop table if exists model_c_program_library_version cascade;

# --- !Downs

alter table model_board
  drop column if exists developer_kit;

create table model_c_program_library_version (
  library_version_id             varchar(255) not null,
  c_program_version_id           varchar(255) not null,
  constraint pk_model_c_program_library_version primary key (library_version_id, c_program_version_id))
;

alter table model_c_program_library_version add constraint fk_model_c_program_library_ve_01 foreign key (library_version_id) references model_version_object (id);

alter table model_c_program_library_version add constraint fk_model_c_program_library_ve_02 foreign key (c_program_version_id) references model_version_object (id);
