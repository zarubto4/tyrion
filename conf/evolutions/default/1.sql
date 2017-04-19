# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table model_actualization_procedure (
  id                        varchar(255) not null,
  state                     varchar(19),
  homer_instance_record_id  varchar(255),
  date_of_create            timestamp,
  date_of_planing           timestamp,
  date_of_finish            timestamp,
  type_of_update            varchar(41),
  constraint ck_model_actualization_procedure_state check (state in ('complete_with_error','canceled','in_progress','successful_complete','complete','not_start_yet')),
  constraint ck_model_actualization_procedure_type_of_update check (type_of_update in ('AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE','AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE','MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME','MANUALLY_BY_USER','MANUALLY_BY_USER_BLOCKO_GROUP')),
  constraint pk_model_actualization_procedure primary key (id))
;

create table model_bpair (
  id                        varchar(40) not null,
  c_program_version_id      varchar(255),
  board_id                  varchar(255),
  device_board_pair_id      varchar(40),
  main_board_pair_id        varchar(40),
  constraint uq_model_bpair_main_board_pair_i unique (main_board_pair_id),
  constraint pk_model_bpair primary key (id))
;

create table model_bprogram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  instance_blocko_instance_name varchar(255),
  last_update               timestamp,
  date_of_create            timestamp,
  project_id                varchar(255),
  removed_by_user           boolean,
  azure_b_program_link      varchar(255),
  constraint uq_model_bprogram_instance_block unique (instance_blocko_instance_name),
  constraint pk_model_bprogram primary key (id))
;

create table model_bprogram_hw_group (
  id                        varchar(40) not null,
  constraint pk_model_bprogram_hw_group primary key (id))
;

create table model_blocko_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  author_id                 varchar(255),
  type_of_block_id          varchar(255),
  producer_id               varchar(255),
  constraint pk_model_blocko_block primary key (id))
;

create table model_blocko_block_version (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  approval_state            varchar(11),
  author_id                 varchar(255),
  date_of_create            timestamp,
  design_json               TEXT,
  logic_json                TEXT,
  blocko_block_id           varchar(255),
  constraint ck_model_blocko_block_version_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint pk_model_blocko_block_version primary key (id))
;

create table model_board (
  id                        varchar(255) not null,
  hash_for_adding           varchar(255),
  wifi_mac_address          varchar(255),
  mac_address               varchar(255),
  generation_description    varchar(255),
  personal_description      TEXT,
  type_of_board_id          varchar(255),
  is_active                 boolean,
  backup_mode               boolean,
  date_of_create            timestamp,
  project_id                varchar(255),
  actual_c_program_version_id varchar(255),
  actual_backup_c_program_version_id varchar(255),
  actual_boot_loader_id     varchar(255),
  virtual_instance_under_project_blocko_instance_name varchar(255),
  connected_server_unique_identificator varchar(255),
  constraint pk_model_board primary key (id))
;

create table model_boot_loader (
  id                        varchar(255) not null,
  date_of_create            timestamp,
  name                      varchar(255),
  description               TEXT,
  version_identificator     varchar(255),
  changing_note             TEXT,
  type_of_board_id          varchar(255),
  main_type_of_board_id     varchar(255),
  azure_product_link        varchar(255),
  constraint uq_model_boot_loader_main_type_o unique (main_type_of_board_id),
  constraint pk_model_boot_loader primary key (id))
;

create table model_ccompilation (
  id                        varchar(255) not null,
  date_of_create            timestamp,
  c_compilation_version     varchar(255),
  status                    varchar(34),
  virtual_input_output      TEXT,
  c_comp_build_url          TEXT,
  bin_compilation_file_id   varchar(255),
  firmware_version_core     varchar(255),
  firmware_version_mbed     varchar(255),
  firmware_version_lib      varchar(255),
  firmware_build_id         varchar(255),
  firmware_build_datetime   varchar(255),
  constraint ck_model_ccompilation_status check (status in ('file_with_code_not_found','json_code_is_broken','successfully_compiled_and_restored','compilation_in_progress','compilation_server_error','server_was_offline','successfully_compiled_not_restored','compiled_with_code_errors','undefined')),
  constraint uq_model_ccompilation_c_compilat unique (c_compilation_version),
  constraint uq_model_ccompilation_bin_compil unique (bin_compilation_file_id),
  constraint pk_model_ccompilation primary key (id))
;

create table model_cprogram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  type_of_board_id          varchar(255),
  date_of_create            timestamp,
  removed_by_user           boolean,
  type_of_board_default_id  varchar(255),
  example_library_id        varchar(255),
  azure_c_program_link      varchar(255),
  constraint uq_model_cprogram_type_of_board_ unique (type_of_board_default_id),
  constraint pk_model_cprogram primary key (id))
;

create table model_cprogram_update_plan (
  id                        varchar(255) not null,
  actualization_procedure_id varchar(255),
  date_of_create            timestamp,
  date_of_finish            timestamp,
  board_id                  varchar(255),
  firmware_type             varchar(10),
  c_program_version_for_update_id varchar(255),
  bootloader_id             varchar(255),
  binary_file_id            varchar(255),
  state                     varchar(23),
  error                     varchar(255),
  error_code                integer,
  constraint ck_model_cprogram_update_plan_firmware_type check (firmware_type in ('BACKUP','FIRMWARE','BOOTLOADER','WIFI')),
  constraint ck_model_cprogram_update_plan_state check (state in ('canceled','in_progress','waiting_for_device','overwritten','bin_file_not_found','not_updated','homer_server_is_offline','complete','instance_inaccessible','critical_error','not_start_yet')),
  constraint pk_model_cprogram_update_plan primary key (id))
