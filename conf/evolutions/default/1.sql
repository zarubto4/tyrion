# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table blocko_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  author_mail               varchar(255),
  general_description       TEXT,
  constraint pk_blocko_block primary key (id))
;

create table blocko_content_block (
  id                        varchar(255) not null,
  version_description       varchar(255),
  version                   float,
  date_of_create            timestamp,
  design_json               TEXT,
  logic_json                TEXT,
  blocko_block_id           varchar(255),
  constraint pk_blocko_content_block primary key (id))
;

create table board (
  id                        varchar(255) not null,
  general_description       TEXT,
  user_description          TEXT,
  project_project_id        varchar(255),
  type_of_board_id          varchar(255),
  constraint pk_board primary key (id))
;

create table confirm_type_of_post (
  id                        varchar(255) not null,
  constraint pk_confirm_type_of_post primary key (id))
;

create table for_upload_program (
  id                        varchar(255) not null,
  homer_homer_id            varchar(255),
  program_program_id        varchar(255),
  when_date                 timestamp,
  until_date                timestamp,
  constraint pk_for_upload_program primary key (id))
;

create table group_with_permissions (
  group_id                  varchar(255) not null,
  group_name                varchar(255),
  description               varchar(255),
  constraint uq_group_with_permissions_group_ unique (group_name),
  constraint pk_group_with_permissions primary key (group_id))
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
  project_project_id        varchar(255),
  constraint pk_homer primary key (homer_id))
;

create table homer_program (
  program_id                varchar(255) not null,
  program_name              varchar(255),
  program_description       varchar(255),
  program_in_string         TEXT,
  date_of_create            timestamp,
  project_project_id        varchar(255),
  constraint pk_homer_program primary key (program_id))
;

create table library_group (
  id                        varchar(255) not null,
  group_name                varchar(255),
  description               TEXT,
  azure_package_link        varchar(255),
  azure_storage_link        varchar(255),
  azure_primary_url         varchar(255),
  azure_secondary_url       varchar(255),
  constraint pk_library_group primary key (id))
;

create table library_record (
  id                        varchar(255) not null,
  filename                  varchar(255),
  constraint pk_library_record primary key (id))
;

create table linked_post (
  link_id                   varchar(255) not null,
  author_mail               varchar(255),
  answer_post_id            varchar(255),
  question_post_id          varchar(255),
  constraint pk_linked_post primary key (link_id))
;

create table permission_key (
  id                        varchar(255) not null,
  key                       varchar(255),
  comment                   varchar(255),
  constraint pk_permission_key primary key (id))
;

create table person (
  mail                      varchar(255) not null,
  password                  varchar(255),
  first_name                varchar(255),
  middle_name               varchar(255),
  last_name                 varchar(255),
  first_title               varchar(255),
  last_title                varchar(255),
  date_of_birth             timestamp,
  auth_token                varchar(255),
  sha_password              bytea not null,
  constraint pk_person primary key (mail))
;

create table post (
  post_id                   varchar(255) not null,
  name                      varchar(255),
  type_id                   varchar(255),
  views                     integer,
  likes                     integer,
  date_of_create            timestamp,
  deleted                   boolean,
  author_mail               varchar(255),
  text_of_post              TEXT,
  post_parent_comment_post_id varchar(255),
  post_parent_answer_post_id varchar(255),
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
  project_id                varchar(255) not null,
  project_name              varchar(255),
  project_description       varchar(255),
  constraint pk_project primary key (project_id))
;

create table property_of_post (
  property_of_post_id       varchar(255) not null,
  constraint pk_property_of_post primary key (property_of_post_id))
;

create table single_library (
  id                        varchar(255) not null,
  description               TEXT,
  library_name              varchar(255),
  azure_blob_link           varchar(255),
  constraint pk_single_library primary key (id))
;

create table type_of_board (
  id                        varchar(255) not null,
  description               TEXT,
  producer_id               varchar(255),
  processor_id              varchar(255),
  constraint pk_type_of_board primary key (id))
;

