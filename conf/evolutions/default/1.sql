# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table authorizationtoken (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  token                         uuid,
  person_id                     uuid,
  where_logged                  varchar(13),
  access_age                    timestamptz,
  user_agent                    varchar(255),
  provider_user_id              varchar(255),
  provider_key                  TEXT,
  type_of_connection            varchar(255),
  return_url                    varchar(255),
  social_token_verified         boolean default false not null,
  deleted                       boolean default false not null,
  constraint ck_authorizationtoken_where_logged check ( where_logged in ('E_STORE','HOMER_SERVER','BECKI_WEBSITE')),
  constraint pk_authorizationtoken primary key (id)
);

create table bprogram (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  project_id                    uuid,
  azure_b_program_link          varchar(255),
  deleted                       boolean default false not null,
  constraint pk_bprogram primary key (id)
);

create table bprogram_tag (
  bprogram_id                   uuid not null,
  tag_id                        uuid not null,
  constraint pk_bprogram_tag primary key (bprogram_id,tag_id)
);

create table bprogramversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  library_id                    uuid,
  b_program_id                  uuid,
  additional_configuration      varchar(255),
  deleted                       boolean default false not null,
  constraint ck_bprogramversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_bprogramversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_bprogramversion_file_id unique (file_id),
  constraint pk_bprogramversion primary key (id)
);

create table blob (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  path                          varchar(255),
  boot_loader_id                uuid,
  deleted                       boolean default false not null,
  constraint uq_blob_boot_loader_id unique (boot_loader_id),
  constraint pk_blob primary key (id)
);

create table block (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  active                        boolean default false not null,
  project_id                    uuid,
  producer_id                   uuid,
  order_position                integer,
  publish_type                  varchar(15),
  deleted                       boolean default false not null,
  constraint ck_block_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint pk_block primary key (id)
);

create table block_tag (
  block_id                      uuid not null,
  tag_id                        uuid not null,
  constraint pk_block_tag primary key (block_id,tag_id)
);

create table blockversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  design_json                   TEXT,
  logic_json                    TEXT,
  block_id                      uuid,
  deleted                       boolean default false not null,
  constraint ck_blockversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_blockversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_blockversion_file_id unique (file_id),
  constraint pk_blockversion primary key (id)
);

create table bootloader (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  version_identifier            varchar(255),
  changing_note                 TEXT,
  hardware_type_id              uuid,
  main_hardware_type_id         uuid,
  azure_product_link            varchar(255),
  deleted                       boolean default false not null,
  constraint uq_bootloader_main_hardware_type_id unique (main_hardware_type_id),
  constraint pk_bootloader primary key (id)
);

create table cprogram (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  project_id                    uuid,
  hardware_type_id              uuid,
  publish_type                  varchar(15),
  hardware_type_default_id      uuid,
  hardware_type_test_id         uuid,
  example_library_id            uuid,
  azure_c_program_link          varchar(255),
  deleted                       boolean default false not null,
  constraint ck_cprogram_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_cprogram_hardware_type_default_id unique (hardware_type_default_id),
  constraint uq_cprogram_hardware_type_test_id unique (hardware_type_test_id),
  constraint pk_cprogram primary key (id)
);

create table cprogram_tag (
  cprogram_id                   uuid not null,
  tag_id                        uuid not null,
  constraint pk_cprogram_tag primary key (cprogram_id,tag_id)
);

create table cprogramversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  c_program_id                  uuid,
  default_program_id            uuid,
  deleted                       boolean default false not null,
  constraint ck_cprogramversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_cprogramversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_cprogramversion_file_id unique (file_id),
  constraint uq_cprogramversion_default_program_id unique (default_program_id),
  constraint pk_cprogramversion primary key (id)
);

create table changepropertytoken (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  property                      varchar(255),
  value                         varchar(255),
  person_id                     uuid,
  deleted                       boolean default false not null,
  constraint uq_changepropertytoken_person_id unique (person_id),
  constraint pk_changepropertytoken primary key (id)
);

create table compilation (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  c_compilation_version         uuid,
  status                        varchar(23),
  virtual_input_output          TEXT,
  build_url                     TEXT,
  bin_compilation_file_id       uuid,
  firmware_version_core         varchar(255),
  firmware_version_mbed         varchar(255),
  firmware_version_lib          varchar(255),
  firmware_build_id             varchar(255),
  firmware_build_datetime       timestamptz,
  deleted                       boolean default false not null,
  constraint ck_compilation_status check ( status in ('IN_PROGRESS','FILE_NOT_FOUND','SERVER_ERROR','SUCCESS','FAILED','UNDEFINED','BROKEN_JSON','SUCCESS_DOWNLOAD_FAILED','UNSTABLE','SERVER_OFFLINE')),
  constraint uq_compilation_c_compilation_version unique (c_compilation_version),
  constraint uq_compilation_bin_compilation_file_id unique (bin_compilation_file_id),
  constraint pk_compilation primary key (id)
);

create table compilationserver (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  personal_server_name          varchar(255),
  connection_identifier         varchar(255),
  hash_certificate              varchar(255),
  server_url                    varchar(255),
  deleted                       boolean default false not null,
  constraint uq_compilationserver_personal_server_name unique (personal_server_name),
  constraint uq_compilationserver_server_url unique (server_url),
  constraint pk_compilationserver primary key (id)
);

create table customer (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  fakturoid_subject_id          varchar(255),
  deleted                       boolean default false not null,
  constraint pk_customer primary key (id)
);

create table employee (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  state                         varchar(7),
  person_id                     uuid,
  customer_id                   uuid,
  deleted                       boolean default false not null,
  constraint ck_employee_state check ( state in ('OWNER','INVITED','ADMIN','MEMBER')),
  constraint pk_employee primary key (id)
);

create table garfield (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  hardware_tester_id            varchar(255),
  print_label_id_1              integer,
  print_label_id_2              integer,
  print_sticker_id              integer,
  hardware_type_id              uuid,
  producer_id                   uuid,
  configurations                TEXT,
  deleted                       boolean default false not null,
  constraint uq_garfield_hardware_tester_id unique (hardware_tester_id),
  constraint pk_garfield primary key (id)
);

create table gridprogram (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  grid_project_id               uuid,
  blob_link                     varchar(255),
  deleted                       boolean default false not null,
  constraint pk_gridprogram primary key (id)
);

create table gridprogram_tag (
  grid_program_id               uuid not null,
  tag_id                        uuid not null,
  constraint pk_gridprogram_tag primary key (grid_program_id,tag_id)
);

create table gridprogramversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  grid_program_id               uuid,
  m_program_virtual_input_output TEXT,
  public_access                 boolean default false not null,
  deleted                       boolean default false not null,
  constraint ck_gridprogramversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_gridprogramversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_gridprogramversion_file_id unique (file_id),
  constraint pk_gridprogramversion primary key (id)
);

create table gridproject (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  project_id                    uuid,
  blob_link                     varchar(255),
  deleted                       boolean default false not null,
  constraint pk_gridproject primary key (id)
);

create table gridproject_tag (
  grid_project_id               uuid not null,
  tag_id                        uuid not null,
  constraint pk_gridproject_tag primary key (grid_project_id,tag_id)
);

create table gridterminal (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  terminal_token                varchar(255),
  user_agent                    varchar(255),
  device_type                   varchar(255),
  device_name                   varchar(255),
  person_id                     uuid,
  ws_permission                 boolean default false not null,
  m_program_access              boolean default false not null,
  up_to_date                    boolean default false not null,
  deleted                       boolean default false not null,
  constraint pk_gridterminal primary key (id)
);