;

create table model_change_property_token (
  change_property_token     varchar(255) not null,
  person_id                 varchar(255),
  time_of_creation          timestamp,
  property                  varchar(255),
  value                     varchar(255),
  constraint uq_model_change_property_token_p unique (person_id),
  constraint pk_model_change_property_token primary key (change_property_token))
;

create table CompilationServer (
  unique_identificator      varchar(255) not null,
  personal_server_name      varchar(255),
  hash_certificate          varchar(255),
  server_url                varchar(255),
  constraint uq_CompilationServer_personal_se unique (personal_server_name),
  constraint uq_CompilationServer_server_url unique (server_url),
  constraint pk_CompilationServer primary key (unique_identificator))
;

create table model_example_model_name (
  id                        varchar(255) not null,
  created                   timestamp,
  constraint pk_model_example_model_name primary key (id))
;

create table model_file_record (
  id                        varchar(255) not null,
  file_name                 varchar(255),
  file_path                 varchar(255),
  boot_loader_id            varchar(255),
  version_object_id         varchar(255),
  constraint uq_model_file_record_boot_loader unique (boot_loader_id),
  constraint pk_model_file_record primary key (id))
;

create table model_floating_person_token (
  connection_id             varchar(255) not null,
  auth_token                varchar(255),
  person_id                 varchar(255),
  created                   timestamp,
  where_logged              varchar(13),
  access_age                timestamp,
  user_agent                varchar(255),
  provider_user_id          varchar(255),
  provider_key              TEXT,
  type_of_connection        varchar(255),
  return_url                varchar(255),
  social_token_verified     boolean,
  notification_subscriber   boolean,
  constraint ck_model_floating_person_token_where_logged check (where_logged in ('E_STORE','HOMER_SERVER','BECKI_WEBSITE')),
  constraint pk_model_floating_person_token primary key (connection_id))
;

create table model_grid_terminal (
  terminal_token            varchar(255) not null,
  user_agent                varchar(255),
  device_type               varchar(255),
  device_name               varchar(255),
  person_id                 varchar(255),
  date_of_create            timestamp,
  date_of_last_update       timestamp,
  ws_permission             boolean,
  m_program_access          boolean,
  up_to_date                boolean,
  constraint pk_model_grid_terminal primary key (terminal_token))
;

create table model_grid_widget (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  author_id                 varchar(255),
  type_of_widget_id         varchar(255),
  producer_id               varchar(255),
  constraint pk_model_grid_widget primary key (id))
;

create table model_grid_widget_version (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  approval_state            varchar(11),
  author_id                 varchar(255),
  date_of_create            timestamp,
  design_json               TEXT,
  logic_json                TEXT,
  grid_widget_id            varchar(255),
  constraint ck_model_grid_widget_version_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint pk_model_grid_widget_version primary key (id))
;

create table model_homer_instance (
  blocko_instance_name      varchar(255) not null,
  cloud_homer_server_unique_identificator varchar(255),
  instance_type             varchar(10),
  constraint ck_model_homer_instance_instance_type check (instance_type in ('INDIVIDUAL','VIRTUAL')),
  constraint pk_model_homer_instance primary key (blocko_instance_name))
;

create table model_homer_instance_record (
  id                        varchar(255) not null,
  main_instance_history_blocko_instance_name varchar(255),
  date_of_created           timestamp,
  running_from              timestamp,
  running_to                timestamp,
  planed_when               timestamp,
  version_object_id         varchar(255),
  actual_running_instance_blocko_instance_name varchar(255),
  constraint uq_model_homer_instance_record_a unique (actual_running_instance_blocko_instance_name),
  constraint pk_model_homer_instance_record primary key (id))
;

create table model_homer_server (
  unique_identificator      varchar(255) not null,
  hash_certificate          varchar(255),
  personal_server_name      varchar(255),
  mqtt_port                 integer,
  mqtt_username             varchar(255),
  mqtt_password             varchar(255),
  grid_port                 integer,
  web_view_port             integer,
  server_remote_port        integer,
  server_url                varchar(255),
  server_type               varchar(14),
  time_stamp_configuration  timestamp,
  days_in_archive           integer,
  logging                   boolean,
  interactive               boolean,
  log_level                 varchar(5),
  constraint ck_model_homer_server_server_type check (server_type in ('main_server','test_server','private_server','backup_server','public_server')),
  constraint ck_model_homer_server_log_level check (log_level in ('warn','trace','debug','error','info')),
  constraint pk_model_homer_server primary key (unique_identificator))
;

create table model_import_library (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  long_description          TEXT,
  state                     varchar(10),
  removed                   boolean,
  azure_library_link        varchar(255),
  tag                       varchar(9),
  constraint ck_model_import_library_state check (state in ('NEW','TESTED','DEPRECATED')),
  constraint ck_model_import_library_tag check (tag in ('MATH','BLUETOOTH','AUDIO','SPI','WIFI')),
  constraint pk_model_import_library primary key (id))
;

create table model_invitation (
  id                        varchar(255) not null,
  owner_id                  varchar(255),
  project_id                varchar(255),
  mail                      varchar(255),
  date_of_creation          timestamp,
  notification_id           varchar(255),
  constraint pk_model_invitation primary key (id))
;

