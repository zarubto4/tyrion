# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table actualization_procedure (
  id                        varchar(255) not null,
  state                     integer,
  project_id                varchar(255),
  b_program_version_procedure_id varchar(255),
  date_of_create            timestamp,
  date_of_finish            timestamp,
  constraint ck_actualization_procedure_state check (state in (0,1,2,3,4)),
  constraint pk_actualization_procedure primary key (id))
;

create table b_pair (
  id                        varchar(255) not null,
  c_program_version_id      varchar(255),
  board_id                  varchar(255),
  device_board_pair_id      bigint,
  main_board_pair_id        bigint,
  constraint uq_b_pair_main_board_pair_id unique (main_board_pair_id),
  constraint pk_b_pair primary key (id))
;

create table b_program (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  last_update               timestamp,
  date_of_create            timestamp,
  project_id                varchar(255),
  azure_b_program_link      varchar(255),
  constraint pk_b_program primary key (id))
;

create table b_program_hw_group (
  id                        bigint not null,
  constraint pk_b_program_hw_group primary key (id))
;

create table blocko_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  general_description       TEXT,
  author_id                 varchar(255),
  type_of_block_id          varchar(255),
  producer_id               varchar(255),
  constraint pk_blocko_block primary key (id))
;

create table blocko_block_version (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  approval_state            integer,
  date_of_create            timestamp,
  design_json               TEXT,
  logic_json                TEXT,
  blocko_block_id           varchar(255),
  constraint ck_blocko_block_version_approval_state check (approval_state in (0,1,2,3)),
  constraint pk_blocko_block_version primary key (id))
;

create table board (
  id                        varchar(255) not null,
  ethernet_mac_address      varchar(255),
  wifi_mac_address          varchar(255),
  personal_description      TEXT,
  type_of_board_id          varchar(255),
  is_active                 boolean,
  backup_mode               boolean,
  date_of_create            timestamp,
  project_id                varchar(255),
  actual_c_program_version_id varchar(255),
  alternative_program_name  varchar(255),
  actual_boot_loader_id     varchar(255),
  latest_know_server_id     varchar(255),
  private_homer_servers_id  varchar(255),
  private_instance_id       varchar(255),
  constraint uq_board_private_instance_id unique (private_instance_id),
  constraint pk_board primary key (id))
;

create table boot_loader (
  id                        varchar(255) not null,
  date_of_create            timestamp,
  name                      varchar(255),
  description               TEXT,
  version_identificator     varchar(255),
  changing_note             TEXT,
  type_of_board_id          varchar(255),
  main_type_of_board_id     varchar(255),
  azure_product_link        varchar(255),
  constraint uq_boot_loader_main_type_of_boar unique (main_type_of_board_id),
  constraint pk_boot_loader primary key (id))
;

create table c_compilation (
  id                        varchar(255) not null,
  date_of_create            timestamp,
  c_compilation_version     varchar(255),
  virtual_input_output      TEXT,
  c_comp_build_url          TEXT,
  file_id                   varchar(255),
  firmware_version_core     varchar(255),
  firmware_version_mbed     varchar(255),
  firmware_version_lib      varchar(255),
  firmware_build_id         varchar(255),
  firmware_build_datetime   varchar(255),
  constraint uq_c_compilation_c_compilation_v unique (c_compilation_version),
  constraint uq_c_compilation_file_id unique (file_id),
  constraint pk_c_compilation primary key (id))
;

create table c_program (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  type_of_board_id          varchar(255),
  date_of_create            timestamp,
  first_default_version_object_id varchar(255),
  defaul_program_type_of_board_id varchar(255),
  default_main_version_id   varchar(255),
  azure_c_program_link      varchar(255),
  constraint uq_c_program_defaul_program_type unique (defaul_program_type_of_board_id),
  constraint uq_c_program_default_main_versio unique (default_main_version_id),
  constraint pk_c_program primary key (id))
;

create table c_program_update_plan (
  id                        varchar(255) not null,
  actualization_procedure_id varchar(255),
  board_id                  varchar(255),
  firmware_type             varchar(10),
  c_program_version_for_update_id varchar(255),
  binary_file_id            varchar(255),
  state                     varchar(23),
  constraint ck_c_program_update_plan_firmware_type check (firmware_type in ('FIRMWARE','BOOTLOADER','BACKUP')),
  constraint ck_c_program_update_plan_state check (state in ('complete','canceled','in_progress','overwritten','waiting_for_device','instance_inaccessible','homer_server_is_offline','critical_error')),
  constraint pk_c_program_update_plan primary key (id))
;