create table hardware (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  full_id                       varchar(255),
  dominant_entity               boolean default false not null,
  wifi_mac_address              varchar(255),
  mac_address                   varchar(255),
  json_bootloader_core_configuration TEXT,
  batch_id                      varchar(255),
  is_active                     boolean default false not null,
  mqtt_password                 varchar(255),
  mqtt_username                 varchar(255),
  developer_kit                 boolean default false not null,
  backup_mode                   boolean default false not null,
  database_synchronize          boolean default false not null,
  hardware_type_id              uuid,
  actual_c_program_version_id   uuid,
  actual_backup_c_program_version_id uuid,
  actual_boot_loader_id         uuid,
  picture_id                    uuid,
  project_id                    uuid,
  connected_server_id           uuid,
  connected_instance_id         uuid,
  deleted                       boolean default false not null,
  constraint uq_hardware_picture_id unique (picture_id),
  constraint pk_hardware primary key (id)
);

create table hardware_tag (
  hardware_id                   uuid not null,
  tag_id                        uuid not null,
  constraint pk_hardware_tag primary key (hardware_id,tag_id)
);

create table hardware_hardwaregroup (
  hardware_id                   uuid not null,
  hardware_group_id             uuid not null,
  constraint pk_hardware_hardwaregroup primary key (hardware_id,hardware_group_id)
);

create table hardwarefeature (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  deleted                       boolean default false not null,
  constraint pk_hardwarefeature primary key (id)
);

create table hardwarefeature_hardwaretype (
  hardware_feature_id           uuid not null,
  hardware_type_id              uuid not null,
  constraint pk_hardwarefeature_hardwaretype primary key (hardware_feature_id,hardware_type_id)
);

create table hardwaregroup (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  project_id                    uuid,
  deleted                       boolean default false not null,
  constraint pk_hardwaregroup primary key (id)
);

create table hardwaretype (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  compiler_target_name          varchar(255),
  producer_id                   uuid,
  processor_id                  uuid,
  connectible_to_internet       boolean,
  picture_id                    uuid,
  deleted                       boolean default false not null,
  constraint uq_hardwaretype_compiler_target_name unique (compiler_target_name),
  constraint uq_hardwaretype_picture_id unique (picture_id),
  constraint pk_hardwaretype primary key (id)
);

create table hardwareupdate (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  actualization_procedure_id    uuid,
  date_of_finish                timestamptz,
  hardware_id                   uuid,
  firmware_type                 varchar(10),
  c_program_version_for_update_id uuid,
  bootloader_id                 uuid,
  binary_file_id                uuid,
  state                         varchar(28),
  count_of_tries                integer,
  error                         varchar(255),
  error_code                    integer,
  deleted                       boolean default false not null,
  constraint ck_hardwareupdate_firmware_type check ( firmware_type in ('BACKUP','FIRMWARE','BOOTLOADER','WIFI')),
  constraint ck_hardwareupdate_state check ( state in ('NOT_YET_STARTED','IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')),
  constraint pk_hardwareupdate primary key (id)
);

create table homerserver (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  connection_identifier         varchar(255),
  hash_certificate              varchar(255),
  personal_server_name          varchar(255),
  json_additional_parameter     TEXT,
  mqtt_port                     integer,
  grid_port                     integer,
  web_view_port                 integer,
  hardware_logger_port          integer,
  rest_api_port                 integer,
  server_url                    varchar(255),
  server_version                varchar(255),
  project_id                    uuid,
  server_type                   varchar(7),
  time_stamp_configuration      timestamptz,
  days_in_archive               integer,
  logging                       boolean default false not null,
  interactive                   boolean default false not null,
  log_level                     integer,
  deleted                       boolean default false not null,
  constraint ck_homerserver_server_type check ( server_type in ('BACKUP','TEST','PUBLIC','MAIN','PRIVATE')),
  constraint ck_homerserver_log_level check ( log_level in (0,1,2,3,4)),
  constraint pk_homerserver primary key (id)
);

create table instance (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  current_snapshot_id           uuid,
  server_main_id                uuid,
  server_backup_id              uuid,
  project_id                    uuid,
  b_program_id                  uuid,
  deleted                       boolean default false not null,
  constraint pk_instance primary key (id)
);

create table instance_tag (
  instance_id                   uuid not null,
  tag_id                        uuid not null,
  constraint pk_instance_tag primary key (instance_id,tag_id)
);

create table instancesnapshot (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  deployed                      timestamptz,
  stopped                       timestamptz,
  instance_id                   uuid,
  b_program_version_id          uuid,
  program_id                    uuid,
  deleted                       boolean default false not null,
  constraint uq_instancesnapshot_program_id unique (program_id),
  constraint pk_instancesnapshot primary key (id)
);

create table invitation (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  owner_id                      uuid,
  project_id                    uuid,
  email                         varchar(255),
  notification_id               uuid,
  deleted                       boolean default false not null,
  constraint pk_invitation primary key (id)
);

create table invoice (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  fakturoid_id                  bigint,
  fakturoid_pdf_url             varchar(255),
  invoice_number                varchar(255),
  gopay_id                      bigint,
  gopay_order_number            varchar(255),
  gw_url                        varchar(255),
  proforma                      boolean default false not null,
  proforma_id                   bigint,
  proforma_pdf_url              varchar(255),
  paid                          timestamptz,
  overdue                       timestamptz,
  product_id                    uuid,
  status                        varchar(8),
  method                        varchar(13),
  warning                       varchar(12),
  deleted                       boolean default false not null,
  constraint ck_invoice_status check ( status in ('CANCELED','PAID','PENDING','OVERDUE')),
  constraint ck_invoice_method check ( method in ('CREDIT','BANK_TRANSFER','CREDIT_CARD','FREE')),
  constraint ck_invoice_warning check ( warning in ('ZERO_BALANCE','SECOND','NONE','FIRST','DEACTIVATION')),
  constraint pk_invoice primary key (id)
);

create table invoiceitem (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  invoice_id                    uuid,
  name                          varchar(255),
  quantity                      bigint,
  unit_name                     varchar(255),
  unit_price                    bigint,
  currency                      varchar(3),
  deleted                       boolean default false not null,
  constraint ck_invoiceitem_currency check ( currency in ('EUR','CZK','USD')),
  constraint pk_invoiceitem primary key (id)
);

create table library (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  project_id                    uuid,
  publish_type                  varchar(15),
  azure_library_link            varchar(255),
  deleted                       boolean default false not null,
  constraint ck_library_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint pk_library primary key (id)
);

create table library_tag (
  library_id                    uuid not null,
  tag_id                        uuid not null,
  constraint pk_library_tag primary key (library_id,tag_id)
);

create table library_hardwaretype (
  library_id                    uuid not null,
  hardware_type_id              uuid not null,
  constraint pk_library_hardwaretype primary key (library_id,hardware_type_id)
);

create table libraryversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  library_id                    uuid,
  deleted                       boolean default false not null,
  constraint ck_libraryversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_libraryversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_libraryversion_file_id unique (file_id),
  constraint pk_libraryversion primary key (id)
);

create table log (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  type                          varchar(255),
  file_id                       uuid,
  deleted                       boolean default false not null,
  constraint uq_log_file_id unique (file_id),
  constraint pk_log primary key (id)
);

create table mprograminstanceparameter (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  grid_project_program_snapshot_id uuid,
  grid_program_version_id       uuid,
  connection_token              varchar(255),
  snapshot_settings             varchar(7),
  deleted                       boolean default false not null,
  constraint ck_mprograminstanceparameter_snapshot_settings check ( snapshot_settings in ('PROJECT','TESTING','PUBLIC')),
  constraint pk_mprograminstanceparameter primary key (id)
);

