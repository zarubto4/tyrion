# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table b_program (
  b_program_id              varchar(255) not null,
  name                      varchar(255),
  program_description       TEXT,
  last_update               timestamp,
  date_of_create            timestamp,
  project_id                varchar(255),
  azure_package_link        varchar(255),
  azure_storage_link        varchar(255),
  constraint pk_b_program primary key (b_program_id))
;

create table b_program_cloud (
  id                        varchar(255) not null,
  blocko_server_name        varchar(255),
  blocko_instance_name      varchar(255),
  vrs_obj_id                varchar(255),
  running_from              timestamp,
  constraint uq_b_program_cloud_vrs_obj_id unique (vrs_obj_id),
  constraint pk_b_program_cloud primary key (id))
;

create table b_program_homer (
  id                        varchar(255) not null,
  vrs_obj_id                varchar(255),
  BProgramHomer_id          varchar(255),
  running_from              timestamp,
  constraint uq_b_program_homer_vrs_obj_id unique (vrs_obj_id),
  constraint uq_b_program_homer_BProgramHomer unique (BProgramHomer_id),
  constraint pk_b_program_homer primary key (id))
;

create table blocko_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  general_description       TEXT,
  author_id                 varchar(255),
  type_of_block_id          varchar(255),
  constraint pk_blocko_block primary key (id))
;

create table blocko_block_version (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  date_of_create            timestamp,
  design_json               TEXT,
  logic_json                TEXT,
  blocko_block_id           varchar(255),
  constraint pk_blocko_block_version primary key (id))
;

create table board (
  id                        varchar(255) not null,
  personal_description      TEXT,
  type_of_board_id          varchar(255),
  is_active                 boolean,
  constraint pk_board primary key (id))
;

create table c_program (
  id                        varchar(255) not null,
  program_name              varchar(255),
  program_description       TEXT,
  project_id                varchar(255),
  azure_package_link        varchar(255),
  azure_storage_link        varchar(255),
  date_of_create            timestamp,
  constraint pk_c_program primary key (id))
;

create table file_record (
  id                        varchar(255) not null,
  file_name                 varchar(255),
  version_object_id         varchar(255),
  constraint pk_file_record primary key (id))
;

create table floating_person_token (
  connection_id             varchar(255) not null,
  auth_token                varchar(255),
  person_id                 varchar(255),
  created                   timestamp,
  access_age                timestamp,
  user_agent                varchar(255),
  provider_user_id          varchar(255),
  provider_key              TEXT,
  type_of_connection        varchar(255),
  return_url                varchar(255),
  social_token_verified     boolean,
  notification_subscriber   boolean,
  constraint pk_floating_person_token primary key (connection_id))
;

create table grid_terminal (
  terminal_id               varchar(255) not null,
  user_agent                varchar(255),
  device_type               varchar(255),
  device_name               varchar(255),
  date_of_create            timestamp,
  date_of_last_update       timestamp,
  ws_permission             boolean,
  m_program_access          boolean,
  up_to_date                boolean,
  constraint pk_grid_terminal primary key (terminal_id))
;

create table hash_tag (
  post_hash_tag_id          varchar(255) not null,
  constraint pk_hash_tag primary key (post_hash_tag_id))
;

create table home (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_home primary key (id))
;

create table homer (
  homer_id                  varchar(255) not null,
  type_of_device            varchar(255),
  version                   varchar(255),
  project_id                varchar(255),
  constraint pk_homer primary key (homer_id))
;

create table library_group (
  id                        varchar(255) not null,
  group_name                varchar(255),
  description               TEXT,
  azure_package_link        varchar(255),
  azure_storage_link        varchar(255),
  constraint pk_library_group primary key (id))
;

create table linked_post (
  link_id                   varchar(255) not null,
  author_id                 varchar(255),
  answer_post_id            varchar(255),
  question_post_id          varchar(255),
  constraint pk_linked_post primary key (link_id))
;

create table m_program (
  id                        varchar(255) not null,
  program_name              varchar(255),
  program_description       TEXT,
  program_in_string         TEXT,
  height_lock               boolean,
  width_lock                boolean,
  date_of_create            timestamp,
  last_update               timestamp,
  qr_token                  varchar(255),
  m_project_object_id       varchar(255),
  screen_size_type_object_id varchar(255),
  constraint pk_m_program primary key (id))
;