create table change_property_token (
  id                        varchar(255) not null,
  person_id                 varchar(255),
  change_property_token     varchar(255),
  time_of_creation          timestamp,
  property                  varchar(255),
  value                     varchar(255),
  constraint uq_change_property_token_person_ unique (person_id),
  constraint pk_change_property_token primary key (id))
;

create table cloud_compilation_server (
  id                        varchar(255) not null,
  server_name               varchar(255),
  unique_identificator      varchar(255),
  hash_certificate          varchar(255),
  destination_address       varchar(255),
  constraint uq_cloud_compilation_server_serv unique (server_name),
  constraint uq_cloud_compilation_server_uniq unique (unique_identificator),
  constraint uq_cloud_compilation_server_dest unique (destination_address),
  constraint pk_cloud_compilation_server primary key (id))
;

create table cloud_homer_server (
  id                        varchar(255) not null,
  unique_identificator      varchar(255),
  hash_certificate          varchar(255),
  server_name               varchar(255),
  destination_address       varchar(255),
  is_private                boolean,
  constraint uq_cloud_homer_server_server_nam unique (server_name),
  constraint uq_cloud_homer_server_destinatio unique (destination_address),
  constraint pk_cloud_homer_server primary key (id))
;

create table file_record (
  id                        varchar(255) not null,
  file_name                 varchar(255),
  file_path                 varchar(255),
  person_id                 varchar(255),
  boot_loader_id            varchar(255),
  version_object_id         varchar(255),
  constraint uq_file_record_person_id unique (person_id),
  constraint uq_file_record_boot_loader_id unique (boot_loader_id),
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

create table general_tariff (
  id                        varchar(255) not null,
  tariff_name               varchar(255),
  tariff_description        varchar(255),
  identificator             varchar(255),
  company_details_required  boolean,
  required_payment_mode     boolean,
  required_payment_method   boolean,
  required_paid_that        boolean,
  number_of_free_months     integer,
  usd                       float,
  eur                       float,
  czk                       float,
  color                     varchar(255),
  bank_transfer_support     boolean,
  credit_card_support       boolean,
  mode_annually             boolean,
  mode_credit               boolean,
  free                      boolean,
  constraint uq_general_tariff_identificator unique (identificator),
  constraint pk_general_tariff primary key (id))
;

create table general_tariff_label (
  id                        varchar(255) not null,
  general_tariff_id         varchar(255),
  label                     varchar(255),
  description               varchar(255),
  icon                      varchar(255),
  constraint pk_general_tariff_label primary key (id))
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

create table homer_instance (
  id                        varchar(255) not null,
  cloud_homer_server_id     varchar(255),
  private_server_id         varchar(255),
  blocko_instance_name      varchar(255),
  vrs_obj_id                varchar(255),
  project_id                varchar(255),
  running_from              timestamp,
  constraint uq_homer_instance_private_server unique (private_server_id),
  constraint uq_homer_instance_vrs_obj_id unique (vrs_obj_id),
  constraint pk_homer_instance primary key (id))
;

create table invitation (
  id                        varchar(255) not null,
  owner_id                  varchar(255),
  project_id                varchar(255),
  mail                      varchar(255),
  date_of_creation          timestamp,
  notification_id           varchar(255),
  constraint pk_invitation primary key (id))
;

create table invoice (
  id                        bigint not null,
  facturoid_invoice_id      bigint,
  facturoid_pdf_url         varchar(255),
  invoice_number            varchar(255),
  gopay_id                  bigint,
  gopay_order_number        varchar(255),
  proforma                  boolean,
  date_of_create            timestamp,
  product_id                bigint,
  status                    varchar(14),
  method                    varchar(13),
  constraint ck_invoice_status check (status in ('paid','sent','created_waited','cancelled')),
  constraint ck_invoice_method check (method in ('bank_transfer','credit_card','free')),
  constraint pk_invoice primary key (id))
;

create table invoice_item (
  id                        bigint not null,
  invoice_id                bigint,
  name                      varchar(255),
  quantity                  bigint,
  unit_name                 varchar(255),
  unit_price                float,
  constraint pk_invoice_item primary key (id))
;

create table library_group (
  id                        varchar(255) not null,
  group_name                varchar(255),
  description               TEXT,
  product_id                bigint,
  azure_library_group_link  varchar(255),
  constraint pk_library_group primary key (id))
;

create table linked_post (
  link_id                   varchar(255) not null,
  author_id                 varchar(255),
  answer_id                 varchar(255),
  question_id               varchar(255),
  constraint pk_linked_post primary key (link_id))
;

create table loggy_error (
  id                        varchar(255) not null,
  summary                   TEXT,
  description               TEXT,
  youtrack_url              varchar(255),
  constraint pk_loggy_error primary key (id))
;

create table m_program (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  height_lock               boolean,
  width_lock                boolean,
  qr_token                  varchar(255),
  m_project_id              varchar(255),
  screen_size_type_id       varchar(255),
  date_of_create            timestamp,
  azure_m_program_link      varchar(255),
  constraint pk_m_program primary key (id))
;

create table m_project (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  date_of_create            timestamp,
  project_id                varchar(255),
  auto_incrementing         boolean,
  azure_m_project_link      varchar(255),
  constraint pk_m_project primary key (id))
;

create table notification (
  id                        varchar(255) not null,
  notification_level        varchar(8),
  notification_importance   varchar(6),
  content_string            TEXT,
  confirmation_required     boolean,
  confirmed                 boolean,
  was_read                  boolean,
  created                   timestamp,
  person_id                 varchar(255),
  constraint ck_notification_notification_level check (notification_level in ('info','success','warning','error','question')),
  constraint ck_notification_notification_importance check (notification_importance in ('low','normal','high')),
  constraint pk_notification primary key (id))
;

create table password_recovery_token (
  id                        varchar(255) not null,
  person_id                 varchar(255),
  password_recovery_token   varchar(255),
  time_of_creation          timestamp,
  constraint uq_password_recovery_token_perso unique (person_id),
  constraint pk_password_recovery_token primary key (id))
;

create table payment_details (
  id                        bigint not null,
  person_id                 varchar(255),
  productidpaymentdetails   bigint,
  company_account           boolean,
  company_name              varchar(255),
  company_authorized_email  varchar(255),
  company_authorized_phone  varchar(255),
  company_invoice_email     varchar(255),
  company_web               varchar(255),
  company_registration_no   varchar(255),
  company_vat_number        varchar(255),
  street                    varchar(255),
  street_number             varchar(255),
  city                      varchar(255),
  zip_code                  varchar(255),
  country                   varchar(255),
  constraint uq_payment_details_productidpaym unique (productidpaymentdetails),
  constraint pk_payment_details primary key (id))
;

create table person (
  id                        varchar(255) not null,
  mail                      varchar(255),
  nick_name                 varchar(255),
  full_name                 varchar(255),
  azure_picture_link        varchar(255),
  freeze_account            boolean,
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
  id                        varchar(255) not null,
  name                      varchar(255),
  likes                     integer,
  date_of_create            timestamp,
  deleted                   boolean,
  updated                   boolean,
  views                     integer,
  text_of_post              TEXT,
  post_parent_comment_id    varchar(255),
  post_parent_answer_id     varchar(255),
  type_id                   varchar(255),
  author_id                 varchar(255),
  constraint pk_post primary key (id))
;

create table private_homer_server (
  id                        varchar(255) not null,
  mac_address               varchar(255),
  type_of_device            varchar(255),
  version                   varchar(255),
  project_id                varchar(255),
  private_server_id         varchar(255),
  constraint uq_private_homer_server_private_ unique (private_server_id),
  constraint pk_private_homer_server primary key (id))
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

create table product (
  id                        bigint not null,
  product_individual_name   varchar(255),
  general_tariff_id         varchar(255),
  mode                      varchar(10),
  method                    varchar(13),
  fakturoid_subject_id      varchar(255),
  gopay_id                  bigint,
  active                    boolean,
  monthly_day_period        integer,
  monthly_year_period       integer,
  paid_until_the_day        timestamp,
  on_demand_active          boolean,
  remaining_credit          float,
  currency                  varchar(3),
  azure_product_link        varchar(255),
  constraint ck_product_mode check (mode in ('per_credit','monthly','annual','free')),
  constraint ck_product_method check (method in ('bank_transfer','credit_card','free')),
  constraint ck_product_currency check (currency in ('eur','czk','usd')),
  constraint pk_product primary key (id))
;

create table project (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  product_id                bigint,
  blob_project_link         varchar(255),
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
  constraint uq_screen_size_type_name unique (name),
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
  product_id                bigint,
  azure_single_library_link varchar(255),
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
  compiler_target_name      varchar(255),
  revision                  varchar(255),
  description               TEXT,
  producer_id               varchar(255),
  processor_id              varchar(255),
  connectible_to_internet   boolean,
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
  author_id                 varchar(255),
  approval_state            integer,
  public_version            boolean,
  date_of_create            timestamp,
  library_group_id          varchar(255),
  single_library_id         varchar(255),
  c_program_id              varchar(255),
  compilation_in_progress   boolean,
  compilable                boolean,
  b_program_id              varchar(255),
  m_program_id              varchar(255),
  blob_version_link         varchar(255),
  constraint ck_version_object_approval_state check (approval_state in (0,1,2,3)),
  constraint pk_version_object primary key (id))
;


create table hash_tag_post (
  hash_tag_post_hash_tag_id      varchar(255) not null,
  post_id                        varchar(255) not null,
  constraint pk_hash_tag_post primary key (hash_tag_post_hash_tag_id, post_id))
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
  post_id                        varchar(255) not null,
  constraint pk_person_post primary key (person_id, post_id))
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
  post_id                        varchar(255) not null,
  constraint pk_property_of_post_post primary key (property_of_post_property_of_post_id, post_id))
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
  post_id                        varchar(255) not null,
  constraint pk_type_of_confirms_post primary key (type_of_confirms_id, post_id))