create table model_invoice (
  id                        varchar(255) not null,
  fakturoid_id              bigint,
  fakturoid_pdf_url         varchar(255),
  invoice_number            varchar(255),
  gopay_id                  bigint,
  gopay_order_number        varchar(255),
  gw_url                    varchar(255),
  proforma                  boolean,
  proforma_id               bigint,
  proforma_pdf_url          varchar(255),
  created                   timestamp,
  paid                      timestamp,
  overdue                   timestamp,
  product_id                varchar(255),
  status                    varchar(8),
  method                    varchar(13),
  warning                   varchar(12),
  constraint ck_model_invoice_status check (status in ('canceled','overdue','pending','paid')),
  constraint ck_model_invoice_method check (method in ('credit_card','bank_transfer','free')),
  constraint ck_model_invoice_warning check (warning in ('none','first','zero_balance','deactivation','second')),
  constraint pk_model_invoice primary key (id))
;

create table model_invoice_item (
  id                        bigint not null,
  invoice_id                varchar(255),
  name                      varchar(255),
  quantity                  bigint,
  unit_name                 varchar(255),
  unit_price                float,
  currency                  varchar(12),
  constraint ck_model_invoice_item_currency check (currency in ('eur','czk','price_in_usd')),
  constraint pk_model_invoice_item primary key (id))
;

create table model_log (
  id                        varchar(255) not null,
  name                      varchar(255),
  created                   timestamp,
  type                      varchar(255),
  file_id                   varchar(255),
  constraint uq_model_log_file_id unique (file_id),
  constraint pk_model_log primary key (id))
;

create table model_loggy_error (
  id                        varchar(255) not null,
  summary                   TEXT,
  description               TEXT,
  youtrack_url              varchar(255),
  constraint pk_model_loggy_error primary key (id))
;

create table model_mprogram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  date_of_create            timestamp,
  m_project_id              varchar(255),
  azure_m_program_link      varchar(255),
  constraint pk_model_mprogram primary key (id))
;

create table model_mproject (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  date_of_create            timestamp,
  project_id                varchar(255),
  azure_m_project_link      varchar(255),
  removed_by_user           boolean,
  constraint pk_model_mproject primary key (id))
;

create table model_mproject_program_snap_shot (
  id                        varchar(40) not null,
  m_project_id              varchar(255),
  constraint pk_model_mproject_program_snap_s primary key (id))
;

create table model_mac_address_register_record (
  uuid_request_number       varchar(255) not null,
  mac_address               varchar(255),
  date_of_create            timestamp,
  type_of_board             varchar(255),
  full_id                   varchar(255),
  bootloader_id             varchar(255),
  firmware_version_id       varchar(255),
  state                     varchar(13),
  constraint ck_model_mac_address_register_record_state check (state in ('unknown_error','in_progress','complete','broken_device')),
  constraint pk_model_mac_address_register_re primary key (uuid_request_number))
;

create table model_notification (
  id                        varchar(255) not null,
  notification_level        varchar(7),
  notification_importance   varchar(6),
  content_string            TEXT,
  buttons_string            TEXT,
  confirmation_required     boolean,
  confirmed                 boolean,
  was_read                  boolean,
  created                   timestamp,
  person_id                 varchar(255),
  constraint ck_model_notification_notification_level check (notification_level in ('success','warning','error','info')),
  constraint ck_model_notification_notification_importance check (notification_importance in ('normal','high','low')),
  constraint pk_model_notification primary key (id))
;

create table model_password_recovery_token (
  id                        varchar(255) not null,
  person_id                 varchar(255),
  password_recovery_token   varchar(255),
  time_of_creation          timestamp,
  constraint uq_model_password_recovery_token unique (person_id),
  constraint pk_model_password_recovery_token primary key (id))
;

create table model_payment_details (
  id                        bigint not null,
  person_id                 varchar(255),
  productidpaymentdetails   varchar(255),
  company_account           boolean,
  company_name              varchar(255),
  company_authorized_email  varchar(255),
  company_authorized_phone  varchar(255),
  company_web               varchar(255),
  company_registration_no   varchar(255),
  company_vat_number        varchar(255),
  full_name                 varchar(255),
  street                    varchar(255),
  street_number             varchar(255),
  city                      varchar(255),
  zip_code                  varchar(255),
  country                   varchar(255),
  invoice_email             varchar(255),
  constraint uq_model_payment_details_product unique (productidpaymentdetails),
  constraint pk_model_payment_details primary key (id))
;

create table model_permission (
  value                     varchar(255) not null,
  description               varchar(255),
  constraint pk_model_permission primary key (value))
;

create table model_person (
  id                        varchar(255) not null,
  mail                      varchar(255),
  nick_name                 varchar(255),
  full_name                 varchar(255),
  country                   varchar(255),
  gender                    varchar(255),
  azure_picture_link        varchar(255),
  picture_id                varchar(255),
  freeze_account            boolean,
  mail_validated            boolean,
  sha_password              bytea,
  constraint uq_model_person_mail unique (mail),
  constraint uq_model_person_nick_name unique (nick_name),
  constraint uq_model_person_picture_id unique (picture_id),
  constraint pk_model_person primary key (id))
;

create table model_processor (
  id                        varchar(255) not null,
  processor_name            varchar(255),
  description               TEXT,
  processor_code            varchar(255),
  speed                     integer,
  constraint pk_model_processor primary key (id))
;

create table model_producer (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  constraint pk_model_producer primary key (id))
;