create table type_of_post (
  id                        varchar(255) not null,
  type                      varchar(255),
  constraint pk_type_of_post primary key (id))
;

create table version (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       TEXT,
  date_of_create            timestamp,
  azure_link_version        float,
  library_group_id          varchar(255),
  single_library_id         varchar(255),
  constraint pk_version primary key (id))
;


create table confirm_type_of_post_post (
  confirm_type_of_post_id        varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_confirm_type_of_post_post primary key (confirm_type_of_post_id, post_post_id))
;

create table hash_tag_post (
  hash_tag_post_hash_tag_id      varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_hash_tag_post primary key (hash_tag_post_hash_tag_id, post_post_id))
;

create table homer_program_homer (
  homer_program_program_id       varchar(255) not null,
  homer_homer_id                 varchar(255) not null,
  constraint pk_homer_program_homer primary key (homer_program_program_id, homer_homer_id))
;

create table library_group_processor (
  library_group_id               varchar(255) not null,
  processor_id                   varchar(255) not null,
  constraint pk_library_group_processor primary key (library_group_id, processor_id))
;

create table permission_key_group_with_permis (
  permission_key_id              varchar(255) not null,
  group_with_permissions_group_id varchar(255) not null,
  constraint pk_permission_key_group_with_permis primary key (permission_key_id, group_with_permissions_group_id))
;

create table permission_key_person (
  permission_key_id              varchar(255) not null,
  person_mail                    varchar(255) not null,
  constraint pk_permission_key_person primary key (permission_key_id, person_mail))
;

create table person_project (
  person_mail                    varchar(255) not null,
  project_project_id             varchar(255) not null,
  constraint pk_person_project primary key (person_mail, project_project_id))
;

create table person_post (
  person_mail                    varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_person_post primary key (person_mail, post_post_id))
;

create table person_group_with_permissions (
  person_mail                    varchar(255) not null,
  group_with_permissions_group_id varchar(255) not null,
  constraint pk_person_group_with_permissions primary key (person_mail, group_with_permissions_group_id))
;

create table property_of_post_post (
  property_of_post_property_of_post_id varchar(255) not null,
  post_post_id                   varchar(255) not null,
  constraint pk_property_of_post_post primary key (property_of_post_property_of_post_id, post_post_id))
;

create table type_of_board_library_record (
  type_of_board_id               varchar(255) not null,
  library_record_id              varchar(255) not null,
  constraint pk_type_of_board_library_record primary key (type_of_board_id, library_record_id))
;

create table type_of_board_library_group (
  type_of_board_id               varchar(255) not null,
  library_group_id               varchar(255) not null,
  constraint pk_type_of_board_library_group primary key (type_of_board_id, library_group_id))
;

create table version_library_record (
  version_id                     varchar(255) not null,
  library_record_id              varchar(255) not null,
  constraint pk_version_library_record primary key (version_id, library_record_id))
;
create sequence blocko_block_seq;

create sequence blocko_content_block_seq;

create sequence for_upload_program_seq;

create sequence group_with_permissions_seq;

create sequence homer_program_seq;

create sequence library_group_seq;

create sequence library_record_seq;

create sequence linked_post_seq;

create sequence permission_key_seq;

create sequence post_seq;

create sequence project_seq;

create sequence single_library_seq;

create sequence type_of_post_seq;

create sequence version_seq;