create table m_project (
  id                        varchar(255) not null,
  program_name              varchar(255),
  program_description       TEXT,
  date_of_create            timestamp,
  project_id                varchar(255),
  b_program_id              varchar(255),
  vrs_obj_id                varchar(255),
  auto_incrementing         boolean,
  constraint uq_m_project_b_program_id unique (b_program_id),
  constraint uq_m_project_vrs_obj_id unique (vrs_obj_id),
  constraint pk_m_project primary key (id))
;

create table person (
  id                        varchar(255) not null,
  mail                      varchar(255),
  nick_name                 varchar(255),
  first_name                varchar(255),
  middle_name               varchar(255),
  last_name                 varchar(255),
  first_title               varchar(255),
  last_title                varchar(255),
  date_of_birth             timestamp,
  mail_validated            boolean,
  sha_password              bytea,
  constraint uq_person_mail unique (mail),
  constraint uq_person_nick_name unique (nick_name),
  constraint pk_person primary key (id))
;

create table person_permission (
  value                     varchar(255) not null,
  description               varchar(255),
  constraint pk_person_permission primary key (value))
;

create table post (
  post_id                   varchar(255) not null,
  name                      varchar(255),
  likes                     integer,
  date_of_create            timestamp,
  deleted                   boolean,
  updated                   boolean,
  views                     integer,
  text_of_post              TEXT,
  post_parent_comment_post_id varchar(255),
  post_parent_answer_post_id varchar(255),
  type_id                   varchar(255),
  author_id                 varchar(255),
  constraint pk_post primary key (post_id))
;

create table processor (
  id                        varchar(255) not null,
  processor_name            varchar(255),
  description               TEXT,
  processor_code            varchar(255),
  speed                     integer,
  constraint pk_processor primary key (id))
;

create table producer (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  constraint pk_producer primary key (id))
;

create table project (
  id                        varchar(255) not null,
  project_name              varchar(255),
  project_description       varchar(255),
  constraint pk_project primary key (id))
;

create table property_of_post (
  property_of_post_id       varchar(255) not null,
  constraint pk_property_of_post primary key (property_of_post_id))
;

create table screen_size_type (
  id                        varchar(255) not null,
  name                      varchar(255),
  portrait_height           integer,
  portrait_width            integer,
  portrait_square_height    integer,
  portrait_square_width     integer,
  portrait_min_screens      integer,
  portrait_max_screens      integer,
  landscape_height          integer,
  landscape_width           integer,
  landscape_square_height   integer,
  landscape_square_width    integer,
  landscape_min_screens     integer,
  landscape_max_screens     integer,
  height_lock               boolean,
  width_lock                boolean,
  touch_screen              boolean,
  project_id                varchar(255),
  constraint pk_screen_size_type primary key (id))
;

create table security_role (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_security_role primary key (id))
;

create table single_library (
  id                        varchar(255) not null,
  description               TEXT,
  library_name              varchar(255),
  azure_package_link        varchar(255),
  azure_storage_link        varchar(255),
  constraint pk_single_library primary key (id))
;

create table type_of_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  general_description       TEXT,
  project_id                varchar(255),
  constraint pk_type_of_block primary key (id))
;

create table type_of_board (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  producer_id               varchar(255),
  processor_id              varchar(255),
  constraint pk_type_of_board primary key (id))
;

create table type_of_confirms (
  id                        varchar(255) not null,
  type                      varchar(255),
  color                     varchar(255),
  size                      integer,
  constraint pk_type_of_confirms primary key (id))
;

create table type_of_post (
  id                        varchar(255) not null,
  type                      varchar(255),
  constraint pk_type_of_post primary key (id))
;

create table validation_token (
  person_email              varchar(255) not null,
  auth_token                varchar(255),
  constraint pk_validation_token primary key (person_email))
;

create table version_object (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       TEXT,
  azure_link_version        integer,
  date_of_create            timestamp,
  library_group_id          varchar(255),
  single_library_id         varchar(255),
  c_program_id              varchar(255),
  b_program_b_program_id    varchar(255),
  constraint pk_version_object primary key (id))
;


create table board_project (
  board_id                       varchar(255) not null,
  project_id                     varchar(255) not null,
  constraint pk_board_project primary key (board_id, project_id))
;

create table hash_tag_post (
  hash_tag_post_hash_tag_id      varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_hash_tag_post primary key (hash_tag_post_hash_tag_id, post_post_id))
;

create table library_group_processor (
  library_group_id               varchar(255) not null,
  processor_id                   varchar(255) not null,
  constraint pk_library_group_processor primary key (library_group_id, processor_id))