create table model_product (
  id                        varchar(255) not null,
  name                      varchar(255),
  tariff_id                 varchar(255),
  mode                      varchar(10),
  method                    varchar(13),
  business_model            varchar(8),
  subscription_id           varchar(255),
  fakturoid_subject_id      varchar(255),
  gopay_id                  bigint,
  active                    boolean,
  monthly_day_period        integer,
  monthly_year_period       integer,
  created                   timestamp,
  on_demand                 boolean,
  credit                    float,
  financial_history         TEXT,
  azure_product_link        varchar(255),
  constraint ck_model_product_mode check (mode in ('per_credit','monthly','annual','free')),
  constraint ck_model_product_method check (method in ('credit_card','bank_transfer','free')),
  constraint ck_model_product_business_model check (business_model in ('saas','lifelong','fee')),
  constraint pk_model_product primary key (id))
;

create table model_product_extension (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  color                     varchar(255),
  type                      varchar(8),
  config                    varchar(255),
  order_position            integer,
  active                    boolean,
  removed                   boolean,
  created                   timestamp,
  product_id                varchar(255),
  tariff_included_id        varchar(255),
  tariff_optional_id        varchar(255),
  constraint ck_model_product_extension_type check (type in ('Project','Log','Database')),
  constraint pk_model_product_extension primary key (id))
;

create table model_project (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  private_instance_blocko_instance_name varchar(255),
  product_id                varchar(255),
  blob_project_link         varchar(255),
  constraint uq_model_project_private_instanc unique (private_instance_blocko_instance_name),
  constraint pk_model_project primary key (id))
;

create table model_project_participant (
  id                        varchar(255) not null,
  project_id                varchar(255),
  person_id                 varchar(255),
  state                     varchar(7),
  constraint ck_model_project_participant_state check (state in ('owner','member','invited','admin')),
  constraint pk_model_project_participant primary key (id))
;

create table model_request_log (
  id                        varchar(255) not null,
  request                   varchar(255),
  call_count                bigint,
  date_of_create            timestamp,
  constraint uq_model_request_log_request unique (request),
  constraint pk_model_request_log primary key (id))
;

create table model_security_role (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_model_security_role primary key (id))
;

create table model_tariff (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  identifier                varchar(255),
  active                    boolean,
  order_position            integer,
  company_details_required  boolean,
  payment_mode_required     boolean,
  payment_method_required   boolean,
  payment_required          boolean,
  credit_for_beginning      float,
  color                     varchar(255),
  bank_transfer_support     boolean,
  credit_card_support       boolean,
  mode_annually             boolean,
  mode_credit               boolean,
  free_tariff               boolean,
  constraint uq_model_tariff_identifier unique (identifier),
  constraint pk_model_tariff primary key (id))
;

create table model_tariff_label (
  id                        varchar(255) not null,
  tariff_id                 varchar(255),
  label                     varchar(255),
  description               varchar(255),
  icon                      varchar(255),
  order_position            integer,
  constraint pk_model_tariff_label primary key (id))
;

create table model_type_of_block (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  constraint pk_model_type_of_block primary key (id))
;

create table model_type_of_board (
  id                        varchar(255) not null,
  name                      varchar(255),
  compiler_target_name      varchar(255),
  revision                  varchar(255),
  azure_picture_link        varchar(255),
  description               TEXT,
  producer_id               varchar(255),
  processor_id              varchar(255),
  connectible_to_internet   boolean,
  picture_id                varchar(255),
  constraint uq_model_type_of_board_compiler_ unique (compiler_target_name),
  constraint uq_model_type_of_board_picture_i unique (picture_id),
  constraint pk_model_type_of_board primary key (id))
;

create table model_type_of_widget (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  constraint pk_model_type_of_widget primary key (id))
;

create table model_validation_token (
  person_email              varchar(255) not null,
  auth_token                varchar(255),
  created                   timestamp,
  constraint pk_model_validation_token primary key (person_email))
;

create table model_version_object (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       TEXT,
  author_id                 varchar(255),
  public_version            boolean,
  removed_by_user           boolean,
  date_of_create            timestamp,
  library_id                varchar(255),
  c_program_id              varchar(255),
  approval_state            varchar(11),
  default_program_id        varchar(255),
  b_program_id              varchar(255),
  m_program_id              varchar(255),
  m_program_virtual_input_output TEXT,
  qr_token                  varchar(255),
  blob_version_link         varchar(255),
  constraint ck_model_version_object_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint uq_model_version_object_default_ unique (default_program_id),
  constraint pk_model_version_object primary key (id))
;


create table model_import_library_model_type_ (
  model_import_library_id        varchar(255) not null,
  model_type_of_board_id         varchar(255) not null,
  constraint pk_model_import_library_model_type_ primary key (model_import_library_id, model_type_of_board_id))
;

create table b_program_version_snapshots (
  model_mproject_program_snap_shot_id varchar(40) not null,
  model_version_object_id        varchar(255) not null,
  constraint pk_b_program_version_snapshots primary key (model_mproject_program_snap_shot_id, model_version_object_id))
;

create table m_project_program_snapshots (
  model_mproject_program_snap_shot_id varchar(40) not null,
  model_version_object_id        varchar(255) not null,
  constraint pk_m_project_program_snapshots primary key (model_mproject_program_snap_shot_id, model_version_object_id))
;

create table model_person_model_security_role (
  model_person_id                varchar(255) not null,
  model_security_role_id         varchar(255) not null,
  constraint pk_model_person_model_security_role primary key (model_person_id, model_security_role_id))
;

create table model_person_model_permission (
  model_person_id                varchar(255) not null,
  model_permission_value         varchar(255) not null,
  constraint pk_model_person_model_permission primary key (model_person_id, model_permission_value))
;

create table model_security_role_model_permis (
  model_security_role_id         varchar(255) not null,
  model_permission_value         varchar(255) not null,
  constraint pk_model_security_role_model_permis primary key (model_security_role_id, model_permission_value))