alter table blocko_block add constraint fk_blocko_block_author_1 foreign key (author_mail) references person (mail);
create index ix_blocko_block_author_1 on blocko_block (author_mail);
alter table blocko_content_block add constraint fk_blocko_content_block_blocko_2 foreign key (blocko_block_id) references blocko_block (id);
create index ix_blocko_content_block_blocko_2 on blocko_content_block (blocko_block_id);
alter table board add constraint fk_board_project_3 foreign key (project_project_id) references project (project_id);
create index ix_board_project_3 on board (project_project_id);
alter table board add constraint fk_board_typeOfBoard_4 foreign key (type_of_board_id) references type_of_board (id);
create index ix_board_typeOfBoard_4 on board (type_of_board_id);
alter table for_upload_program add constraint fk_for_upload_program_homer_5 foreign key (homer_homer_id) references homer (homer_id);
create index ix_for_upload_program_homer_5 on for_upload_program (homer_homer_id);
alter table for_upload_program add constraint fk_for_upload_program_program_6 foreign key (program_program_id) references homer_program (program_id);
create index ix_for_upload_program_program_6 on for_upload_program (program_program_id);
alter table homer add constraint fk_homer_project_7 foreign key (project_project_id) references project (project_id);
create index ix_homer_project_7 on homer (project_project_id);
alter table homer_program add constraint fk_homer_program_project_8 foreign key (project_project_id) references project (project_id);
create index ix_homer_program_project_8 on homer_program (project_project_id);
alter table linked_post add constraint fk_linked_post_author_9 foreign key (author_mail) references person (mail);
create index ix_linked_post_author_9 on linked_post (author_mail);
alter table linked_post add constraint fk_linked_post_answer_10 foreign key (answer_post_id) references post (post_id);
create index ix_linked_post_answer_10 on linked_post (answer_post_id);
alter table linked_post add constraint fk_linked_post_question_11 foreign key (question_post_id) references post (post_id);
create index ix_linked_post_question_11 on linked_post (question_post_id);
alter table post add constraint fk_post_type_12 foreign key (type_id) references type_of_post (id);
create index ix_post_type_12 on post (type_id);
alter table post add constraint fk_post_author_13 foreign key (author_mail) references person (mail);
create index ix_post_author_13 on post (author_mail);
alter table post add constraint fk_post_postParentComment_14 foreign key (post_parent_comment_post_id) references post (post_id);
create index ix_post_postParentComment_14 on post (post_parent_comment_post_id);
alter table post add constraint fk_post_postParentAnswer_15 foreign key (post_parent_answer_post_id) references post (post_id);
create index ix_post_postParentAnswer_15 on post (post_parent_answer_post_id);
alter table type_of_board add constraint fk_type_of_board_producer_16 foreign key (producer_id) references producer (id);
create index ix_type_of_board_producer_16 on type_of_board (producer_id);
alter table type_of_board add constraint fk_type_of_board_processor_17 foreign key (processor_id) references processor (id);
create index ix_type_of_board_processor_17 on type_of_board (processor_id);
alter table version add constraint fk_version_libraryGroup_18 foreign key (library_group_id) references library_group (id);
create index ix_version_libraryGroup_18 on version (library_group_id);
alter table version add constraint fk_version_singleLibrary_19 foreign key (single_library_id) references single_library (id);
create index ix_version_singleLibrary_19 on version (single_library_id);



alter table confirm_type_of_post_post add constraint fk_confirm_type_of_post_post__01 foreign key (confirm_type_of_post_id) references confirm_type_of_post (id);

alter table confirm_type_of_post_post add constraint fk_confirm_type_of_post_post__02 foreign key (post_post_id) references post (post_id);

alter table hash_tag_post add constraint fk_hash_tag_post_hash_tag_01 foreign key (hash_tag_post_hash_tag_id) references hash_tag (post_hash_tag_id);

alter table hash_tag_post add constraint fk_hash_tag_post_post_02 foreign key (post_post_id) references post (post_id);

alter table homer_program_homer add constraint fk_homer_program_homer_homer__01 foreign key (homer_program_program_id) references homer_program (program_id);

alter table homer_program_homer add constraint fk_homer_program_homer_homer_02 foreign key (homer_homer_id) references homer (homer_id);

alter table library_group_processor add constraint fk_library_group_processor_li_01 foreign key (library_group_id) references library_group (id);

alter table library_group_processor add constraint fk_library_group_processor_pr_02 foreign key (processor_id) references processor (id);

alter table permission_key_group_with_permis add constraint fk_permission_key_group_with__01 foreign key (permission_key_id) references permission_key (id);

alter table permission_key_group_with_permis add constraint fk_permission_key_group_with__02 foreign key (group_with_permissions_group_id) references group_with_permissions (group_id);