;

create table person_project (
  person_id                      varchar(255) not null,
  project_id                     varchar(255) not null,
  constraint pk_person_project primary key (person_id, project_id))
;

create table person_post (
  person_id                      varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_person_post primary key (person_id, post_post_id))
;

create table person_security_role (
  person_id                      varchar(255) not null,
  security_role_id               varchar(255) not null,
  constraint pk_person_security_role primary key (person_id, security_role_id))
;

create table person_person_permission (
  person_id                      varchar(255) not null,
  person_permission_value        varchar(255) not null,
  constraint pk_person_person_permission primary key (person_id, person_permission_value))
;

create table property_of_post_post (
  property_of_post_property_of_post_id varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_property_of_post_post primary key (property_of_post_property_of_post_id, post_post_id))
;

create table security_role_person_permission (
  security_role_id               varchar(255) not null,
  person_permission_value        varchar(255) not null,
  constraint pk_security_role_person_permission primary key (security_role_id, person_permission_value))
;

create table single_library_processor (
  single_library_id              varchar(255) not null,
  processor_id                   varchar(255) not null,
  constraint pk_single_library_processor primary key (single_library_id, processor_id))
;

create table type_of_confirms_post (
  type_of_confirms_id            varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_type_of_confirms_post primary key (type_of_confirms_id, post_post_id))
;
create sequence b_program_seq;

create sequence b_program_cloud_seq;

create sequence b_program_homer_seq;

create sequence blocko_block_seq;

create sequence blocko_block_version_seq;

create sequence board_seq;

create sequence c_program_seq;

create sequence file_record_seq;

create sequence floating_person_token_seq;

create sequence library_group_seq;

create sequence linked_post_seq;

create sequence m_program_seq;

create sequence m_project_seq;

create sequence person_seq;

create sequence post_seq;

create sequence processor_seq;

create sequence producer_seq;

create sequence project_seq;

create sequence screen_size_type_seq;

create sequence security_role_seq;

create sequence single_library_seq;

create sequence type_of_block_seq;

create sequence type_of_board_seq;

create sequence type_of_confirms_seq;

create sequence type_of_post_seq;

create sequence version_object_seq;