create table mprojectprogramsnapshot (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  grid_project_id               uuid,
  deleted                       boolean default false not null,
  constraint pk_mprojectprogramsnapshot primary key (id)
);

create table b_program_version_snapshots (
  mproject_program_snap_shot_id uuid not null,
  bprogram_version_id           uuid not null,
  constraint pk_b_program_version_snapshots primary key (mproject_program_snap_shot_id,bprogram_version_id)
);

create table notification (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  notification_level            varchar(7),
  notification_importance       varchar(6),
  state                         varchar(11),
  content_string                TEXT,
  buttons_string                TEXT,
  confirmation_required         boolean default false not null,
  confirmed                     boolean default false not null,
  was_read                      boolean default false not null,
  person_id                     uuid,
  deleted                       boolean default false not null,
  constraint ck_notification_notification_level check ( notification_level in ('SUCCESS','ERROR','INFO','WARNING')),
  constraint ck_notification_notification_importance check ( notification_importance in ('HIGH','LOW','NORMAL')),
  constraint ck_notification_state check ( state in ('CREATED','UPDATED','UNCONFIRMED','DELETED')),
  constraint pk_notification primary key (id)
);

create table passwordrecoverytoken (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  person_id                     uuid,
  password_recovery_token       varchar(255),
  deleted                       boolean default false not null,
  constraint uq_passwordrecoverytoken_person_id unique (person_id),
  constraint pk_passwordrecoverytoken primary key (id)
);

create table paymentdetails (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  customer_id                   uuid,
  productidpaymentdetails       uuid,
  company_account               boolean default false not null,
  company_name                  varchar(255),
  company_authorized_email      varchar(255),
  company_authorized_phone      varchar(255),
  company_web                   varchar(255),
  company_registration_no       varchar(255),
  company_vat_number            varchar(255),
  full_name                     varchar(255),
  street                        varchar(255),
  street_number                 varchar(255),
  city                          varchar(255),
  zip_code                      varchar(255),
  country                       varchar(255),
  invoice_email                 varchar(255),
  bank_account                  varchar(255),
  deleted                       boolean default false not null,
  constraint uq_paymentdetails_customer_id unique (customer_id),
  constraint uq_paymentdetails_productidpaymentdetails unique (productidpaymentdetails),
  constraint pk_paymentdetails primary key (id)
);

create table permission (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  deleted                       boolean default false not null,
  constraint pk_permission primary key (id)
);

create table person (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  email                         varchar(255),
  nick_name                     varchar(255),
  first_name                    varchar(255),
  last_name                     varchar(255),
  country                       varchar(255),
  gender                        varchar(255),
  portal_config                 varchar(255),
  validated                     boolean default false not null,
  frozen                        boolean default false not null,
  password                      varchar(255),
  alternative_picture_link      varchar(255),
  facebook_oauth_id             varchar(255),
  github_oauth_id               varchar(255),
  picture_id                    uuid,
  deleted                       boolean default false not null,
  constraint uq_person_email unique (email),
  constraint uq_person_nick_name unique (nick_name),
  constraint uq_person_picture_id unique (picture_id),
  constraint pk_person primary key (id)
);

create table person_role (
  person_id                     uuid not null,
  role_id                       uuid not null,
  constraint pk_person_role primary key (person_id,role_id)
);

create table person_permission (
  person_id                     uuid not null,
  permission_id                 uuid not null,
  constraint pk_person_permission primary key (person_id,permission_id)
);

create table processor (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  processor_code                varchar(255),
  speed                         integer not null,
  deleted                       boolean default false not null,
  constraint pk_processor primary key (id)
);

create table producer (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  deleted                       boolean default false not null,
  constraint pk_producer primary key (id)
);

create table product (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  method                        varchar(13),
  business_model                varchar(11),
  subscription_id               varchar(255),
  fakturoid_subject_id          varchar(255),
  gopay_id                      bigint,
  active                        boolean default false not null,
  on_demand                     boolean default false not null,
  credit                        bigint,
  financial_history             TEXT,
  configuration                 TEXT,
  removed_byinvoi_user          boolean default false not null,
  client_billing                boolean default false not null,
  client_billing_invoice_parameters varchar(255),
  customer_id                   uuid,
  azure_product_link            varchar(255),
  deleted                       boolean default false not null,
  constraint ck_product_method check ( method in ('CREDIT','BANK_TRANSFER','CREDIT_CARD','FREE')),
  constraint ck_product_business_model check ( business_model in ('INTEGRATOR','INTEGRATION','SAAS','FEE','ALPHA','CAL')),
  constraint pk_product primary key (id)
);

create table productextension (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  color                         varchar(255),
  type                          varchar(12),
  configuration                 TEXT,
  order_position                integer,
  active                        boolean default false not null,
  product_id                    uuid,
  tariff_included_id            uuid,
  tariff_optional_id            uuid,
  deleted                       boolean default false not null,
  constraint ck_productextension_type check ( type in ('database','instance','log','rest_api','project','support','homer_server','participant')),
  constraint pk_productextension primary key (id)
);

create table project (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  product_id                    uuid,
  blob_project_link             varchar(255),
  deleted                       boolean default false not null,
  constraint pk_project primary key (id)
);

create table project_tag (
  project_id                    uuid not null,
  tag_id                        uuid not null,
  constraint pk_project_tag primary key (project_id,tag_id)
);

create table projectparticipant (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  project_id                    uuid,
  person_id                     uuid,
  state                         varchar(7),
  deleted                       boolean default false not null,
  constraint ck_projectparticipant_state check ( state in ('OWNER','INVITED','ADMIN','MEMBER')),
  constraint pk_projectparticipant primary key (id)
);

create table role (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  deleted                       boolean default false not null,
  constraint pk_role primary key (id)
);

create table role_permission (
  role_id                       uuid not null,
  permission_id                 uuid not null,
  constraint pk_role_permission primary key (role_id,permission_id)
);

create table servererror (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  type                          varchar(255),
  message                       TEXT,
  stack_trace                   TEXT,
  request                       varchar(255),
  person                        varchar(255),
  tyrion                        varchar(255),
  repetition                    bigint,
  cause_type                    varchar(255),
  cause_message                 TEXT,
  cause_stack_trace             TEXT,
  youtrack_url                  varchar(255),
  deleted                       boolean default false not null,
  constraint pk_servererror primary key (id)
);

create table tag (
  id                            uuid not null,
  value                         varchar(255),
  constraint uq_tag_value unique (value),
  constraint pk_tag primary key (id)
);

create table tariff (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  identifier                    varchar(255),
  active                        boolean default false not null,
  business_model                varchar(11),
  order_position                integer,
  company_details_required      boolean default false not null,
  payment_details_required      boolean default false not null,
  payment_method_required       boolean default false not null,
  credit_for_beginning          bigint,
  color                         varchar(255),
  awesome_icon                  varchar(255),
  labels_json                   varchar(255),
  deleted                       boolean default false not null,
  constraint ck_tariff_business_model check ( business_model in ('INTEGRATOR','INTEGRATION','SAAS','FEE','ALPHA','CAL')),
  constraint uq_tariff_identifier unique (identifier),
  constraint pk_tariff primary key (id)
);

create table updateprocedure (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  state                         varchar(19),
  instance_id                   uuid,
  date_of_planing               timestamptz,
  date_of_finish                timestamptz,
  type_of_update                varchar(41),
  project_id                    uuid,
  deleted                       boolean default false not null,
  constraint ck_updateprocedure_state check ( state in ('complete_with_error','canceled','in_progress','successful_complete','complete','not_start_yet')),
  constraint ck_updateprocedure_type_of_update check ( type_of_update in ('AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE','AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE','MANUALLY_RELEASE_MANAGER','MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME','MANUALLY_BY_USER','MANUALLY_BY_USER_BLOCKO_GROUP')),
  constraint pk_updateprocedure primary key (id)
);