alter table permission_key_person add constraint fk_permission_key_person_perm_01 foreign key (permission_key_id) references permission_key (id);

alter table permission_key_person add constraint fk_permission_key_person_pers_02 foreign key (person_mail) references person (mail);

alter table person_project add constraint fk_person_project_person_01 foreign key (person_mail) references person (mail);

alter table person_project add constraint fk_person_project_project_02 foreign key (project_project_id) references project (project_id);

alter table person_post add constraint fk_person_post_person_01 foreign key (person_mail) references person (mail);

alter table person_post add constraint fk_person_post_post_02 foreign key (post_post_id) references post (post_id);

alter table person_group_with_permissions add constraint fk_person_group_with_permissi_01 foreign key (person_mail) references person (mail);

alter table person_group_with_permissions add constraint fk_person_group_with_permissi_02 foreign key (group_with_permissions_group_id) references group_with_permissions (group_id);

alter table property_of_post_post add constraint fk_property_of_post_post_prop_01 foreign key (property_of_post_property_of_post_id) references property_of_post (property_of_post_id);

alter table property_of_post_post add constraint fk_property_of_post_post_post_02 foreign key (post_post_id) references post (post_id);

alter table type_of_board_library_record add constraint fk_type_of_board_library_reco_01 foreign key (type_of_board_id) references type_of_board (id);

alter table type_of_board_library_record add constraint fk_type_of_board_library_reco_02 foreign key (library_record_id) references library_record (id);

alter table type_of_board_library_group add constraint fk_type_of_board_library_grou_01 foreign key (type_of_board_id) references type_of_board (id);

alter table type_of_board_library_group add constraint fk_type_of_board_library_grou_02 foreign key (library_group_id) references library_group (id);

alter table version_library_record add constraint fk_version_library_record_ver_01 foreign key (version_id) references version (id);

alter table version_library_record add constraint fk_version_library_record_lib_02 foreign key (library_record_id) references library_record (id);

# --- !Downs

drop table if exists blocko_block cascade;

drop table if exists blocko_content_block cascade;

drop table if exists board cascade;

drop table if exists confirm_type_of_post cascade;

drop table if exists confirm_type_of_post_post cascade;

drop table if exists for_upload_program cascade;

drop table if exists group_with_permissions cascade;

drop table if exists person_group_with_permissions cascade;

drop table if exists permission_key_group_with_permis cascade;

drop table if exists hash_tag cascade;

drop table if exists hash_tag_post cascade;

drop table if exists home cascade;

drop table if exists homer cascade;

drop table if exists homer_program_homer cascade;

drop table if exists homer_program cascade;

drop table if exists library_group cascade;

drop table if exists library_group_processor cascade;

drop table if exists library_record cascade;

drop table if exists version_library_record cascade;

drop table if exists linked_post cascade;

drop table if exists permission_key cascade;

drop table if exists permission_key_person cascade;

drop table if exists person cascade;

drop table if exists person_project cascade;

drop table if exists person_post cascade;

drop table if exists post cascade;

drop table if exists property_of_post_post cascade;

drop table if exists processor cascade;

drop table if exists producer cascade;

drop table if exists project cascade;

drop table if exists property_of_post cascade;

drop table if exists single_library cascade;

drop table if exists type_of_board cascade;

drop table if exists type_of_board_library_record cascade;

drop table if exists type_of_board_library_group cascade;

drop table if exists type_of_post cascade;

drop table if exists version cascade;

drop sequence if exists blocko_block_seq;

drop sequence if exists blocko_content_block_seq;

drop sequence if exists for_upload_program_seq;

drop sequence if exists group_with_permissions_seq;

drop sequence if exists homer_program_seq;

drop sequence if exists library_group_seq;

drop sequence if exists library_record_seq;

drop sequence if exists linked_post_seq;

drop sequence if exists permission_key_seq;

drop sequence if exists post_seq;

drop sequence if exists project_seq;

drop sequence if exists single_library_seq;

drop sequence if exists type_of_post_seq;

drop sequence if exists version_seq;