alter table b_program add constraint fk_b_program_project_1 foreign key (project_id) references project (id);
create index ix_b_program_project_1 on b_program (project_id);
alter table b_program_cloud add constraint fk_b_program_cloud_version_obj_2 foreign key (vrs_obj_id) references version_object (id);
create index ix_b_program_cloud_version_obj_2 on b_program_cloud (vrs_obj_id);
alter table b_program_homer add constraint fk_b_program_homer_version_obj_3 foreign key (vrs_obj_id) references version_object (id);
create index ix_b_program_homer_version_obj_3 on b_program_homer (vrs_obj_id);
alter table b_program_homer add constraint fk_b_program_homer_homer_4 foreign key (BProgramHomer_id) references homer (homer_id);
create index ix_b_program_homer_homer_4 on b_program_homer (BProgramHomer_id);
alter table blocko_block add constraint fk_blocko_block_author_5 foreign key (author_id) references person (id);
create index ix_blocko_block_author_5 on blocko_block (author_id);
alter table blocko_block add constraint fk_blocko_block_type_of_block_6 foreign key (type_of_block_id) references type_of_block (id);
create index ix_blocko_block_type_of_block_6 on blocko_block (type_of_block_id);
alter table blocko_block_version add constraint fk_blocko_block_version_blocko_7 foreign key (blocko_block_id) references blocko_block (id);
create index ix_blocko_block_version_blocko_7 on blocko_block_version (blocko_block_id);
alter table board add constraint fk_board_type_of_board_8 foreign key (type_of_board_id) references type_of_board (id);
create index ix_board_type_of_board_8 on board (type_of_board_id);
alter table c_program add constraint fk_c_program_project_9 foreign key (project_id) references project (id);
create index ix_c_program_project_9 on c_program (project_id);
alter table file_record add constraint fk_file_record_version_object_10 foreign key (version_object_id) references version_object (id);
create index ix_file_record_version_object_10 on file_record (version_object_id);
alter table floating_person_token add constraint fk_floating_person_token_pers_11 foreign key (person_id) references person (id);
create index ix_floating_person_token_pers_11 on floating_person_token (person_id);
alter table homer add constraint fk_homer_project_12 foreign key (project_id) references project (id);
create index ix_homer_project_12 on homer (project_id);
alter table linked_post add constraint fk_linked_post_author_13 foreign key (author_id) references person (id);
create index ix_linked_post_author_13 on linked_post (author_id);
alter table linked_post add constraint fk_linked_post_answer_14 foreign key (answer_post_id) references post (post_id);
create index ix_linked_post_answer_14 on linked_post (answer_post_id);
alter table linked_post add constraint fk_linked_post_question_15 foreign key (question_post_id) references post (post_id);
create index ix_linked_post_question_15 on linked_post (question_post_id);
alter table m_program add constraint fk_m_program_m_project_object_16 foreign key (m_project_object_id) references m_project (id);
create index ix_m_program_m_project_object_16 on m_program (m_project_object_id);
alter table m_program add constraint fk_m_program_screen_size_type_17 foreign key (screen_size_type_object_id) references screen_size_type (id);
create index ix_m_program_screen_size_type_17 on m_program (screen_size_type_object_id);
alter table m_project add constraint fk_m_project_project_18 foreign key (project_id) references project (id);
create index ix_m_project_project_18 on m_project (project_id);
alter table m_project add constraint fk_m_project_b_program_19 foreign key (b_program_id) references b_program (b_program_id);
create index ix_m_project_b_program_19 on m_project (b_program_id);
alter table m_project add constraint fk_m_project_b_program_versio_20 foreign key (vrs_obj_id) references version_object (id);
create index ix_m_project_b_program_versio_20 on m_project (vrs_obj_id);
alter table post add constraint fk_post_postParentComment_21 foreign key (post_parent_comment_post_id) references post (post_id);
create index ix_post_postParentComment_21 on post (post_parent_comment_post_id);
alter table post add constraint fk_post_postParentAnswer_22 foreign key (post_parent_answer_post_id) references post (post_id);
create index ix_post_postParentAnswer_22 on post (post_parent_answer_post_id);
alter table post add constraint fk_post_type_23 foreign key (type_id) references type_of_post (id);
create index ix_post_type_23 on post (type_id);
alter table post add constraint fk_post_author_24 foreign key (author_id) references person (id);
create index ix_post_author_24 on post (author_id);
alter table screen_size_type add constraint fk_screen_size_type_project_25 foreign key (project_id) references project (id);
create index ix_screen_size_type_project_25 on screen_size_type (project_id);
alter table type_of_block add constraint fk_type_of_block_project_26 foreign key (project_id) references project (id);
create index ix_type_of_block_project_26 on type_of_block (project_id);
alter table type_of_board add constraint fk_type_of_board_producer_27 foreign key (producer_id) references producer (id);
create index ix_type_of_board_producer_27 on type_of_board (producer_id);
alter table type_of_board add constraint fk_type_of_board_processor_28 foreign key (processor_id) references processor (id);
create index ix_type_of_board_processor_28 on type_of_board (processor_id);
alter table version_object add constraint fk_version_object_libraryGrou_29 foreign key (library_group_id) references library_group (id);
create index ix_version_object_libraryGrou_29 on version_object (library_group_id);
alter table version_object add constraint fk_version_object_singleLibra_30 foreign key (single_library_id) references single_library (id);
create index ix_version_object_singleLibra_30 on version_object (single_library_id);
alter table version_object add constraint fk_version_object_c_program_31 foreign key (c_program_id) references c_program (id);
create index ix_version_object_c_program_31 on version_object (c_program_id);
alter table version_object add constraint fk_version_object_b_program_32 foreign key (b_program_b_program_id) references b_program (b_program_id);
create index ix_version_object_b_program_32 on version_object (b_program_b_program_id);



alter table board_project add constraint fk_board_project_board_01 foreign key (board_id) references board (id);

alter table board_project add constraint fk_board_project_project_02 foreign key (project_id) references project (id);

alter table hash_tag_post add constraint fk_hash_tag_post_hash_tag_01 foreign key (hash_tag_post_hash_tag_id) references hash_tag (post_hash_tag_id);

alter table hash_tag_post add constraint fk_hash_tag_post_post_02 foreign key (post_post_id) references post (post_id);

alter table library_group_processor add constraint fk_library_group_processor_li_01 foreign key (library_group_id) references library_group (id);

alter table library_group_processor add constraint fk_library_group_processor_pr_02 foreign key (processor_id) references processor (id);

alter table person_project add constraint fk_person_project_person_01 foreign key (person_id) references person (id);

alter table person_project add constraint fk_person_project_project_02 foreign key (project_id) references project (id);