;

create table model_c_program_library_version (
  library_version_id             varchar(255) not null,
  c_program_version_id           varchar(255) not null,
  constraint pk_model_c_program_library_version primary key (library_version_id, c_program_version_id))
;

create table model_version_object_model_bprog (
  model_version_object_id        varchar(255) not null,
  model_bprogram_hw_group_id     varchar(40) not null,
  constraint pk_model_version_object_model_bprog primary key (model_version_object_id, model_bprogram_hw_group_id))
;
create sequence model_invoice_item_seq;

create sequence model_payment_details_seq;

alter table model_actualization_procedure add constraint fk_model_actualization_procedu_1 foreign key (homer_instance_record_id) references model_homer_instance_record (id);
create index ix_model_actualization_procedu_1 on model_actualization_procedure (homer_instance_record_id);
alter table model_bpair add constraint fk_model_bpair_c_program_versi_2 foreign key (c_program_version_id) references model_version_object (id);
create index ix_model_bpair_c_program_versi_2 on model_bpair (c_program_version_id);
alter table model_bpair add constraint fk_model_bpair_board_3 foreign key (board_id) references model_board (id);
create index ix_model_bpair_board_3 on model_bpair (board_id);
alter table model_bpair add constraint fk_model_bpair_device_board_pa_4 foreign key (device_board_pair_id) references model_bprogram_hw_group (id);
create index ix_model_bpair_device_board_pa_4 on model_bpair (device_board_pair_id);
alter table model_bpair add constraint fk_model_bpair_main_board_pair_5 foreign key (main_board_pair_id) references model_bprogram_hw_group (id);
create index ix_model_bpair_main_board_pair_5 on model_bpair (main_board_pair_id);
alter table model_bprogram add constraint fk_model_bprogram_instance_6 foreign key (instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);
create index ix_model_bprogram_instance_6 on model_bprogram (instance_blocko_instance_name);
alter table model_bprogram add constraint fk_model_bprogram_project_7 foreign key (project_id) references model_project (id);
create index ix_model_bprogram_project_7 on model_bprogram (project_id);
alter table model_blocko_block add constraint fk_model_blocko_block_author_8 foreign key (author_id) references model_person (id);
create index ix_model_blocko_block_author_8 on model_blocko_block (author_id);
alter table model_blocko_block add constraint fk_model_blocko_block_type_of__9 foreign key (type_of_block_id) references model_type_of_block (id);
create index ix_model_blocko_block_type_of__9 on model_blocko_block (type_of_block_id);
alter table model_blocko_block add constraint fk_model_blocko_block_produce_10 foreign key (producer_id) references model_producer (id);
create index ix_model_blocko_block_produce_10 on model_blocko_block (producer_id);
alter table model_blocko_block_version add constraint fk_model_blocko_block_version_11 foreign key (author_id) references model_person (id);
create index ix_model_blocko_block_version_11 on model_blocko_block_version (author_id);
alter table model_blocko_block_version add constraint fk_model_blocko_block_version_12 foreign key (blocko_block_id) references model_blocko_block (id);
create index ix_model_blocko_block_version_12 on model_blocko_block_version (blocko_block_id);
alter table model_board add constraint fk_model_board_type_of_board_13 foreign key (type_of_board_id) references model_type_of_board (id);
create index ix_model_board_type_of_board_13 on model_board (type_of_board_id);
alter table model_board add constraint fk_model_board_project_14 foreign key (project_id) references model_project (id);
create index ix_model_board_project_14 on model_board (project_id);
alter table model_board add constraint fk_model_board_actual_c_progr_15 foreign key (actual_c_program_version_id) references model_version_object (id);
create index ix_model_board_actual_c_progr_15 on model_board (actual_c_program_version_id);
alter table model_board add constraint fk_model_board_actual_backup__16 foreign key (actual_backup_c_program_version_id) references model_version_object (id);
create index ix_model_board_actual_backup__16 on model_board (actual_backup_c_program_version_id);
alter table model_board add constraint fk_model_board_actual_boot_lo_17 foreign key (actual_boot_loader_id) references model_boot_loader (id);
create index ix_model_board_actual_boot_lo_17 on model_board (actual_boot_loader_id);
alter table model_board add constraint fk_model_board_virtual_instan_18 foreign key (virtual_instance_under_project_blocko_instance_name) references model_homer_instance (blocko_instance_name);
create index ix_model_board_virtual_instan_18 on model_board (virtual_instance_under_project_blocko_instance_name);
alter table model_board add constraint fk_model_board_connected_serv_19 foreign key (connected_server_unique_identificator) references model_homer_server (unique_identificator);
create index ix_model_board_connected_serv_19 on model_board (connected_server_unique_identificator);
alter table model_boot_loader add constraint fk_model_boot_loader_type_of__20 foreign key (type_of_board_id) references model_type_of_board (id);
create index ix_model_boot_loader_type_of__20 on model_boot_loader (type_of_board_id);
alter table model_boot_loader add constraint fk_model_boot_loader_main_typ_21 foreign key (main_type_of_board_id) references model_type_of_board (id);
create index ix_model_boot_loader_main_typ_21 on model_boot_loader (main_type_of_board_id);
alter table model_ccompilation add constraint fk_model_ccompilation_version_22 foreign key (c_compilation_version) references model_version_object (id);
create index ix_model_ccompilation_version_22 on model_ccompilation (c_compilation_version);
alter table model_ccompilation add constraint fk_model_ccompilation_bin_com_23 foreign key (bin_compilation_file_id) references model_file_record (id);
create index ix_model_ccompilation_bin_com_23 on model_ccompilation (bin_compilation_file_id);
alter table model_cprogram add constraint fk_model_cprogram_project_24 foreign key (project_id) references model_project (id);
create index ix_model_cprogram_project_24 on model_cprogram (project_id);
alter table model_cprogram add constraint fk_model_cprogram_type_of_boa_25 foreign key (type_of_board_id) references model_type_of_board (id);
create index ix_model_cprogram_type_of_boa_25 on model_cprogram (type_of_board_id);
alter table model_cprogram add constraint fk_model_cprogram_type_of_boa_26 foreign key (type_of_board_default_id) references model_type_of_board (id);
create index ix_model_cprogram_type_of_boa_26 on model_cprogram (type_of_board_default_id);
alter table model_cprogram add constraint fk_model_cprogram_example_lib_27 foreign key (example_library_id) references model_version_object (id);
create index ix_model_cprogram_example_lib_27 on model_cprogram (example_library_id);
alter table model_cprogram_update_plan add constraint fk_model_cprogram_update_plan_28 foreign key (actualization_procedure_id) references model_actualization_procedure (id);
create index ix_model_cprogram_update_plan_28 on model_cprogram_update_plan (actualization_procedure_id);
alter table model_cprogram_update_plan add constraint fk_model_cprogram_update_plan_29 foreign key (board_id) references model_board (id);
create index ix_model_cprogram_update_plan_29 on model_cprogram_update_plan (board_id);
alter table model_cprogram_update_plan add constraint fk_model_cprogram_update_plan_30 foreign key (c_program_version_for_update_id) references model_version_object (id);
create index ix_model_cprogram_update_plan_30 on model_cprogram_update_plan (c_program_version_for_update_id);
alter table model_cprogram_update_plan add constraint fk_model_cprogram_update_plan_31 foreign key (bootloader_id) references model_boot_loader (id);
create index ix_model_cprogram_update_plan_31 on model_cprogram_update_plan (bootloader_id);
alter table model_cprogram_update_plan add constraint fk_model_cprogram_update_plan_32 foreign key (binary_file_id) references model_file_record (id);
create index ix_model_cprogram_update_plan_32 on model_cprogram_update_plan (binary_file_id);
alter table model_change_property_token add constraint fk_model_change_property_toke_33 foreign key (person_id) references model_person (id);
create index ix_model_change_property_toke_33 on model_change_property_token (person_id);
alter table model_file_record add constraint fk_model_file_record_boot_loa_34 foreign key (boot_loader_id) references model_boot_loader (id);
create index ix_model_file_record_boot_loa_34 on model_file_record (boot_loader_id);
alter table model_file_record add constraint fk_model_file_record_version__35 foreign key (version_object_id) references model_version_object (id);
create index ix_model_file_record_version__35 on model_file_record (version_object_id);
alter table model_floating_person_token add constraint fk_model_floating_person_toke_36 foreign key (person_id) references model_person (id);
create index ix_model_floating_person_toke_36 on model_floating_person_token (person_id);
alter table model_grid_terminal add constraint fk_model_grid_terminal_person_37 foreign key (person_id) references model_person (id);
create index ix_model_grid_terminal_person_37 on model_grid_terminal (person_id);
alter table model_grid_widget add constraint fk_model_grid_widget_author_38 foreign key (author_id) references model_person (id);
create index ix_model_grid_widget_author_38 on model_grid_widget (author_id);
alter table model_grid_widget add constraint fk_model_grid_widget_type_of__39 foreign key (type_of_widget_id) references model_type_of_widget (id);
create index ix_model_grid_widget_type_of__39 on model_grid_widget (type_of_widget_id);
alter table model_grid_widget add constraint fk_model_grid_widget_producer_40 foreign key (producer_id) references model_producer (id);
create index ix_model_grid_widget_producer_40 on model_grid_widget (producer_id);
alter table model_grid_widget_version add constraint fk_model_grid_widget_version__41 foreign key (author_id) references model_person (id);
create index ix_model_grid_widget_version__41 on model_grid_widget_version (author_id);
alter table model_grid_widget_version add constraint fk_model_grid_widget_version__42 foreign key (grid_widget_id) references model_grid_widget (id);
create index ix_model_grid_widget_version__42 on model_grid_widget_version (grid_widget_id);
alter table model_homer_instance add constraint fk_model_homer_instance_cloud_43 foreign key (cloud_homer_server_unique_identificator) references model_homer_server (unique_identificator);
create index ix_model_homer_instance_cloud_43 on model_homer_instance (cloud_homer_server_unique_identificator);
alter table model_homer_instance_record add constraint fk_model_homer_instance_recor_44 foreign key (main_instance_history_blocko_instance_name) references model_homer_instance (blocko_instance_name);
create index ix_model_homer_instance_recor_44 on model_homer_instance_record (main_instance_history_blocko_instance_name);
alter table model_homer_instance_record add constraint fk_model_homer_instance_recor_45 foreign key (version_object_id) references model_version_object (id);
create index ix_model_homer_instance_recor_45 on model_homer_instance_record (version_object_id);
alter table model_homer_instance_record add constraint fk_model_homer_instance_recor_46 foreign key (actual_running_instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);
create index ix_model_homer_instance_recor_46 on model_homer_instance_record (actual_running_instance_blocko_instance_name);
alter table model_invitation add constraint fk_model_invitation_owner_47 foreign key (owner_id) references model_person (id);
create index ix_model_invitation_owner_47 on model_invitation (owner_id);
alter table model_invitation add constraint fk_model_invitation_project_48 foreign key (project_id) references model_project (id);
create index ix_model_invitation_project_48 on model_invitation (project_id);
alter table model_invoice add constraint fk_model_invoice_product_49 foreign key (product_id) references model_product (id);
create index ix_model_invoice_product_49 on model_invoice (product_id);
alter table model_invoice_item add constraint fk_model_invoice_item_invoice_50 foreign key (invoice_id) references model_invoice (id);
create index ix_model_invoice_item_invoice_50 on model_invoice_item (invoice_id);
alter table model_log add constraint fk_model_log_file_51 foreign key (file_id) references model_file_record (id);
create index ix_model_log_file_51 on model_log (file_id);
alter table model_mprogram add constraint fk_model_mprogram_m_project_52 foreign key (m_project_id) references model_mproject (id);
create index ix_model_mprogram_m_project_52 on model_mprogram (m_project_id);
alter table model_mproject add constraint fk_model_mproject_project_53 foreign key (project_id) references model_project (id);
create index ix_model_mproject_project_53 on model_mproject (project_id);
alter table model_mproject_program_snap_shot add constraint fk_model_mproject_program_sna_54 foreign key (m_project_id) references model_mproject (id);
create index ix_model_mproject_program_sna_54 on model_mproject_program_snap_shot (m_project_id);
alter table model_notification add constraint fk_model_notification_person_55 foreign key (person_id) references model_person (id);
create index ix_model_notification_person_55 on model_notification (person_id);
alter table model_password_recovery_token add constraint fk_model_password_recovery_to_56 foreign key (person_id) references model_person (id);
create index ix_model_password_recovery_to_56 on model_password_recovery_token (person_id);
alter table model_payment_details add constraint fk_model_payment_details_pers_57 foreign key (person_id) references model_person (id);
create index ix_model_payment_details_pers_57 on model_payment_details (person_id);
alter table model_payment_details add constraint fk_model_payment_details_prod_58 foreign key (productidpaymentdetails) references model_product (id);
create index ix_model_payment_details_prod_58 on model_payment_details (productidpaymentdetails);
alter table model_person add constraint fk_model_person_picture_59 foreign key (picture_id) references model_file_record (id);
create index ix_model_person_picture_59 on model_person (picture_id);
alter table model_product add constraint fk_model_product_tariff_60 foreign key (tariff_id) references model_tariff (id);
create index ix_model_product_tariff_60 on model_product (tariff_id);
alter table model_product_extension add constraint fk_model_product_extension_pr_61 foreign key (product_id) references model_product (id);
create index ix_model_product_extension_pr_61 on model_product_extension (product_id);
alter table model_product_extension add constraint fk_model_product_extension_ta_62 foreign key (tariff_included_id) references model_tariff (id);
create index ix_model_product_extension_ta_62 on model_product_extension (tariff_included_id);
alter table model_product_extension add constraint fk_model_product_extension_ta_63 foreign key (tariff_optional_id) references model_tariff (id);
create index ix_model_product_extension_ta_63 on model_product_extension (tariff_optional_id);
alter table model_project add constraint fk_model_project_private_inst_64 foreign key (private_instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);
create index ix_model_project_private_inst_64 on model_project (private_instance_blocko_instance_name);
alter table model_project add constraint fk_model_project_product_65 foreign key (product_id) references model_product (id);
create index ix_model_project_product_65 on model_project (product_id);
alter table model_project_participant add constraint fk_model_project_participant__66 foreign key (project_id) references model_project (id);
create index ix_model_project_participant__66 on model_project_participant (project_id);
alter table model_project_participant add constraint fk_model_project_participant__67 foreign key (person_id) references model_person (id);
create index ix_model_project_participant__67 on model_project_participant (person_id);
alter table model_tariff_label add constraint fk_model_tariff_label_tariff_68 foreign key (tariff_id) references model_tariff (id);
create index ix_model_tariff_label_tariff_68 on model_tariff_label (tariff_id);
alter table model_type_of_block add constraint fk_model_type_of_block_projec_69 foreign key (project_id) references model_project (id);
create index ix_model_type_of_block_projec_69 on model_type_of_block (project_id);
alter table model_type_of_board add constraint fk_model_type_of_board_produc_70 foreign key (producer_id) references model_producer (id);
create index ix_model_type_of_board_produc_70 on model_type_of_board (producer_id);
alter table model_type_of_board add constraint fk_model_type_of_board_proces_71 foreign key (processor_id) references model_processor (id);
create index ix_model_type_of_board_proces_71 on model_type_of_board (processor_id);
alter table model_type_of_board add constraint fk_model_type_of_board_pictur_72 foreign key (picture_id) references model_file_record (id);
create index ix_model_type_of_board_pictur_72 on model_type_of_board (picture_id);
alter table model_type_of_widget add constraint fk_model_type_of_widget_proje_73 foreign key (project_id) references model_project (id);
create index ix_model_type_of_widget_proje_73 on model_type_of_widget (project_id);
alter table model_version_object add constraint fk_model_version_object_autho_74 foreign key (author_id) references model_person (id);
create index ix_model_version_object_autho_74 on model_version_object (author_id);
alter table model_version_object add constraint fk_model_version_object_libra_75 foreign key (library_id) references model_import_library (id);
create index ix_model_version_object_libra_75 on model_version_object (library_id);
alter table model_version_object add constraint fk_model_version_object_c_pro_76 foreign key (c_program_id) references model_cprogram (id);
create index ix_model_version_object_c_pro_76 on model_version_object (c_program_id);
alter table model_version_object add constraint fk_model_version_object_defau_77 foreign key (default_program_id) references model_cprogram (id);
create index ix_model_version_object_defau_77 on model_version_object (default_program_id);
alter table model_version_object add constraint fk_model_version_object_b_pro_78 foreign key (b_program_id) references model_bprogram (id);
create index ix_model_version_object_b_pro_78 on model_version_object (b_program_id);
alter table model_version_object add constraint fk_model_version_object_m_pro_79 foreign key (m_program_id) references model_mprogram (id);
create index ix_model_version_object_m_pro_79 on model_version_object (m_program_id);