;

create table version_object_program_hw_group (
  version_object_id              varchar(255) not null,
  b_program_hw_group_id          bigint not null,
  constraint pk_version_object_program_hw_group primary key (version_object_id, b_program_hw_group_id))
;

create table version_object_project (
  version_object_id              varchar(255) not null,
  m_project_id                   varchar(255) not null,
  constraint pk_version_object_project primary key (version_object_id, m_project_id))
;
create sequence actualization_procedure_seq;

create sequence b_pair_seq;

create sequence b_program_seq;

create sequence b_program_hw_group_seq;

create sequence blocko_block_seq;

create sequence blocko_block_version_seq;

create sequence boot_loader_seq;

create sequence c_compilation_seq;

create sequence c_program_seq;

create sequence c_program_update_plan_seq;

create sequence change_property_token_seq;

create sequence cloud_compilation_server_seq;

create sequence cloud_homer_server_seq;

create sequence file_record_seq;

create sequence floating_person_token_seq;

create sequence general_tariff_seq;

create sequence general_tariff_label_seq;

create sequence homer_instance_seq;

create sequence invitation_seq;

create sequence invoice_seq;

create sequence invoice_item_seq;

create sequence library_group_seq;

create sequence linked_post_seq;

create sequence m_program_seq;

create sequence m_project_seq;