create table validationtoken (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  email                         varchar(255),
  token                         uuid,
  deleted                       boolean default false not null,
  constraint pk_validationtoken primary key (id)
);

create table widget (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  order_position                integer,
  project_id                    uuid,
  producer_id                   uuid,
  publish_type                  varchar(15),
  active                        boolean default false not null,
  deleted                       boolean default false not null,
  constraint ck_widget_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint pk_widget primary key (id)
);

create table widget_tag (
  widget_id                     uuid not null,
  tag_id                        uuid not null,
  constraint pk_widget_tag primary key (widget_id,tag_id)
);

create table widgetversion (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  file_id                       uuid,
  approval_state                varchar(11),
  publish_type                  varchar(15),
  blob_version_link             varchar(255),
  design_json                   TEXT,
  logic_json                    TEXT,
  widget_id                     uuid,
  deleted                       boolean default false not null,
  constraint ck_widgetversion_approval_state check ( approval_state in ('EDITED','DISAPPROVED','PENDING','APPROVED')),
  constraint ck_widgetversion_publish_type check ( publish_type in ('PUBLIC','DEFAULT_VERSION','DEFAULT_MAIN','PRIVATE','DEFAULT_TEST')),
  constraint uq_widgetversion_file_id unique (file_id),
  constraint pk_widgetversion primary key (id)
);

alter table authorizationtoken add constraint fk_authorizationtoken_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_authorizationtoken_person_id on authorizationtoken (person_id);

alter table bprogram add constraint fk_bprogram_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_bprogram_project_id on bprogram (project_id);

alter table bprogram_tag add constraint fk_bprogram_tag_bprogram foreign key (bprogram_id) references bprogram (id) on delete restrict on update restrict;
create index ix_bprogram_tag_bprogram on bprogram_tag (bprogram_id);

alter table bprogram_tag add constraint fk_bprogram_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_bprogram_tag_tag on bprogram_tag (tag_id);

alter table bprogramversion add constraint fk_bprogramversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table bprogramversion add constraint fk_bprogramversion_library_id foreign key (library_id) references library (id) on delete restrict on update restrict;
create index ix_bprogramversion_library_id on bprogramversion (library_id);

alter table bprogramversion add constraint fk_bprogramversion_b_program_id foreign key (b_program_id) references bprogram (id) on delete restrict on update restrict;
create index ix_bprogramversion_b_program_id on bprogramversion (b_program_id);

alter table blob add constraint fk_blob_boot_loader_id foreign key (boot_loader_id) references bootloader (id) on delete restrict on update restrict;

alter table block add constraint fk_block_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_block_project_id on block (project_id);

alter table block add constraint fk_block_producer_id foreign key (producer_id) references producer (id) on delete restrict on update restrict;
create index ix_block_producer_id on block (producer_id);

alter table block_tag add constraint fk_block_tag_block foreign key (block_id) references block (id) on delete restrict on update restrict;
create index ix_block_tag_block on block_tag (block_id);

alter table block_tag add constraint fk_block_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_block_tag_tag on block_tag (tag_id);

alter table blockversion add constraint fk_blockversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table blockversion add constraint fk_blockversion_block_id foreign key (block_id) references block (id) on delete restrict on update restrict;
create index ix_blockversion_block_id on blockversion (block_id);