alter table person_post add constraint fk_person_post_person_01 foreign key (person_id) references person (id);

alter table person_post add constraint fk_person_post_post_02 foreign key (post_post_id) references post (post_id);

alter table person_security_role add constraint fk_person_security_role_perso_01 foreign key (person_id) references person (id);

alter table person_security_role add constraint fk_person_security_role_secur_02 foreign key (security_role_id) references security_role (id);

alter table person_person_permission add constraint fk_person_person_permission_p_01 foreign key (person_id) references person (id);

alter table person_person_permission add constraint fk_person_person_permission_p_02 foreign key (person_permission_value) references person_permission (value);

alter table property_of_post_post add constraint fk_property_of_post_post_prop_01 foreign key (property_of_post_property_of_post_id) references property_of_post (property_of_post_id);

alter table property_of_post_post add constraint fk_property_of_post_post_post_02 foreign key (post_post_id) references post (post_id);

alter table security_role_person_permission add constraint fk_security_role_person_permi_01 foreign key (security_role_id) references security_role (id);

alter table security_role_person_permission add constraint fk_security_role_person_permi_02 foreign key (person_permission_value) references person_permission (value);

alter table single_library_processor add constraint fk_single_library_processor_s_01 foreign key (single_library_id) references single_library (id);

alter table single_library_processor add constraint fk_single_library_processor_p_02 foreign key (processor_id) references processor (id);

alter table type_of_confirms_post add constraint fk_type_of_confirms_post_type_01 foreign key (type_of_confirms_id) references type_of_confirms (id);

alter table type_of_confirms_post add constraint fk_type_of_confirms_post_post_02 foreign key (post_post_id) references post (post_id);

# --- !Downs

drop table if exists b_program cascade;

drop table if exists b_program_cloud cascade;

drop table if exists b_program_homer cascade;

drop table if exists blocko_block cascade;

drop table if exists blocko_block_version cascade;

drop table if exists board cascade;

drop table if exists board_project cascade;

drop table if exists c_program cascade;

drop table if exists file_record cascade;

drop table if exists floating_person_token cascade;

drop table if exists grid_terminal cascade;

drop table if exists hash_tag cascade;

drop table if exists hash_tag_post cascade;

drop table if exists home cascade;

drop table if exists homer cascade;

drop table if exists library_group cascade;

drop table if exists library_group_processor cascade;

drop table if exists linked_post cascade;

drop table if exists m_program cascade;

drop table if exists m_project cascade;

drop table if exists person cascade;

drop table if exists person_project cascade;

drop table if exists person_post cascade;

drop table if exists person_security_role cascade;

drop table if exists person_person_permission cascade;

drop table if exists person_permission cascade;

drop table if exists security_role_person_permission cascade;

drop table if exists post cascade;

drop table if exists property_of_post_post cascade;

drop table if exists type_of_confirms_post cascade;

drop table if exists processor cascade;

drop table if exists single_library_processor cascade;

drop table if exists producer cascade;

drop table if exists project cascade;

drop table if exists property_of_post cascade;

drop table if exists screen_size_type cascade;

drop table if exists security_role cascade;

drop table if exists single_library cascade;

drop table if exists type_of_block cascade;

drop table if exists type_of_board cascade;

drop table if exists type_of_confirms cascade;

drop table if exists type_of_post cascade;

drop table if exists validation_token cascade;

drop table if exists version_object cascade;

drop sequence if exists b_program_seq;

drop sequence if exists b_program_cloud_seq;

drop sequence if exists b_program_homer_seq;

drop sequence if exists blocko_block_seq;

drop sequence if exists blocko_block_version_seq;

drop sequence if exists board_seq;

drop sequence if exists c_program_seq;

drop sequence if exists file_record_seq;

drop sequence if exists floating_person_token_seq;

drop sequence if exists library_group_seq;

drop sequence if exists linked_post_seq;

drop sequence if exists m_program_seq;

drop sequence if exists m_project_seq;

drop sequence if exists person_seq;

drop sequence if exists post_seq;

drop sequence if exists processor_seq;

drop sequence if exists producer_seq;

drop sequence if exists project_seq;

drop sequence if exists screen_size_type_seq;

drop sequence if exists security_role_seq;

drop sequence if exists single_library_seq;

drop sequence if exists type_of_block_seq;

drop sequence if exists type_of_board_seq;

drop sequence if exists type_of_confirms_seq;

drop sequence if exists type_of_post_seq;

drop sequence if exists version_object_seq;