create sequence notification_seq;

create sequence password_recovery_token_seq;

create sequence payment_details_seq;

create sequence person_seq;

create sequence post_seq;

create sequence private_homer_server_seq;

create sequence processor_seq;

create sequence producer_seq;

create sequence product_seq;

create sequence project_seq;

create sequence screen_size_type_seq;

create sequence security_role_seq;

create sequence single_library_seq;

create sequence type_of_block_seq;

create sequence type_of_board_seq;

create sequence type_of_confirms_seq;

create sequence type_of_post_seq;

create sequence version_object_seq;

alter table actualization_procedure add constraint fk_actualization_procedure_pro_1 foreign key (project_id) references project (id);
create index ix_actualization_procedure_pro_1 on actualization_procedure (project_id);
alter table actualization_procedure add constraint fk_actualization_procedure_b_p_2 foreign key (b_program_version_procedure_id) references version_object (id);
create index ix_actualization_procedure_b_p_2 on actualization_procedure (b_program_version_procedure_id);
alter table b_pair add constraint fk_b_pair_c_program_version_3 foreign key (c_program_version_id) references version_object (id);
create index ix_b_pair_c_program_version_3 on b_pair (c_program_version_id);
alter table b_pair add constraint fk_b_pair_board_4 foreign key (board_id) references board (id);
create index ix_b_pair_board_4 on b_pair (board_id);
alter table b_pair add constraint fk_b_pair_device_board_pair_5 foreign key (device_board_pair_id) references b_program_hw_group (id);
create index ix_b_pair_device_board_pair_5 on b_pair (device_board_pair_id);
alter table b_pair add constraint fk_b_pair_main_board_pair_6 foreign key (main_board_pair_id) references b_program_hw_group (id);
create index ix_b_pair_main_board_pair_6 on b_pair (main_board_pair_id);
alter table b_program add constraint fk_b_program_project_7 foreign key (project_id) references project (id);
create index ix_b_program_project_7 on b_program (project_id);
alter table blocko_block add constraint fk_blocko_block_author_8 foreign key (author_id) references person (id);
create index ix_blocko_block_author_8 on blocko_block (author_id);
alter table blocko_block add constraint fk_blocko_block_type_of_block_9 foreign key (type_of_block_id) references type_of_block (id);
create index ix_blocko_block_type_of_block_9 on blocko_block (type_of_block_id);
alter table blocko_block add constraint fk_blocko_block_producer_10 foreign key (producer_id) references producer (id);
create index ix_blocko_block_producer_10 on blocko_block (producer_id);
alter table blocko_block_version add constraint fk_blocko_block_version_block_11 foreign key (blocko_block_id) references blocko_block (id);
create index ix_blocko_block_version_block_11 on blocko_block_version (blocko_block_id);
alter table board add constraint fk_board_type_of_board_12 foreign key (type_of_board_id) references type_of_board (id);
create index ix_board_type_of_board_12 on board (type_of_board_id);
alter table board add constraint fk_board_project_13 foreign key (project_id) references project (id);
create index ix_board_project_13 on board (project_id);
alter table board add constraint fk_board_actual_c_program_ver_14 foreign key (actual_c_program_version_id) references version_object (id);
create index ix_board_actual_c_program_ver_14 on board (actual_c_program_version_id);
alter table board add constraint fk_board_actual_boot_loader_15 foreign key (actual_boot_loader_id) references boot_loader (id);
create index ix_board_actual_boot_loader_15 on board (actual_boot_loader_id);
alter table board add constraint fk_board_latest_know_server_16 foreign key (latest_know_server_id) references cloud_homer_server (id);
create index ix_board_latest_know_server_16 on board (latest_know_server_id);
alter table board add constraint fk_board_private_homer_server_17 foreign key (private_homer_servers_id) references private_homer_server (id);
create index ix_board_private_homer_server_17 on board (private_homer_servers_id);
alter table board add constraint fk_board_private_instance_18 foreign key (private_instance_id) references homer_instance (id);
create index ix_board_private_instance_18 on board (private_instance_id);
alter table boot_loader add constraint fk_boot_loader_type_of_board_19 foreign key (type_of_board_id) references type_of_board (id);
create index ix_boot_loader_type_of_board_19 on boot_loader (type_of_board_id);
alter table boot_loader add constraint fk_boot_loader_main_type_of_b_20 foreign key (main_type_of_board_id) references type_of_board (id);
create index ix_boot_loader_main_type_of_b_20 on boot_loader (main_type_of_board_id);
alter table c_compilation add constraint fk_c_compilation_version_obje_21 foreign key (c_compilation_version) references version_object (id);
create index ix_c_compilation_version_obje_21 on c_compilation (c_compilation_version);
alter table c_compilation add constraint fk_c_compilation_bin_compilat_22 foreign key (file_id) references file_record (id);
create index ix_c_compilation_bin_compilat_22 on c_compilation (file_id);
alter table c_program add constraint fk_c_program_project_23 foreign key (project_id) references project (id);
create index ix_c_program_project_23 on c_program (project_id);
alter table c_program add constraint fk_c_program_type_of_board_24 foreign key (type_of_board_id) references type_of_board (id);
create index ix_c_program_type_of_board_24 on c_program (type_of_board_id);
alter table c_program add constraint fk_c_program_first_default_ve_25 foreign key (first_default_version_object_id) references version_object (id);
create index ix_c_program_first_default_ve_25 on c_program (first_default_version_object_id);
alter table c_program add constraint fk_c_program_defaul_program_t_26 foreign key (defaul_program_type_of_board_id) references type_of_board (id);
create index ix_c_program_defaul_program_t_26 on c_program (defaul_program_type_of_board_id);
alter table c_program add constraint fk_c_program_default_main_ver_27 foreign key (default_main_version_id) references version_object (id);
create index ix_c_program_default_main_ver_27 on c_program (default_main_version_id);
alter table c_program_update_plan add constraint fk_c_program_update_plan_actu_28 foreign key (actualization_procedure_id) references actualization_procedure (id);
create index ix_c_program_update_plan_actu_28 on c_program_update_plan (actualization_procedure_id);
alter table c_program_update_plan add constraint fk_c_program_update_plan_boar_29 foreign key (board_id) references board (id);
create index ix_c_program_update_plan_boar_29 on c_program_update_plan (board_id);
alter table c_program_update_plan add constraint fk_c_program_update_plan_c_pr_30 foreign key (c_program_version_for_update_id) references version_object (id);
create index ix_c_program_update_plan_c_pr_30 on c_program_update_plan (c_program_version_for_update_id);
alter table c_program_update_plan add constraint fk_c_program_update_plan_bina_31 foreign key (binary_file_id) references file_record (id);
create index ix_c_program_update_plan_bina_31 on c_program_update_plan (binary_file_id);
alter table change_property_token add constraint fk_change_property_token_pers_32 foreign key (person_id) references person (id);
create index ix_change_property_token_pers_32 on change_property_token (person_id);
alter table file_record add constraint fk_file_record_person_33 foreign key (person_id) references person (id);
create index ix_file_record_person_33 on file_record (person_id);
alter table file_record add constraint fk_file_record_boot_loader_34 foreign key (boot_loader_id) references boot_loader (id);
create index ix_file_record_boot_loader_34 on file_record (boot_loader_id);
alter table file_record add constraint fk_file_record_version_object_35 foreign key (version_object_id) references version_object (id);
create index ix_file_record_version_object_35 on file_record (version_object_id);
alter table floating_person_token add constraint fk_floating_person_token_pers_36 foreign key (person_id) references person (id);
create index ix_floating_person_token_pers_36 on floating_person_token (person_id);
alter table general_tariff_label add constraint fk_general_tariff_label_gener_37 foreign key (general_tariff_id) references general_tariff (id);
create index ix_general_tariff_label_gener_37 on general_tariff_label (general_tariff_id);
alter table homer_instance add constraint fk_homer_instance_cloud_homer_38 foreign key (cloud_homer_server_id) references cloud_homer_server (id);
create index ix_homer_instance_cloud_homer_38 on homer_instance (cloud_homer_server_id);
alter table homer_instance add constraint fk_homer_instance_private_ser_39 foreign key (private_server_id) references private_homer_server (id);
create index ix_homer_instance_private_ser_39 on homer_instance (private_server_id);
alter table homer_instance add constraint fk_homer_instance_version_obj_40 foreign key (vrs_obj_id) references version_object (id);
create index ix_homer_instance_version_obj_40 on homer_instance (vrs_obj_id);
alter table homer_instance add constraint fk_homer_instance_project_41 foreign key (project_id) references project (id);
create index ix_homer_instance_project_41 on homer_instance (project_id);
alter table invitation add constraint fk_invitation_owner_42 foreign key (owner_id) references person (id);
create index ix_invitation_owner_42 on invitation (owner_id);
alter table invitation add constraint fk_invitation_project_43 foreign key (project_id) references project (id);
create index ix_invitation_project_43 on invitation (project_id);
alter table invoice add constraint fk_invoice_product_44 foreign key (product_id) references product (id);
create index ix_invoice_product_44 on invoice (product_id);
alter table invoice_item add constraint fk_invoice_item_invoice_45 foreign key (invoice_id) references invoice (id);
create index ix_invoice_item_invoice_45 on invoice_item (invoice_id);
alter table library_group add constraint fk_library_group_product_46 foreign key (product_id) references product (id);
create index ix_library_group_product_46 on library_group (product_id);
alter table linked_post add constraint fk_linked_post_author_47 foreign key (author_id) references person (id);
create index ix_linked_post_author_47 on linked_post (author_id);
alter table linked_post add constraint fk_linked_post_answer_48 foreign key (answer_id) references post (id);
create index ix_linked_post_answer_48 on linked_post (answer_id);
alter table linked_post add constraint fk_linked_post_question_49 foreign key (question_id) references post (id);
create index ix_linked_post_question_49 on linked_post (question_id);
alter table m_program add constraint fk_m_program_m_project_50 foreign key (m_project_id) references m_project (id);
create index ix_m_program_m_project_50 on m_program (m_project_id);
alter table m_program add constraint fk_m_program_screen_size_type_51 foreign key (screen_size_type_id) references screen_size_type (id);
create index ix_m_program_screen_size_type_51 on m_program (screen_size_type_id);
alter table m_project add constraint fk_m_project_project_52 foreign key (project_id) references project (id);
create index ix_m_project_project_52 on m_project (project_id);
alter table notification add constraint fk_notification_person_53 foreign key (person_id) references person (id);
create index ix_notification_person_53 on notification (person_id);
alter table password_recovery_token add constraint fk_password_recovery_token_pe_54 foreign key (person_id) references person (id);
create index ix_password_recovery_token_pe_54 on password_recovery_token (person_id);
alter table payment_details add constraint fk_payment_details_person_55 foreign key (person_id) references person (id);
create index ix_payment_details_person_55 on payment_details (person_id);
alter table payment_details add constraint fk_payment_details_product_56 foreign key (productidpaymentdetails) references product (id);
create index ix_payment_details_product_56 on payment_details (productidpaymentdetails);
alter table post add constraint fk_post_postParentComment_57 foreign key (post_parent_comment_id) references post (id);
create index ix_post_postParentComment_57 on post (post_parent_comment_id);
alter table post add constraint fk_post_postParentAnswer_58 foreign key (post_parent_answer_id) references post (id);
create index ix_post_postParentAnswer_58 on post (post_parent_answer_id);
alter table post add constraint fk_post_type_59 foreign key (type_id) references type_of_post (id);
create index ix_post_type_59 on post (type_id);
alter table post add constraint fk_post_author_60 foreign key (author_id) references person (id);
create index ix_post_author_60 on post (author_id);
alter table private_homer_server add constraint fk_private_homer_server_proje_61 foreign key (project_id) references project (id);
create index ix_private_homer_server_proje_61 on private_homer_server (project_id);
alter table private_homer_server add constraint fk_private_homer_server_b_pro_62 foreign key (private_server_id) references homer_instance (id);
create index ix_private_homer_server_b_pro_62 on private_homer_server (private_server_id);
alter table product add constraint fk_product_general_tariff_63 foreign key (general_tariff_id) references general_tariff (id);
create index ix_product_general_tariff_63 on product (general_tariff_id);
alter table project add constraint fk_project_product_64 foreign key (product_id) references product (id);
create index ix_project_product_64 on project (product_id);
alter table screen_size_type add constraint fk_screen_size_type_project_65 foreign key (project_id) references project (id);
create index ix_screen_size_type_project_65 on screen_size_type (project_id);
alter table single_library add constraint fk_single_library_product_66 foreign key (product_id) references product (id);
create index ix_single_library_product_66 on single_library (product_id);
alter table type_of_block add constraint fk_type_of_block_project_67 foreign key (project_id) references project (id);
create index ix_type_of_block_project_67 on type_of_block (project_id);
alter table type_of_board add constraint fk_type_of_board_producer_68 foreign key (producer_id) references producer (id);
create index ix_type_of_board_producer_68 on type_of_board (producer_id);
alter table type_of_board add constraint fk_type_of_board_processor_69 foreign key (processor_id) references processor (id);
create index ix_type_of_board_processor_69 on type_of_board (processor_id);
alter table version_object add constraint fk_version_object_author_70 foreign key (author_id) references person (id);
create index ix_version_object_author_70 on version_object (author_id);
alter table version_object add constraint fk_version_object_library_gro_71 foreign key (library_group_id) references library_group (id);
create index ix_version_object_library_gro_71 on version_object (library_group_id);
alter table version_object add constraint fk_version_object_single_libr_72 foreign key (single_library_id) references single_library (id);
create index ix_version_object_single_libr_72 on version_object (single_library_id);
alter table version_object add constraint fk_version_object_c_program_73 foreign key (c_program_id) references c_program (id);
create index ix_version_object_c_program_73 on version_object (c_program_id);
alter table version_object add constraint fk_version_object_b_program_74 foreign key (b_program_id) references b_program (id);
create index ix_version_object_b_program_74 on version_object (b_program_id);
alter table version_object add constraint fk_version_object_m_program_75 foreign key (m_program_id) references m_program (id);
create index ix_version_object_m_program_75 on version_object (m_program_id);