alter table bootloader add constraint fk_bootloader_hardware_type_id foreign key (hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;
create index ix_bootloader_hardware_type_id on bootloader (hardware_type_id);

alter table bootloader add constraint fk_bootloader_main_hardware_type_id foreign key (main_hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;

alter table cprogram add constraint fk_cprogram_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_cprogram_project_id on cprogram (project_id);

alter table cprogram add constraint fk_cprogram_hardware_type_id foreign key (hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;
create index ix_cprogram_hardware_type_id on cprogram (hardware_type_id);

alter table cprogram add constraint fk_cprogram_hardware_type_default_id foreign key (hardware_type_default_id) references hardwaretype (id) on delete restrict on update restrict;

alter table cprogram add constraint fk_cprogram_hardware_type_test_id foreign key (hardware_type_test_id) references hardwaretype (id) on delete restrict on update restrict;

alter table cprogram add constraint fk_cprogram_example_library_id foreign key (example_library_id) references libraryversion (id) on delete restrict on update restrict;
create index ix_cprogram_example_library_id on cprogram (example_library_id);

alter table cprogram_tag add constraint fk_cprogram_tag_cprogram foreign key (cprogram_id) references cprogram (id) on delete restrict on update restrict;
create index ix_cprogram_tag_cprogram on cprogram_tag (cprogram_id);

alter table cprogram_tag add constraint fk_cprogram_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_cprogram_tag_tag on cprogram_tag (tag_id);

alter table cprogramversion add constraint fk_cprogramversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table cprogramversion add constraint fk_cprogramversion_c_program_id foreign key (c_program_id) references cprogram (id) on delete restrict on update restrict;
create index ix_cprogramversion_c_program_id on cprogramversion (c_program_id);

alter table cprogramversion add constraint fk_cprogramversion_default_program_id foreign key (default_program_id) references cprogram (id) on delete restrict on update restrict;

alter table changepropertytoken add constraint fk_changepropertytoken_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;

alter table compilation add constraint fk_compilation_c_compilation_version foreign key (c_compilation_version) references cprogramversion (id) on delete restrict on update restrict;

alter table compilation add constraint fk_compilation_bin_compilation_file_id foreign key (bin_compilation_file_id) references blob (id) on delete restrict on update restrict;

alter table employee add constraint fk_employee_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_employee_person_id on employee (person_id);

alter table employee add constraint fk_employee_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;
create index ix_employee_customer_id on employee (customer_id);

alter table gridprogram add constraint fk_gridprogram_grid_project_id foreign key (grid_project_id) references gridproject (id) on delete restrict on update restrict;
create index ix_gridprogram_grid_project_id on gridprogram (grid_project_id);

alter table gridprogram_tag add constraint fk_gridprogram_tag_gridprogram foreign key (grid_program_id) references gridprogram (id) on delete restrict on update restrict;
create index ix_gridprogram_tag_gridprogram on gridprogram_tag (grid_program_id);

alter table gridprogram_tag add constraint fk_gridprogram_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_gridprogram_tag_tag on gridprogram_tag (tag_id);

alter table gridprogramversion add constraint fk_gridprogramversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table gridprogramversion add constraint fk_gridprogramversion_grid_program_id foreign key (grid_program_id) references gridprogram (id) on delete restrict on update restrict;
create index ix_gridprogramversion_grid_program_id on gridprogramversion (grid_program_id);

alter table gridproject add constraint fk_gridproject_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_gridproject_project_id on gridproject (project_id);

alter table gridproject_tag add constraint fk_gridproject_tag_gridproject foreign key (grid_project_id) references gridproject (id) on delete restrict on update restrict;
create index ix_gridproject_tag_gridproject on gridproject_tag (grid_project_id);

alter table gridproject_tag add constraint fk_gridproject_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_gridproject_tag_tag on gridproject_tag (tag_id);

alter table gridterminal add constraint fk_gridterminal_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_gridterminal_person_id on gridterminal (person_id);

alter table hardware add constraint fk_hardware_hardware_type_id foreign key (hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;
create index ix_hardware_hardware_type_id on hardware (hardware_type_id);

alter table hardware add constraint fk_hardware_actual_c_program_version_id foreign key (actual_c_program_version_id) references cprogramversion (id) on delete restrict on update restrict;
create index ix_hardware_actual_c_program_version_id on hardware (actual_c_program_version_id);

alter table hardware add constraint fk_hardware_actual_backup_c_program_version_id foreign key (actual_backup_c_program_version_id) references cprogramversion (id) on delete restrict on update restrict;
create index ix_hardware_actual_backup_c_program_version_id on hardware (actual_backup_c_program_version_id);

alter table hardware add constraint fk_hardware_actual_boot_loader_id foreign key (actual_boot_loader_id) references bootloader (id) on delete restrict on update restrict;
create index ix_hardware_actual_boot_loader_id on hardware (actual_boot_loader_id);

alter table hardware add constraint fk_hardware_picture_id foreign key (picture_id) references blob (id) on delete restrict on update restrict;

alter table hardware add constraint fk_hardware_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_hardware_project_id on hardware (project_id);

alter table hardware_tag add constraint fk_hardware_tag_hardware foreign key (hardware_id) references hardware (id) on delete restrict on update restrict;
create index ix_hardware_tag_hardware on hardware_tag (hardware_id);

alter table hardware_tag add constraint fk_hardware_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_hardware_tag_tag on hardware_tag (tag_id);

alter table hardware_hardwaregroup add constraint fk_hardware_hardwaregroup_hardware foreign key (hardware_id) references hardware (id) on delete restrict on update restrict;
create index ix_hardware_hardwaregroup_hardware on hardware_hardwaregroup (hardware_id);

alter table hardware_hardwaregroup add constraint fk_hardware_hardwaregroup_hardwaregroup foreign key (hardware_group_id) references hardwaregroup (id) on delete restrict on update restrict;
create index ix_hardware_hardwaregroup_hardwaregroup on hardware_hardwaregroup (hardware_group_id);

alter table hardwarefeature_hardwaretype add constraint fk_hardwarefeature_hardwaretype_hardwarefeature foreign key (hardware_feature_id) references hardwarefeature (id) on delete restrict on update restrict;
create index ix_hardwarefeature_hardwaretype_hardwarefeature on hardwarefeature_hardwaretype (hardware_feature_id);

alter table hardwarefeature_hardwaretype add constraint fk_hardwarefeature_hardwaretype_hardwaretype foreign key (hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;
create index ix_hardwarefeature_hardwaretype_hardwaretype on hardwarefeature_hardwaretype (hardware_type_id);

alter table hardwaregroup add constraint fk_hardwaregroup_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_hardwaregroup_project_id on hardwaregroup (project_id);

alter table hardwaretype add constraint fk_hardwaretype_producer_id foreign key (producer_id) references producer (id) on delete restrict on update restrict;
create index ix_hardwaretype_producer_id on hardwaretype (producer_id);

alter table hardwaretype add constraint fk_hardwaretype_processor_id foreign key (processor_id) references processor (id) on delete restrict on update restrict;
create index ix_hardwaretype_processor_id on hardwaretype (processor_id);

alter table hardwaretype add constraint fk_hardwaretype_picture_id foreign key (picture_id) references blob (id) on delete restrict on update restrict;

alter table hardwareupdate add constraint fk_hardwareupdate_actualization_procedure_id foreign key (actualization_procedure_id) references updateprocedure (id) on delete restrict on update restrict;
create index ix_hardwareupdate_actualization_procedure_id on hardwareupdate (actualization_procedure_id);

alter table hardwareupdate add constraint fk_hardwareupdate_hardware_id foreign key (hardware_id) references hardware (id) on delete restrict on update restrict;
create index ix_hardwareupdate_hardware_id on hardwareupdate (hardware_id);

alter table hardwareupdate add constraint fk_hardwareupdate_c_program_version_for_update_id foreign key (c_program_version_for_update_id) references cprogramversion (id) on delete restrict on update restrict;
create index ix_hardwareupdate_c_program_version_for_update_id on hardwareupdate (c_program_version_for_update_id);

alter table hardwareupdate add constraint fk_hardwareupdate_bootloader_id foreign key (bootloader_id) references bootloader (id) on delete restrict on update restrict;
create index ix_hardwareupdate_bootloader_id on hardwareupdate (bootloader_id);

alter table hardwareupdate add constraint fk_hardwareupdate_binary_file_id foreign key (binary_file_id) references blob (id) on delete restrict on update restrict;
create index ix_hardwareupdate_binary_file_id on hardwareupdate (binary_file_id);

alter table homerserver add constraint fk_homerserver_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_homerserver_project_id on homerserver (project_id);

alter table instance add constraint fk_instance_server_main_id foreign key (server_main_id) references homerserver (id) on delete restrict on update restrict;
create index ix_instance_server_main_id on instance (server_main_id);

alter table instance add constraint fk_instance_server_backup_id foreign key (server_backup_id) references homerserver (id) on delete restrict on update restrict;
create index ix_instance_server_backup_id on instance (server_backup_id);

alter table instance add constraint fk_instance_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_instance_project_id on instance (project_id);

alter table instance add constraint fk_instance_b_program_id foreign key (b_program_id) references bprogram (id) on delete restrict on update restrict;
create index ix_instance_b_program_id on instance (b_program_id);

alter table instance_tag add constraint fk_instance_tag_instance foreign key (instance_id) references instance (id) on delete restrict on update restrict;
create index ix_instance_tag_instance on instance_tag (instance_id);

alter table instance_tag add constraint fk_instance_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_instance_tag_tag on instance_tag (tag_id);

alter table instancesnapshot add constraint fk_instancesnapshot_instance_id foreign key (instance_id) references instance (id) on delete restrict on update restrict;
create index ix_instancesnapshot_instance_id on instancesnapshot (instance_id);

alter table instancesnapshot add constraint fk_instancesnapshot_b_program_version_id foreign key (b_program_version_id) references bprogramversion (id) on delete restrict on update restrict;
create index ix_instancesnapshot_b_program_version_id on instancesnapshot (b_program_version_id);

alter table instancesnapshot add constraint fk_instancesnapshot_program_id foreign key (program_id) references blob (id) on delete restrict on update restrict;

alter table invitation add constraint fk_invitation_owner_id foreign key (owner_id) references person (id) on delete restrict on update restrict;
create index ix_invitation_owner_id on invitation (owner_id);

alter table invitation add constraint fk_invitation_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_invitation_project_id on invitation (project_id);

alter table invoice add constraint fk_invoice_product_id foreign key (product_id) references product (id) on delete restrict on update restrict;
create index ix_invoice_product_id on invoice (product_id);

alter table invoiceitem add constraint fk_invoiceitem_invoice_id foreign key (invoice_id) references invoice (id) on delete restrict on update restrict;
create index ix_invoiceitem_invoice_id on invoiceitem (invoice_id);

alter table library add constraint fk_library_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_library_project_id on library (project_id);

alter table library_tag add constraint fk_library_tag_library foreign key (library_id) references library (id) on delete restrict on update restrict;
create index ix_library_tag_library on library_tag (library_id);

alter table library_tag add constraint fk_library_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_library_tag_tag on library_tag (tag_id);

alter table library_hardwaretype add constraint fk_library_hardwaretype_library foreign key (library_id) references library (id) on delete restrict on update restrict;
create index ix_library_hardwaretype_library on library_hardwaretype (library_id);

alter table library_hardwaretype add constraint fk_library_hardwaretype_hardwaretype foreign key (hardware_type_id) references hardwaretype (id) on delete restrict on update restrict;
create index ix_library_hardwaretype_hardwaretype on library_hardwaretype (hardware_type_id);

alter table libraryversion add constraint fk_libraryversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table libraryversion add constraint fk_libraryversion_library_id foreign key (library_id) references library (id) on delete restrict on update restrict;
create index ix_libraryversion_library_id on libraryversion (library_id);

alter table log add constraint fk_log_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table mprograminstanceparameter add constraint fk_mprograminstanceparameter_grid_project_program_snapsho_1 foreign key (grid_project_program_snapshot_id) references mprojectprogramsnapshot (id) on delete restrict on update restrict;
create index ix_mprograminstanceparameter_grid_project_program_snapsho_1 on mprograminstanceparameter (grid_project_program_snapshot_id);

alter table mprograminstanceparameter add constraint fk_mprograminstanceparameter_grid_program_version_id foreign key (grid_program_version_id) references gridprogramversion (id) on delete restrict on update restrict;
create index ix_mprograminstanceparameter_grid_program_version_id on mprograminstanceparameter (grid_program_version_id);

alter table mprojectprogramsnapshot add constraint fk_mprojectprogramsnapshot_grid_project_id foreign key (grid_project_id) references gridproject (id) on delete restrict on update restrict;
create index ix_mprojectprogramsnapshot_grid_project_id on mprojectprogramsnapshot (grid_project_id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshots_mprojectprogramsnapshot foreign key (mproject_program_snap_shot_id) references mprojectprogramsnapshot (id) on delete restrict on update restrict;
create index ix_b_program_version_snapshots_mprojectprogramsnapshot on b_program_version_snapshots (mproject_program_snap_shot_id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshots_bprogramversion foreign key (bprogram_version_id) references bprogramversion (id) on delete restrict on update restrict;
create index ix_b_program_version_snapshots_bprogramversion on b_program_version_snapshots (bprogram_version_id);

alter table notification add constraint fk_notification_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_notification_person_id on notification (person_id);

alter table passwordrecoverytoken add constraint fk_passwordrecoverytoken_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;

alter table paymentdetails add constraint fk_paymentdetails_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;

alter table paymentdetails add constraint fk_paymentdetails_productidpaymentdetails foreign key (productidpaymentdetails) references product (id) on delete restrict on update restrict;

alter table person add constraint fk_person_picture_id foreign key (picture_id) references blob (id) on delete restrict on update restrict;

alter table person_role add constraint fk_person_role_person foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_person_role_person on person_role (person_id);

alter table person_role add constraint fk_person_role_role foreign key (role_id) references role (id) on delete restrict on update restrict;
create index ix_person_role_role on person_role (role_id);

alter table person_permission add constraint fk_person_permission_person foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_person_permission_person on person_permission (person_id);

alter table person_permission add constraint fk_person_permission_permission foreign key (permission_id) references permission (id) on delete restrict on update restrict;
create index ix_person_permission_permission on person_permission (permission_id);

alter table product add constraint fk_product_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;
create index ix_product_customer_id on product (customer_id);

alter table productextension add constraint fk_productextension_product_id foreign key (product_id) references product (id) on delete restrict on update restrict;
create index ix_productextension_product_id on productextension (product_id);

alter table productextension add constraint fk_productextension_tariff_included_id foreign key (tariff_included_id) references tariff (id) on delete restrict on update restrict;
create index ix_productextension_tariff_included_id on productextension (tariff_included_id);

alter table productextension add constraint fk_productextension_tariff_optional_id foreign key (tariff_optional_id) references tariff (id) on delete restrict on update restrict;
create index ix_productextension_tariff_optional_id on productextension (tariff_optional_id);

alter table project add constraint fk_project_product_id foreign key (product_id) references product (id) on delete restrict on update restrict;
create index ix_project_product_id on project (product_id);

alter table project_tag add constraint fk_project_tag_project foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_project_tag_project on project_tag (project_id);

alter table project_tag add constraint fk_project_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_project_tag_tag on project_tag (tag_id);

alter table projectparticipant add constraint fk_projectparticipant_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_projectparticipant_project_id on projectparticipant (project_id);

alter table projectparticipant add constraint fk_projectparticipant_person_id foreign key (person_id) references person (id) on delete restrict on update restrict;
create index ix_projectparticipant_person_id on projectparticipant (person_id);

alter table role_permission add constraint fk_role_permission_role foreign key (role_id) references role (id) on delete restrict on update restrict;
create index ix_role_permission_role on role_permission (role_id);

alter table role_permission add constraint fk_role_permission_permission foreign key (permission_id) references permission (id) on delete restrict on update restrict;
create index ix_role_permission_permission on role_permission (permission_id);

alter table updateprocedure add constraint fk_updateprocedure_instance_id foreign key (instance_id) references instancesnapshot (id) on delete restrict on update restrict;
create index ix_updateprocedure_instance_id on updateprocedure (instance_id);

alter table widget add constraint fk_widget_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_widget_project_id on widget (project_id);

alter table widget add constraint fk_widget_producer_id foreign key (producer_id) references producer (id) on delete restrict on update restrict;
create index ix_widget_producer_id on widget (producer_id);

alter table widget_tag add constraint fk_widget_tag_widget foreign key (widget_id) references widget (id) on delete restrict on update restrict;
create index ix_widget_tag_widget on widget_tag (widget_id);

alter table widget_tag add constraint fk_widget_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_widget_tag_tag on widget_tag (tag_id);

alter table widgetversion add constraint fk_widgetversion_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table widgetversion add constraint fk_widgetversion_widget_id foreign key (widget_id) references widget (id) on delete restrict on update restrict;
create index ix_widgetversion_widget_id on widgetversion (widget_id);


# --- !Downs

alter table if exists authorizationtoken drop constraint if exists fk_authorizationtoken_person_id;
drop index if exists ix_authorizationtoken_person_id;

alter table if exists bprogram drop constraint if exists fk_bprogram_project_id;
drop index if exists ix_bprogram_project_id;

alter table if exists bprogram_tag drop constraint if exists fk_bprogram_tag_bprogram;
drop index if exists ix_bprogram_tag_bprogram;

alter table if exists bprogram_tag drop constraint if exists fk_bprogram_tag_tag;
drop index if exists ix_bprogram_tag_tag;

alter table if exists bprogramversion drop constraint if exists fk_bprogramversion_file_id;

alter table if exists bprogramversion drop constraint if exists fk_bprogramversion_library_id;
drop index if exists ix_bprogramversion_library_id;

alter table if exists bprogramversion drop constraint if exists fk_bprogramversion_b_program_id;
drop index if exists ix_bprogramversion_b_program_id;

alter table if exists blob drop constraint if exists fk_blob_boot_loader_id;

alter table if exists block drop constraint if exists fk_block_project_id;
drop index if exists ix_block_project_id;

alter table if exists block drop constraint if exists fk_block_producer_id;
drop index if exists ix_block_producer_id;

alter table if exists block_tag drop constraint if exists fk_block_tag_block;
drop index if exists ix_block_tag_block;

alter table if exists block_tag drop constraint if exists fk_block_tag_tag;
drop index if exists ix_block_tag_tag;

alter table if exists blockversion drop constraint if exists fk_blockversion_file_id;

alter table if exists blockversion drop constraint if exists fk_blockversion_block_id;
drop index if exists ix_blockversion_block_id;

alter table if exists bootloader drop constraint if exists fk_bootloader_hardware_type_id;
drop index if exists ix_bootloader_hardware_type_id;

alter table if exists bootloader drop constraint if exists fk_bootloader_main_hardware_type_id;

alter table if exists cprogram drop constraint if exists fk_cprogram_project_id;
drop index if exists ix_cprogram_project_id;

alter table if exists cprogram drop constraint if exists fk_cprogram_hardware_type_id;
drop index if exists ix_cprogram_hardware_type_id;

alter table if exists cprogram drop constraint if exists fk_cprogram_hardware_type_default_id;

alter table if exists cprogram drop constraint if exists fk_cprogram_hardware_type_test_id;

alter table if exists cprogram drop constraint if exists fk_cprogram_example_library_id;
drop index if exists ix_cprogram_example_library_id;

alter table if exists cprogram_tag drop constraint if exists fk_cprogram_tag_cprogram;
drop index if exists ix_cprogram_tag_cprogram;

alter table if exists cprogram_tag drop constraint if exists fk_cprogram_tag_tag;
drop index if exists ix_cprogram_tag_tag;

alter table if exists cprogramversion drop constraint if exists fk_cprogramversion_file_id;

alter table if exists cprogramversion drop constraint if exists fk_cprogramversion_c_program_id;
drop index if exists ix_cprogramversion_c_program_id;

alter table if exists cprogramversion drop constraint if exists fk_cprogramversion_default_program_id;

alter table if exists changepropertytoken drop constraint if exists fk_changepropertytoken_person_id;

alter table if exists compilation drop constraint if exists fk_compilation_c_compilation_version;

alter table if exists compilation drop constraint if exists fk_compilation_bin_compilation_file_id;

alter table if exists employee drop constraint if exists fk_employee_person_id;
drop index if exists ix_employee_person_id;

alter table if exists employee drop constraint if exists fk_employee_customer_id;
drop index if exists ix_employee_customer_id;

alter table if exists gridprogram drop constraint if exists fk_gridprogram_grid_project_id;
drop index if exists ix_gridprogram_grid_project_id;

alter table if exists gridprogram_tag drop constraint if exists fk_gridprogram_tag_gridprogram;
drop index if exists ix_gridprogram_tag_gridprogram;

alter table if exists gridprogram_tag drop constraint if exists fk_gridprogram_tag_tag;
drop index if exists ix_gridprogram_tag_tag;

alter table if exists gridprogramversion drop constraint if exists fk_gridprogramversion_file_id;

alter table if exists gridprogramversion drop constraint if exists fk_gridprogramversion_grid_program_id;
drop index if exists ix_gridprogramversion_grid_program_id;

alter table if exists gridproject drop constraint if exists fk_gridproject_project_id;
drop index if exists ix_gridproject_project_id;

alter table if exists gridproject_tag drop constraint if exists fk_gridproject_tag_gridproject;
drop index if exists ix_gridproject_tag_gridproject;

alter table if exists gridproject_tag drop constraint if exists fk_gridproject_tag_tag;
drop index if exists ix_gridproject_tag_tag;

alter table if exists gridterminal drop constraint if exists fk_gridterminal_person_id;
drop index if exists ix_gridterminal_person_id;

alter table if exists hardware drop constraint if exists fk_hardware_hardware_type_id;
drop index if exists ix_hardware_hardware_type_id;

alter table if exists hardware drop constraint if exists fk_hardware_actual_c_program_version_id;
drop index if exists ix_hardware_actual_c_program_version_id;

alter table if exists hardware drop constraint if exists fk_hardware_actual_backup_c_program_version_id;
drop index if exists ix_hardware_actual_backup_c_program_version_id;

alter table if exists hardware drop constraint if exists fk_hardware_actual_boot_loader_id;
drop index if exists ix_hardware_actual_boot_loader_id;

alter table if exists hardware drop constraint if exists fk_hardware_picture_id;

alter table if exists hardware drop constraint if exists fk_hardware_project_id;
drop index if exists ix_hardware_project_id;

alter table if exists hardware_tag drop constraint if exists fk_hardware_tag_hardware;
drop index if exists ix_hardware_tag_hardware;

alter table if exists hardware_tag drop constraint if exists fk_hardware_tag_tag;
drop index if exists ix_hardware_tag_tag;

alter table if exists hardware_hardwaregroup drop constraint if exists fk_hardware_hardwaregroup_hardware;
drop index if exists ix_hardware_hardwaregroup_hardware;

alter table if exists hardware_hardwaregroup drop constraint if exists fk_hardware_hardwaregroup_hardwaregroup;
drop index if exists ix_hardware_hardwaregroup_hardwaregroup;

alter table if exists hardwarefeature_hardwaretype drop constraint if exists fk_hardwarefeature_hardwaretype_hardwarefeature;
drop index if exists ix_hardwarefeature_hardwaretype_hardwarefeature;

alter table if exists hardwarefeature_hardwaretype drop constraint if exists fk_hardwarefeature_hardwaretype_hardwaretype;
drop index if exists ix_hardwarefeature_hardwaretype_hardwaretype;

alter table if exists hardwaregroup drop constraint if exists fk_hardwaregroup_project_id;
drop index if exists ix_hardwaregroup_project_id;

alter table if exists hardwaretype drop constraint if exists fk_hardwaretype_producer_id;
drop index if exists ix_hardwaretype_producer_id;

alter table if exists hardwaretype drop constraint if exists fk_hardwaretype_processor_id;
drop index if exists ix_hardwaretype_processor_id;

alter table if exists hardwaretype drop constraint if exists fk_hardwaretype_picture_id;

alter table if exists hardwareupdate drop constraint if exists fk_hardwareupdate_actualization_procedure_id;
drop index if exists ix_hardwareupdate_actualization_procedure_id;

alter table if exists hardwareupdate drop constraint if exists fk_hardwareupdate_hardware_id;
drop index if exists ix_hardwareupdate_hardware_id;

alter table if exists hardwareupdate drop constraint if exists fk_hardwareupdate_c_program_version_for_update_id;
drop index if exists ix_hardwareupdate_c_program_version_for_update_id;

alter table if exists hardwareupdate drop constraint if exists fk_hardwareupdate_bootloader_id;
drop index if exists ix_hardwareupdate_bootloader_id;

alter table if exists hardwareupdate drop constraint if exists fk_hardwareupdate_binary_file_id;
drop index if exists ix_hardwareupdate_binary_file_id;

alter table if exists homerserver drop constraint if exists fk_homerserver_project_id;
drop index if exists ix_homerserver_project_id;

alter table if exists instance drop constraint if exists fk_instance_server_main_id;
drop index if exists ix_instance_server_main_id;

alter table if exists instance drop constraint if exists fk_instance_server_backup_id;
drop index if exists ix_instance_server_backup_id;

alter table if exists instance drop constraint if exists fk_instance_project_id;
drop index if exists ix_instance_project_id;

alter table if exists instance drop constraint if exists fk_instance_b_program_id;
drop index if exists ix_instance_b_program_id;

alter table if exists instance_tag drop constraint if exists fk_instance_tag_instance;
drop index if exists ix_instance_tag_instance;

alter table if exists instance_tag drop constraint if exists fk_instance_tag_tag;
drop index if exists ix_instance_tag_tag;

alter table if exists instancesnapshot drop constraint if exists fk_instancesnapshot_instance_id;
drop index if exists ix_instancesnapshot_instance_id;

alter table if exists instancesnapshot drop constraint if exists fk_instancesnapshot_b_program_version_id;
drop index if exists ix_instancesnapshot_b_program_version_id;

alter table if exists instancesnapshot drop constraint if exists fk_instancesnapshot_program_id;

alter table if exists invitation drop constraint if exists fk_invitation_owner_id;
drop index if exists ix_invitation_owner_id;

alter table if exists invitation drop constraint if exists fk_invitation_project_id;
drop index if exists ix_invitation_project_id;

alter table if exists invoice drop constraint if exists fk_invoice_product_id;
drop index if exists ix_invoice_product_id;

alter table if exists invoiceitem drop constraint if exists fk_invoiceitem_invoice_id;
drop index if exists ix_invoiceitem_invoice_id;

alter table if exists library drop constraint if exists fk_library_project_id;
drop index if exists ix_library_project_id;

alter table if exists library_tag drop constraint if exists fk_library_tag_library;
drop index if exists ix_library_tag_library;

alter table if exists library_tag drop constraint if exists fk_library_tag_tag;
drop index if exists ix_library_tag_tag;

alter table if exists library_hardwaretype drop constraint if exists fk_library_hardwaretype_library;
drop index if exists ix_library_hardwaretype_library;

alter table if exists library_hardwaretype drop constraint if exists fk_library_hardwaretype_hardwaretype;
drop index if exists ix_library_hardwaretype_hardwaretype;

alter table if exists libraryversion drop constraint if exists fk_libraryversion_file_id;

alter table if exists libraryversion drop constraint if exists fk_libraryversion_library_id;
drop index if exists ix_libraryversion_library_id;

alter table if exists log drop constraint if exists fk_log_file_id;

alter table if exists mprograminstanceparameter drop constraint if exists fk_mprograminstanceparameter_grid_project_program_snapsho_1;
drop index if exists ix_mprograminstanceparameter_grid_project_program_snapsho_1;

alter table if exists mprograminstanceparameter drop constraint if exists fk_mprograminstanceparameter_grid_program_version_id;
drop index if exists ix_mprograminstanceparameter_grid_program_version_id;

alter table if exists mprojectprogramsnapshot drop constraint if exists fk_mprojectprogramsnapshot_grid_project_id;
drop index if exists ix_mprojectprogramsnapshot_grid_project_id;

alter table if exists b_program_version_snapshots drop constraint if exists fk_b_program_version_snapshots_mprojectprogramsnapshot;
drop index if exists ix_b_program_version_snapshots_mprojectprogramsnapshot;

alter table if exists b_program_version_snapshots drop constraint if exists fk_b_program_version_snapshots_bprogramversion;
drop index if exists ix_b_program_version_snapshots_bprogramversion;

alter table if exists notification drop constraint if exists fk_notification_person_id;
drop index if exists ix_notification_person_id;

alter table if exists passwordrecoverytoken drop constraint if exists fk_passwordrecoverytoken_person_id;

alter table if exists paymentdetails drop constraint if exists fk_paymentdetails_customer_id;

alter table if exists paymentdetails drop constraint if exists fk_paymentdetails_productidpaymentdetails;

alter table if exists person drop constraint if exists fk_person_picture_id;

alter table if exists person_role drop constraint if exists fk_person_role_person;
drop index if exists ix_person_role_person;

alter table if exists person_role drop constraint if exists fk_person_role_role;
drop index if exists ix_person_role_role;

alter table if exists person_permission drop constraint if exists fk_person_permission_person;
drop index if exists ix_person_permission_person;

alter table if exists person_permission drop constraint if exists fk_person_permission_permission;
drop index if exists ix_person_permission_permission;

alter table if exists product drop constraint if exists fk_product_customer_id;
drop index if exists ix_product_customer_id;

alter table if exists productextension drop constraint if exists fk_productextension_product_id;
drop index if exists ix_productextension_product_id;

alter table if exists productextension drop constraint if exists fk_productextension_tariff_included_id;
drop index if exists ix_productextension_tariff_included_id;

alter table if exists productextension drop constraint if exists fk_productextension_tariff_optional_id;
drop index if exists ix_productextension_tariff_optional_id;

alter table if exists project drop constraint if exists fk_project_product_id;
drop index if exists ix_project_product_id;

alter table if exists project_tag drop constraint if exists fk_project_tag_project;
drop index if exists ix_project_tag_project;

alter table if exists project_tag drop constraint if exists fk_project_tag_tag;
drop index if exists ix_project_tag_tag;

alter table if exists projectparticipant drop constraint if exists fk_projectparticipant_project_id;
drop index if exists ix_projectparticipant_project_id;

alter table if exists projectparticipant drop constraint if exists fk_projectparticipant_person_id;
drop index if exists ix_projectparticipant_person_id;

alter table if exists role_permission drop constraint if exists fk_role_permission_role;
drop index if exists ix_role_permission_role;

alter table if exists role_permission drop constraint if exists fk_role_permission_permission;
drop index if exists ix_role_permission_permission;

alter table if exists updateprocedure drop constraint if exists fk_updateprocedure_instance_id;
drop index if exists ix_updateprocedure_instance_id;

alter table if exists widget drop constraint if exists fk_widget_project_id;
drop index if exists ix_widget_project_id;

alter table if exists widget drop constraint if exists fk_widget_producer_id;
drop index if exists ix_widget_producer_id;

alter table if exists widget_tag drop constraint if exists fk_widget_tag_widget;
drop index if exists ix_widget_tag_widget;

alter table if exists widget_tag drop constraint if exists fk_widget_tag_tag;
drop index if exists ix_widget_tag_tag;

alter table if exists widgetversion drop constraint if exists fk_widgetversion_file_id;

alter table if exists widgetversion drop constraint if exists fk_widgetversion_widget_id;
drop index if exists ix_widgetversion_widget_id;

drop table if exists authorizationtoken cascade;

drop table if exists bprogram cascade;

drop table if exists bprogram_tag cascade;

drop table if exists bprogramversion cascade;

drop table if exists blob cascade;

drop table if exists block cascade;

drop table if exists block_tag cascade;

drop table if exists blockversion cascade;

drop table if exists bootloader cascade;

drop table if exists cprogram cascade;

drop table if exists cprogram_tag cascade;

drop table if exists cprogramversion cascade;

drop table if exists changepropertytoken cascade;

drop table if exists compilation cascade;

drop table if exists compilationserver cascade;

drop table if exists customer cascade;

drop table if exists employee cascade;

drop table if exists garfield cascade;

drop table if exists gridprogram cascade;

drop table if exists gridprogram_tag cascade;

drop table if exists gridprogramversion cascade;

drop table if exists gridproject cascade;

drop table if exists gridproject_tag cascade;

drop table if exists gridterminal cascade;

drop table if exists hardware cascade;

drop table if exists hardware_tag cascade;

drop table if exists hardware_hardwaregroup cascade;

drop table if exists hardwarefeature cascade;

drop table if exists hardwarefeature_hardwaretype cascade;

drop table if exists hardwaregroup cascade;

drop table if exists hardwaretype cascade;

drop table if exists hardwareupdate cascade;

drop table if exists homerserver cascade;

drop table if exists instance cascade;

drop table if exists instance_tag cascade;

drop table if exists instancesnapshot cascade;

drop table if exists invitation cascade;

drop table if exists invoice cascade;

drop table if exists invoiceitem cascade;

drop table if exists library cascade;

drop table if exists library_tag cascade;

drop table if exists library_hardwaretype cascade;

drop table if exists libraryversion cascade;

drop table if exists log cascade;

drop table if exists mprograminstanceparameter cascade;

drop table if exists mprojectprogramsnapshot cascade;

drop table if exists b_program_version_snapshots cascade;

drop table if exists notification cascade;

drop table if exists passwordrecoverytoken cascade;

drop table if exists paymentdetails cascade;

drop table if exists permission cascade;

drop table if exists person cascade;

drop table if exists person_role cascade;

drop table if exists person_permission cascade;

drop table if exists processor cascade;

drop table if exists producer cascade;

drop table if exists product cascade;

drop table if exists productextension cascade;

drop table if exists project cascade;

drop table if exists project_tag cascade;

drop table if exists projectparticipant cascade;

drop table if exists role cascade;

drop table if exists role_permission cascade;

drop table if exists servererror cascade;

drop table if exists tag cascade;

drop table if exists tariff cascade;

drop table if exists updateprocedure cascade;

drop table if exists validationtoken cascade;

drop table if exists widget cascade;

drop table if exists widget_tag cascade;

drop table if exists widgetversion cascade;