alter table model_import_library_model_type_ add constraint fk_model_import_library_model_01 foreign key (model_import_library_id) references model_import_library (id);

alter table model_import_library_model_type_ add constraint fk_model_import_library_model_02 foreign key (model_type_of_board_id) references model_type_of_board (id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshot_01 foreign key (model_mproject_program_snap_shot_id) references model_mproject_program_snap_shot (id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshot_02 foreign key (model_version_object_id) references model_version_object (id);

alter table m_project_program_snapshots add constraint fk_m_project_program_snapshot_01 foreign key (model_mproject_program_snap_shot_id) references model_mproject_program_snap_shot (id);

alter table m_project_program_snapshots add constraint fk_m_project_program_snapshot_02 foreign key (model_version_object_id) references model_version_object (id);

alter table model_person_model_security_role add constraint fk_model_person_model_securit_01 foreign key (model_person_id) references model_person (id);

alter table model_person_model_security_role add constraint fk_model_person_model_securit_02 foreign key (model_security_role_id) references model_security_role (id);

alter table model_person_model_permission add constraint fk_model_person_model_permiss_01 foreign key (model_person_id) references model_person (id);

alter table model_person_model_permission add constraint fk_model_person_model_permiss_02 foreign key (model_permission_value) references model_permission (value);

alter table model_security_role_model_permis add constraint fk_model_security_role_model__01 foreign key (model_security_role_id) references model_security_role (id);

alter table model_security_role_model_permis add constraint fk_model_security_role_model__02 foreign key (model_permission_value) references model_permission (value);

alter table model_c_program_library_version add constraint fk_model_c_program_library_ve_01 foreign key (library_version_id) references model_version_object (id);

alter table model_c_program_library_version add constraint fk_model_c_program_library_ve_02 foreign key (c_program_version_id) references model_version_object (id);

alter table model_version_object_model_bprog add constraint fk_model_version_object_model_01 foreign key (model_version_object_id) references model_version_object (id);

alter table model_version_object_model_bprog add constraint fk_model_version_object_model_02 foreign key (model_bprogram_hw_group_id) references model_bprogram_hw_group (id);

# --- !Downs

drop table if exists model_actualization_procedure cascade;

drop table if exists model_bpair cascade;

drop table if exists model_bprogram cascade;

drop table if exists model_bprogram_hw_group cascade;

drop table if exists model_version_object_model_bprog cascade;

drop table if exists model_blocko_block cascade;

drop table if exists model_blocko_block_version cascade;

drop table if exists model_board cascade;

drop table if exists model_boot_loader cascade;

drop table if exists model_ccompilation cascade;

drop table if exists model_cprogram cascade;

drop table if exists model_cprogram_update_plan cascade;

drop table if exists model_change_property_token cascade;

drop table if exists CompilationServer cascade;

drop table if exists model_example_model_name cascade;

drop table if exists model_file_record cascade;

drop table if exists model_floating_person_token cascade;

drop table if exists model_grid_terminal cascade;

drop table if exists model_grid_widget cascade;

drop table if exists model_grid_widget_version cascade;

drop table if exists model_homer_instance cascade;

drop table if exists model_homer_instance_record cascade;

drop table if exists model_homer_server cascade;

drop table if exists model_import_library cascade;

drop table if exists model_import_library_model_type_ cascade;

drop table if exists model_invitation cascade;

drop table if exists model_invoice cascade;

drop table if exists model_invoice_item cascade;

drop table if exists model_log cascade;

drop table if exists model_loggy_error cascade;

drop table if exists model_mprogram cascade;

drop table if exists model_mproject cascade;

drop table if exists model_mproject_program_snap_shot cascade;

drop table if exists b_program_version_snapshots cascade;

drop table if exists m_project_program_snapshots cascade;

drop table if exists model_mac_address_register_record cascade;

drop table if exists model_notification cascade;

drop table if exists model_password_recovery_token cascade;

drop table if exists model_payment_details cascade;

drop table if exists model_permission cascade;

drop table if exists model_person_model_permission cascade;

drop table if exists model_security_role_model_permis cascade;

drop table if exists model_person cascade;

drop table if exists model_person_model_security_role cascade;

drop table if exists model_processor cascade;

drop table if exists model_producer cascade;

drop table if exists model_product cascade;

drop table if exists model_product_extension cascade;

drop table if exists model_project cascade;

drop table if exists model_project_participant cascade;

drop table if exists model_request_log cascade;

drop table if exists model_security_role cascade;

drop table if exists model_tariff cascade;

drop table if exists model_tariff_label cascade;

drop table if exists model_type_of_block cascade;

drop table if exists model_type_of_board cascade;

drop table if exists model_type_of_widget cascade;

drop table if exists model_validation_token cascade;

drop table if exists model_version_object cascade;

drop table if exists model_c_program_library_version cascade;

drop sequence if exists model_invoice_item_seq;

drop sequence if exists model_payment_details_seq;