alter table hash_tag_post add constraint fk_hash_tag_post_hash_tag_01 foreign key (hash_tag_post_hash_tag_id) references hash_tag (post_hash_tag_id);

alter table hash_tag_post add constraint fk_hash_tag_post_post_02 foreign key (post_id) references post (id);

alter table library_group_processor add constraint fk_library_group_processor_li_01 foreign key (library_group_id) references library_group (id);

alter table library_group_processor add constraint fk_library_group_processor_pr_02 foreign key (processor_id) references processor (id);

alter table person_project add constraint fk_person_project_person_01 foreign key (person_id) references person (id);

alter table person_project add constraint fk_person_project_project_02 foreign key (project_id) references project (id);

alter table person_post add constraint fk_person_post_person_01 foreign key (person_id) references person (id);

alter table person_post add constraint fk_person_post_post_02 foreign key (post_id) references post (id);

alter table person_security_role add constraint fk_person_security_role_perso_01 foreign key (person_id) references person (id);

alter table person_security_role add constraint fk_person_security_role_secur_02 foreign key (security_role_id) references security_role (id);

alter table person_person_permission add constraint fk_person_person_permission_p_01 foreign key (person_id) references person (id);

alter table person_person_permission add constraint fk_person_person_permission_p_02 foreign key (person_permission_value) references person_permission (value);

