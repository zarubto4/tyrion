
# --- !Ups

alter table model_type_of_board
  drop COLUMN IF EXISTS azure_picture_link;

alter table model_producer
  add column IF NOT EXISTS removed_by_user BOOLEAN DEFAULT FALSE;

alter table model_processor
  add column IF NOT EXISTS removed_by_user BOOLEAN DEFAULT FALSE;

alter table model_cprogram
  add column IF NOT EXISTS type_of_board_test_id varchar(255),
  add constraint uq_model_cprogram_type_of_board_test_27 unique (type_of_board_test_id);

alter table model_cprogram
  add constraint  fk_model_cprogram_type_of_board_test_27 foreign key (type_of_board_test_id) references model_type_of_board (id);

create index ix_model_cprogram_type_of_board_test_27 on model_cprogram (type_of_board_test_id);

alter table model_permission
  rename column value to permission_key;

alter table model_person_model_permission
  rename column model_permission_value to model_permission_permission_key;
alter table model_person_model_permission
  DROP CONSTRAINT IF EXISTS pk_model_person_model_permission;
alter table model_person_model_permission
  add constraint pk_model_person_model_permission primary key (model_person_id, model_permission_permission_key);

alter table model_security_role_model_permis
  rename column model_permission_value to model_permission_permission_key;
alter table model_security_role_model_permis
  DROP CONSTRAINT IF EXISTS pk_model_security_role_model_permis;
alter table model_security_role_model_permis
  add constraint pk_model_security_role_model_permis primary key (model_security_role_id, model_permission_permission_key);

alter table model_person_model_permission DROP CONSTRAINT IF EXISTS fk_model_person_model_permiss_02;
alter table model_security_role_model_permis DROP CONSTRAINT IF EXISTS fk_model_security_role_model__02;

alter table model_person_model_permission add constraint fk_model_person_model_permiss_02 foreign key (model_permission_permission_key) references model_permission (permission_key);
alter table model_security_role_model_permis add constraint fk_model_security_role_model__02 foreign key (model_permission_permission_key) references model_permission (permission_key);

# --- !Downs


alter table model_type_of_board
  add COLUMN IF NOT EXISTS azure_picture_link varchar(255);

alter table model_producer
  DROP column removed_by_user CASCADE;

alter table model_processor
  DROP column removed_by_user CASCADE;

alter table model_cprogram
  DROP column IF EXISTS type_of_board_test_id CASCADE;

alter table model_cprogram
  DROP CONSTRAINT IF EXISTS uq_model_cprogram_type_of_board_test_27;

alter table model_cprogram
  DROP CONSTRAINT IF EXISTS fk_model_cprogram_type_of_board_test_27;

alter table model_permission
  rename column permission_key to value;

alter table model_person_model_permission
  rename column model_permission_permission_key to model_permission_value;

alter table model_person_model_permission
  DROP CONSTRAINT IF EXISTS pk_model_person_model_permission;

alter table model_person_model_permission
  add constraint pk_model_person_model_permission primary key (model_person_id, model_permission_value);

alter table model_security_role_model_permis
  rename column model_permission_permission_key to model_permission_value;


alter table model_person_model_permission DROP CONSTRAINT IF EXISTS fk_model_person_model_permiss_02;
alter table model_security_role_model_permis DROP CONSTRAINT IF EXISTS fk_model_security_role_model__02;