alter table property_of_post_post add constraint fk_property_of_post_post_prop_01 foreign key (property_of_post_property_of_post_id) references property_of_post (property_of_post_id);

alter table property_of_post_post add constraint fk_property_of_post_post_post_02 foreign key (post_id) references post (id);

alter table security_role_person_permission add constraint fk_security_role_person_permi_01 foreign key (security_role_id) references security_role (id);

alter table security_role_person_permission add constraint fk_security_role_person_permi_02 foreign key (person_permission_value) references person_permission (value);

alter table single_library_processor add constraint fk_single_library_processor_s_01 foreign key (single_library_id) references single_library (id);

alter table single_library_processor add constraint fk_single_library_processor_p_02 foreign key (processor_id) references processor (id);

alter table type_of_confirms_post add constraint fk_type_of_confirms_post_type_01 foreign key (type_of_confirms_id) references type_of_confirms (id);

alter table type_of_confirms_post add constraint fk_type_of_confirms_post_post_02 foreign key (post_id) references post (id);

alter table version_object_program_hw_group add constraint fk_version_object_program_hw__01 foreign key (version_object_id) references version_object (id);

alter table version_object_program_hw_group add constraint fk_version_object_program_hw__02 foreign key (b_program_hw_group_id) references b_program_hw_group (id);

alter table version_object_project add constraint fk_version_object_project_ver_01 foreign key (version_object_id) references version_object (id);

alter table version_object_project add constraint fk_version_object_project_m_p_02 foreign key (m_project_id) references m_project (id);

# --- !Downs

drop table if exists actualization_procedure cascade;

drop table if exists b_pair cascade;

drop table if exists b_program cascade;

drop table if exists b_program_hw_group cascade;

drop table if exists version_object_program_hw_group cascade;

drop table if exists blocko_block cascade;

drop table if exists blocko_block_version cascade;

drop table if exists board cascade;

drop table if exists boot_loader cascade;

drop table if exists c_compilation cascade;

drop table if exists c_program cascade;

drop table if exists c_program_update_plan cascade;

drop table if exists change_property_token cascade;

drop table if exists cloud_compilation_server cascade;

drop table if exists cloud_homer_server cascade;

drop table if exists file_record cascade;

drop table if exists floating_person_token cascade;

drop table if exists general_tariff cascade;

drop table if exists general_tariff_label cascade;

drop table if exists grid_terminal cascade;

drop table if exists hash_tag cascade;

drop table if exists hash_tag_post cascade;

drop table if exists homer_instance cascade;

drop table if exists invitation cascade;

drop table if exists invoice cascade;

drop table if exists invoice_item cascade;

drop table if exists library_group cascade;

drop table if exists library_group_processor cascade;

drop table if exists linked_post cascade;

drop table if exists loggy_error cascade;

drop table if exists m_program cascade;

drop table if exists m_project cascade;

drop table if exists version_object_project cascade;

drop table if exists notification cascade;

drop table if exists password_recovery_token cascade;

drop table if exists payment_details cascade;

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

drop table if exists private_homer_server cascade;

drop table if exists processor cascade;

drop table if exists single_library_processor cascade;

drop table if exists producer cascade;

drop table if exists product cascade;

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

drop sequence if exists actualization_procedure_seq;

drop sequence if exists b_pair_seq;

drop sequence if exists b_program_seq;

drop sequence if exists b_program_hw_group_seq;

drop sequence if exists blocko_block_seq;

drop sequence if exists blocko_block_version_seq;

drop sequence if exists boot_loader_seq;

drop sequence if exists c_compilation_seq;

drop sequence if exists c_program_seq;

drop sequence if exists c_program_update_plan_seq;

drop sequence if exists change_property_token_seq;

drop sequence if exists cloud_compilation_server_seq;

drop sequence if exists cloud_homer_server_seq;

drop sequence if exists file_record_seq;

drop sequence if exists floating_person_token_seq;

drop sequence if exists general_tariff_seq;

drop sequence if exists general_tariff_label_seq;

drop sequence if exists homer_instance_seq;

drop sequence if exists invitation_seq;

drop sequence if exists invoice_seq;

drop sequence if exists invoice_item_seq;

drop sequence if exists library_group_seq;

drop sequence if exists linked_post_seq;

drop sequence if exists m_program_seq;

drop sequence if exists m_project_seq;

drop sequence if exists notification_seq;

drop sequence if exists password_recovery_token_seq;

drop sequence if exists payment_details_seq;

drop sequence if exists person_seq;

drop sequence if exists post_seq;

drop sequence if exists private_homer_server_seq;

drop sequence if exists processor_seq;

drop sequence if exists producer_seq;

drop sequence if exists product_seq;

drop sequence if exists project_seq;

drop sequence if exists screen_size_type_seq;

drop sequence if exists security_role_seq;

drop sequence if exists single_library_seq;

drop sequence if exists type_of_block_seq;

drop sequence if exists type_of_board_seq;

drop sequence if exists type_of_confirms_seq;

drop sequence if exists type_of_post_seq;

drop sequence if exists version_object_seq;

