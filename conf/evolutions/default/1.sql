# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ActualizationProcedure (
  id                        varchar(40) not null,
  state                     varchar(19),
  homer_instance_record_id  varchar(255),
  date_of_create            timestamp,
  date_of_planing           timestamp,
  date_of_finish            timestamp,
  type_of_update            varchar(41),
  constraint ck_ActualizationProcedure_state check (state in ('complete_with_error','canceled','in_progress','successful_complete','complete','not_start_yet')),
  constraint ck_ActualizationProcedure_type_of_update check (type_of_update in ('AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE','AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE','MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME','MANUALLY_BY_USER','MANUALLY_BY_USER_BLOCKO_GROUP')),
  constraint pk_ActualizationProcedure primary key (id))
;

create table BPair (
  id                        varchar(40) not null,
  c_program_version_id      varchar(255),
  board_id                  varchar(255),
  device_board_pair_id      varchar(40),
  main_board_pair_id        varchar(40),
  constraint uq_BPair_main_board_pair_id unique (main_board_pair_id),
  constraint pk_BPair primary key (id))
;

create table BProgram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  instance_id               varchar(255),
  last_update               timestamp,
  date_of_create            timestamp,
  project_id                varchar(255),
  removed_by_user           boolean,
  azure_b_program_link      varchar(255),
  constraint uq_BProgram_instance_id unique (instance_id),
  constraint pk_BProgram primary key (id))
;

create table BProgramHwGroup (
  id                        varchar(40) not null,
  constraint pk_BProgramHwGroup primary key (id))
;

create table BlockoBlock (
  id                        varchar(40) not null,
  name                      varchar(255),
  description               TEXT,
  author_id                 varchar(255),
  type_of_block_id          varchar(255),
  producer_id               varchar(40),
  order_position            integer,
  removed_by_user           boolean,
  publish_type              varchar(20),
  active                    boolean,
  constraint ck_BlockoBlock_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_BlockoBlock primary key (id))
;

create table BlockoBlockVersion (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  design_json               TEXT,
  logic_json                TEXT,
  approval_state            varchar(11),
  publish_type              varchar(20),
  removed_by_user           boolean,
  author_id                 varchar(255),
  date_of_create            timestamp,
  blocko_block_id           varchar(40),
  constraint ck_BlockoBlockVersion_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint ck_BlockoBlockVersion_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_BlockoBlockVersion primary key (id))
;

create table Board (
  id                        varchar(255) not null,
  hash_for_adding           varchar(255),
  wifi_mac_address          varchar(255),
  mac_address               varchar(255),
  name                      varchar(255),
  description               TEXT,
  picture_id                varchar(255),
  date_of_user_registration timestamp,
  date_of_create            timestamp,
  batch                     varchar(255),
  ean_number                bigint,
  is_active                 boolean,
  developer_kit             boolean,
  backup_mode               boolean,
  database_synchronize      boolean,
  web_view                  boolean,
  web_port                  integer,
  type_of_board_id          varchar(255),
  project_id                varchar(255),
  actual_c_program_version_id varchar(255),
  actual_backup_c_program_version_id varchar(255),
  actual_boot_loader_id     varchar(40),
  connected_server_id       varchar(255),
  connected_instance_id     varchar(255),
  constraint uq_Board_picture_id unique (picture_id),
  constraint pk_Board primary key (id))
;

create table BootLoader (
  id                        varchar(40) not null,
  date_of_create            timestamp,
  name                      varchar(255),
  description               TEXT,
  version_identificator     varchar(255),
  changing_note             TEXT,
  type_of_board_id          varchar(255),
  main_type_of_board_id     varchar(255),
  azure_product_link        varchar(255),
  constraint uq_BootLoader_main_type_of_board unique (main_type_of_board_id),
  constraint pk_BootLoader primary key (id))
;

create table CCompilation (
  id                        varchar(40) not null,
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
  constraint ck_CCompilation_status check (status in ('file_with_code_not_found','json_code_is_broken','successfully_compiled_and_restored','compilation_in_progress','compilation_server_error','hardware_unstable','server_was_offline','successfully_compiled_not_restored','compiled_with_code_errors','undefined')),
  constraint uq_CCompilation_c_compilation_ve unique (c_compilation_version),
  constraint uq_CCompilation_bin_compilation_ unique (bin_compilation_file_id),
  constraint pk_CCompilation primary key (id))
;

create table CProgram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  type_of_board_id          varchar(255),
  publish_type              varchar(20),
  date_of_create            timestamp,
  removed_by_user           boolean,
  type_of_board_default_id  varchar(255),
  type_of_board_test_id     varchar(255),
  example_library_id        varchar(255),
  azure_c_program_link      varchar(255),
  constraint ck_CProgram_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint uq_CProgram_type_of_board_defaul unique (type_of_board_default_id),
  constraint uq_CProgram_type_of_board_test_i unique (type_of_board_test_id),
  constraint pk_CProgram primary key (id))
;

create table CProgramUpdatePlan (
  id                        varchar(40) not null,
  actualization_procedure_id varchar(40),
  date_of_create            timestamp,
  date_of_finish            timestamp,
  board_id                  varchar(255),
  firmware_type             varchar(10),
  c_program_version_for_update_id varchar(255),
  bootloader_id             varchar(40),
  binary_file_id            varchar(255),
  state                     varchar(23),
  count_of_tries            integer,
  error                     varchar(255),
  error_code                integer,
  constraint ck_CProgramUpdatePlan_firmware_type check (firmware_type in ('BACKUP','FIRMWARE','BOOTLOADER','WIFI')),
  constraint ck_CProgramUpdatePlan_state check (state in ('canceled','in_progress','waiting_for_device','overwritten','bin_file_not_found','not_updated','homer_server_is_offline','complete','instance_inaccessible','critical_error','not_start_yet')),
  constraint pk_CProgramUpdatePlan primary key (id))
;

create table ChangePropertyToken (
  change_property_token     varchar(255) not null,
  person_id                 varchar(255),
  time_of_creation          timestamp,
  property                  varchar(255),
  value                     varchar(255),
  constraint uq_ChangePropertyToken_person_id unique (person_id),
  constraint pk_ChangePropertyToken primary key (change_property_token))
;

create table CompilationServer (
  id                        varchar(40) not null,
  personal_server_name      varchar(255),
  connection_identificator  varchar(255),
  hash_certificate          varchar(255),
  date_of_create            timestamp,
  server_url                varchar(255),
  constraint uq_CompilationServer_personal_se unique (personal_server_name),
  constraint uq_CompilationServer_server_url unique (server_url),
  constraint pk_CompilationServer primary key (id))
;

create table Customer (
  id                        varchar(40) not null,
  created                   timestamp,
  fakturoid_subject_id      varchar(255),
  removed_by_user           boolean,
  constraint pk_Customer primary key (id))
;

create table Employee (
  id                        varchar(40) not null,
  created                   timestamp,
  state                     varchar(7),
  person_id                 varchar(255),
  customer_id               varchar(40),
  constraint ck_Employee_state check (state in ('owner','member','invited','admin')),
  constraint pk_Employee primary key (id))
;

create table FileRecord (
  id                        varchar(255) not null,
  file_name                 varchar(255),
  file_path                 varchar(255),
  boot_loader_id            varchar(40),
  version_object_id         varchar(255),
  constraint uq_FileRecord_boot_loader_id unique (boot_loader_id),
  constraint pk_FileRecord primary key (id))
;

create table FloatingPersonToken (
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
  constraint ck_FloatingPersonToken_where_logged check (where_logged in ('E_STORE','HOMER_SERVER','BECKI_WEBSITE')),
  constraint pk_FloatingPersonToken primary key (connection_id))
;

create table Garfield (
  id                        varchar(40) not null,
  name                      varchar(255),
  description               varchar(255),
  hardware_tester_id        varchar(255),
  print_label_id_1          integer,
  print_label_id_2          integer,
  print_sticker_id          integer,
  type_of_board_id          varchar(255),
  producer_id               varchar(255),
  date_of_crate             timestamp,
  constraint uq_Garfield_hardware_tester_id unique (hardware_tester_id),
  constraint pk_Garfield primary key (id))
;

create table GridTerminal (
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
  constraint pk_GridTerminal primary key (terminal_token))
;

create table GridWidget (
  id                        varchar(40) not null,
  name                      varchar(255),
  description               TEXT,
  order_position            integer,
  removed_by_user           boolean,
  author_id                 varchar(255),
  type_of_widget_id         varchar(255),
  producer_id               varchar(40),
  publish_type              varchar(20),
  active                    boolean,
  constraint ck_GridWidget_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_GridWidget primary key (id))
;

create table GridWidgetVersion (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       varchar(255),
  design_json               TEXT,
  logic_json                TEXT,
  approval_state            varchar(11),
  publish_type              varchar(20),
  removed_by_user           boolean,
  author_id                 varchar(255),
  date_of_create            timestamp,
  grid_widget_id            varchar(40),
  constraint ck_GridWidgetVersion_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint ck_GridWidgetVersion_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_GridWidgetVersion primary key (id))
;

create table HomerInstance (
  id                        varchar(255) not null,
  cloud_homer_server_id     varchar(40),
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  instance_type             varchar(10),
  removed_by_user           boolean,
  constraint ck_HomerInstance_instance_type check (instance_type in ('INDIVIDUAL','VIRTUAL')),
  constraint pk_HomerInstance primary key (id))
;

create table HomerInstanceRecord (
  id                        varchar(255) not null,
  main_instance_history_id  varchar(255),
  date_of_created           timestamp,
  running_from              timestamp,
  running_to                timestamp,
  planed_when               timestamp,
  version_object_id         varchar(255),
  actual_running_instance_id varchar(255),
  constraint uq_HomerInstanceRecord_actual_ru unique (actual_running_instance_id),
  constraint pk_HomerInstanceRecord primary key (id))
;

create table HomerServer (
  id                        varchar(40) not null,
  connection_identificator  varchar(255),
  hash_certificate          varchar(255),
  date_of_create            timestamp,
  personal_server_name      varchar(255),
  json_additional_parameter TEXT,
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
  log_level                 varchar(13),
  constraint ck_HomerServer_server_type check (server_type in ('main_server','test_server','private_server','backup_server','public_server')),
  constraint ck_HomerServer_log_level check (log_level in ('warn','trace','debug','error_message','info')),
  constraint pk_HomerServer primary key (id))
;

create table Invitation (
  id                        varchar(255) not null,
  owner_id                  varchar(255),
  project_id                varchar(255),
  mail                      varchar(255),
  date_of_creation          timestamp,
  notification_id           varchar(255),
  constraint pk_Invitation primary key (id))
;

create table Invoice (
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
  constraint ck_Invoice_status check (status in ('canceled','overdue','pending','paid')),
  constraint ck_Invoice_method check (method in ('credit_card','bank_transfer','free')),
  constraint ck_Invoice_warning check (warning in ('none','first','zero_balance','deactivation','second')),
  constraint pk_Invoice primary key (id))
;

create table InvoiceItem (
  id                        bigint not null,
  invoice_id                varchar(255),
  name                      varchar(255),
  quantity                  bigint,
  unit_name                 varchar(255),
  unit_price                bigint,
  currency                  varchar(12),
  constraint ck_InvoiceItem_currency check (currency in ('eur','czk','price_in_usd')),
  constraint pk_InvoiceItem primary key (id))
;

create table Library (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  removed_by_user           boolean,
  project_id                varchar(255),
  date_of_create            timestamp,
  publish_type              varchar(20),
  azure_library_link        varchar(255),
  constraint ck_Library_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_Library primary key (id))
;

create table Log (
  id                        varchar(255) not null,
  name                      varchar(255),
  created                   timestamp,
  type                      varchar(255),
  file_id                   varchar(255),
  constraint uq_Log_file_id unique (file_id),
  constraint pk_Log primary key (id))
;

create table LoggyError (
  id                        varchar(255) not null,
  summary                   TEXT,
  description               TEXT,
  stack_trace               TEXT,
  cause                     TEXT,
  youtrack_url              varchar(255),
  repetition                bigint,
  created                   timestamp,
  constraint pk_LoggyError primary key (id))
;

create table MProgram (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  removed_by_user           boolean,
  date_of_create            timestamp,
  m_project_id              varchar(255),
  azure_m_program_link      varchar(255),
  constraint pk_MProgram primary key (id))
;

create table MProgramInstanceParameter (
  id                        varchar(40) not null,
  m_project_program_snapshot_id varchar(40),
  m_program_version_id      varchar(255),
  connection_token          varchar(255),
  snapshot_settings         varchar(24),
  constraint ck_MProgramInstanceParameter_snapshot_settings check (snapshot_settings in ('only_for_project_members','not_in_instance','absolutely_public')),
  constraint pk_MProgramInstanceParameter primary key (id))
;

create table MProject (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  date_of_create            timestamp,
  project_id                varchar(255),
  removed_by_user           boolean,
  azure_m_project_link      varchar(255),
  constraint pk_MProject primary key (id))
;

create table MProjectProgramSnapShot (
  id                        varchar(40) not null,
  m_project_id              varchar(255),
  constraint pk_MProjectProgramSnapShot primary key (id))
;

create table Notification (
  id                        varchar(255) not null,
  notification_level        varchar(13),
  notification_importance   varchar(6),
  state                     varchar(11),
  content_string            TEXT,
  buttons_string            TEXT,
  confirmation_required     boolean,
  confirmed                 boolean,
  was_read                  boolean,
  created                   timestamp,
  person_id                 varchar(255),
  constraint ck_Notification_notification_level check (notification_level in ('success','warning','error_message','info')),
  constraint ck_Notification_notification_importance check (notification_importance in ('normal','high','low')),
  constraint ck_Notification_state check (state in ('unconfirmed','deleted','created','updated')),
  constraint pk_Notification primary key (id))
;

create table PasswordRecoveryToken (
  id                        varchar(255) not null,
  person_id                 varchar(255),
  password_recovery_token   varchar(255),
  time_of_creation          timestamp,
  constraint uq_PasswordRecoveryToken_person_ unique (person_id),
  constraint pk_PasswordRecoveryToken primary key (id))
;

create table PaymentDetails (
  id                        bigint not null,
  customer_id               varchar(40),
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
  bank_account              varchar(255),
  constraint uq_PaymentDetails_customer_id unique (customer_id),
  constraint uq_PaymentDetails_productidpayme unique (productidpaymentdetails),
  constraint pk_PaymentDetails primary key (id))
;

create table Permission (
  permission_key            varchar(255) not null,
  description               varchar(255),
  constraint pk_Permission primary key (permission_key))
;

create table Person (
  id                        varchar(255) not null,
  mail                      varchar(255),
  nick_name                 varchar(255),
  full_name                 varchar(255),
  country                   varchar(255),
  gender                    varchar(255),
  alternative_picture_link  varchar(255),
  picture_id                varchar(255),
  freeze_account            boolean,
  mail_validated            boolean,
  facebook_oauth_id         varchar(255),
  github_oauth_id           varchar(255),
  sha_password              bytea,
  constraint uq_Person_mail unique (mail),
  constraint uq_Person_nick_name unique (nick_name),
  constraint uq_Person_picture_id unique (picture_id),
  constraint pk_Person primary key (id))
;

create table Processor (
  id                        varchar(40) not null,
  processor_name            varchar(255),
  description               TEXT,
  processor_code            varchar(255),
  speed                     integer,
  removed_by_user           boolean,
  constraint pk_Processor primary key (id))
;

create table Producer (
  id                        varchar(40) not null,
  name                      varchar(255),
  description               TEXT,
  removed_by_user           boolean,
  constraint pk_Producer primary key (id))
;

create table Product (
  id                        varchar(255) not null,
  name                      varchar(255),
  method                    varchar(13),
  business_model            varchar(11),
  subscription_id           varchar(255),
  fakturoid_subject_id      varchar(255),
  gopay_id                  bigint,
  active                    boolean,
  created                   timestamp,
  on_demand                 boolean,
  credit                    bigint,
  financial_history         TEXT,
  configuration             TEXT,
  removed_byinvoi_user      boolean,
  client_billing            boolean,
  client_billing_invoice_parameters varchar(255),
  customer_id               varchar(40),
  azure_product_link        varchar(255),
  constraint ck_Product_method check (method in ('credit_card','bank_transfer','free')),
  constraint ck_Product_business_model check (business_model in ('saas','alpha','fee','integration','integrator','cal')),
  constraint pk_Product primary key (id))
;

create table ProductExtension (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  color                     varchar(255),
  type                      varchar(12),
  configuration             TEXT,
  order_position            integer,
  active                    boolean,
  removed                   boolean,
  created                   timestamp,
  product_id                varchar(255),
  tariff_included_id        varchar(255),
  tariff_optional_id        varchar(255),
  constraint ck_ProductExtension_type check (type in ('database','instance','log','rest_api','project','support','homer_server','participant')),
  constraint pk_ProductExtension primary key (id))
;

create table Project (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  removed_by_user           boolean,
  product_id                varchar(255),
  blob_project_link         varchar(255),
  constraint pk_Project primary key (id))
;

create table ProjectParticipant (
  id                        varchar(255) not null,
  project_id                varchar(255),
  person_id                 varchar(255),
  state                     varchar(7),
  constraint ck_ProjectParticipant_state check (state in ('owner','member','invited','admin')),
  constraint pk_ProjectParticipant primary key (id))
;

create table RequestLog (
  id                        varchar(40) not null,
  request                   varchar(255),
  call_count                bigint,
  date_of_create            timestamp,
  constraint uq_RequestLog_request unique (request),
  constraint pk_RequestLog primary key (id))
;

create table SecurityRole (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_SecurityRole primary key (id))
;

create table Tariff (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  identifier                varchar(255),
  active                    boolean,
  business_model            varchar(11),
  order_position            integer,
  company_details_required  boolean,
  payment_details_required  boolean,
  payment_method_required   boolean,
  credit_for_beginning      bigint,
  color                     varchar(255),
  awesome_icon              varchar(255),
  labels_json               varchar(255),
  constraint ck_Tariff_business_model check (business_model in ('saas','alpha','fee','integration','integrator','cal')),
  constraint uq_Tariff_identifier unique (identifier),
  constraint pk_Tariff primary key (id))
;

create table TypeOfBlock (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  order_position            integer,
  removed_by_user           boolean,
  active                    boolean,
  publish_type              varchar(20),
  constraint ck_TypeOfBlock_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_TypeOfBlock primary key (id))
;

create table TypeOfBoard (
  id                        varchar(255) not null,
  name                      varchar(255),
  compiler_target_name      varchar(255),
  revision                  varchar(255),
  description               TEXT,
  producer_id               varchar(40),
  processor_id              varchar(40),
  connectible_to_internet   boolean,
  picture_id                varchar(255),
  removed_by_user           boolean,
  constraint uq_TypeOfBoard_compiler_target_n unique (compiler_target_name),
  constraint uq_TypeOfBoard_picture_id unique (picture_id),
  constraint pk_TypeOfBoard primary key (id))
;

create table BoardFeature (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_BoardFeature primary key (id))
;

create table TypeOfBoardBatch (
  id                        varchar(40) not null,
  revision                  varchar(255),
  production_batch          varchar(255),
  date_of_assembly          varchar(255),
  pcb_manufacture_name      varchar(255),
  pcb_manufacture_id        varchar(255),
  assembly_manufacture_name varchar(255),
  assembly_manufacture_id   varchar(255),
  customer_product_name     varchar(255),
  customer_company_name     varchar(255),
  customer_company_made_description varchar(255),
  mac_address_start         bigint,
  mac_address_end           bigint,
  latest_used_mac_address   bigint,
  ean_number                bigint,
  type_of_board_id          varchar(255),
  removed_by_user           boolean,
  constraint pk_TypeOfBoardBatch primary key (id))
;

create table TypeOfWidget (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               TEXT,
  project_id                varchar(255),
  order_position            integer,
  removed_by_user           boolean,
  publish_type              varchar(20),
  active                    boolean,
  constraint ck_TypeOfWidget_publish_type check (publish_type in ('default_main_program','public_program','private_program','default_version','default_test_program')),
  constraint pk_TypeOfWidget primary key (id))
;

create table ValidationToken (
  person_email              varchar(255) not null,
  auth_token                varchar(255),
  created                   timestamp,
  constraint pk_ValidationToken primary key (person_email))
;

create table VersionObject (
  id                        varchar(255) not null,
  version_name              varchar(255),
  version_description       TEXT,
  public_version            boolean,
  removed_by_user           boolean,
  date_of_create            timestamp,
  author_id                 varchar(255),
  library_id                varchar(255),
  c_program_id              varchar(255),
  approval_state            varchar(11),
  default_program_id        varchar(255),
  b_program_id              varchar(255),
  m_program_id              varchar(255),
  m_program_virtual_input_output TEXT,
  blob_version_link         varchar(255),
  constraint ck_VersionObject_approval_state check (approval_state in ('approved','edited','pending','disapproved')),
  constraint uq_VersionObject_default_program unique (default_program_id),
  constraint pk_VersionObject primary key (id))
;


create table Library_TypeOfBoard (
  Library_id                     varchar(255) not null,
  TypeOfBoard_id                 varchar(255) not null,
  constraint pk_Library_TypeOfBoard primary key (Library_id, TypeOfBoard_id))
;

create table b_program_version_snapshots (
  MProjectProgramSnapShot_id     varchar(40) not null,
  VersionObject_id               varchar(255) not null,
  constraint pk_b_program_version_snapshots primary key (MProjectProgramSnapShot_id, VersionObject_id))
;

create table Person_SecurityRole (
  Person_id                      varchar(255) not null,
  SecurityRole_id                varchar(255) not null,
  constraint pk_Person_SecurityRole primary key (Person_id, SecurityRole_id))
;

create table Person_Permission (
  Person_id                      varchar(255) not null,
  Permission_permission_key      varchar(255) not null,
  constraint pk_Person_Permission primary key (Person_id, Permission_permission_key))
;

create table SecurityRole_Permission (
  SecurityRole_id                varchar(255) not null,
  Permission_permission_key      varchar(255) not null,
  constraint pk_SecurityRole_Permission primary key (SecurityRole_id, Permission_permission_key))
;

create table BoardFeature_TypeOfBoard (
  BoardFeature_id                varchar(255) not null,
  TypeOfBoard_id                 varchar(255) not null,
  constraint pk_BoardFeature_TypeOfBoard primary key (BoardFeature_id, TypeOfBoard_id))
;

create table VersionObject_BProgramHwGroup (
  VersionObject_id               varchar(255) not null,
  BProgramHwGroup_id             varchar(40) not null,
  constraint pk_VersionObject_BProgramHwGroup primary key (VersionObject_id, BProgramHwGroup_id))
;
create sequence InvoiceItem_seq;

create sequence PaymentDetails_seq;

alter table ActualizationProcedure add constraint fk_ActualizationProcedure_home_1 foreign key (homer_instance_record_id) references HomerInstanceRecord (id);
create index ix_ActualizationProcedure_home_1 on ActualizationProcedure (homer_instance_record_id);
alter table BPair add constraint fk_BPair_c_program_version_2 foreign key (c_program_version_id) references VersionObject (id);
create index ix_BPair_c_program_version_2 on BPair (c_program_version_id);
alter table BPair add constraint fk_BPair_board_3 foreign key (board_id) references Board (id);
create index ix_BPair_board_3 on BPair (board_id);
alter table BPair add constraint fk_BPair_device_board_pair_4 foreign key (device_board_pair_id) references BProgramHwGroup (id);
create index ix_BPair_device_board_pair_4 on BPair (device_board_pair_id);
alter table BPair add constraint fk_BPair_main_board_pair_5 foreign key (main_board_pair_id) references BProgramHwGroup (id);
create index ix_BPair_main_board_pair_5 on BPair (main_board_pair_id);
alter table BProgram add constraint fk_BProgram_instance_6 foreign key (instance_id) references HomerInstance (id);
create index ix_BProgram_instance_6 on BProgram (instance_id);
alter table BProgram add constraint fk_BProgram_project_7 foreign key (project_id) references Project (id);
create index ix_BProgram_project_7 on BProgram (project_id);
alter table BlockoBlock add constraint fk_BlockoBlock_author_8 foreign key (author_id) references Person (id);
create index ix_BlockoBlock_author_8 on BlockoBlock (author_id);
alter table BlockoBlock add constraint fk_BlockoBlock_type_of_block_9 foreign key (type_of_block_id) references TypeOfBlock (id);
create index ix_BlockoBlock_type_of_block_9 on BlockoBlock (type_of_block_id);
alter table BlockoBlock add constraint fk_BlockoBlock_producer_10 foreign key (producer_id) references Producer (id);
create index ix_BlockoBlock_producer_10 on BlockoBlock (producer_id);
alter table BlockoBlockVersion add constraint fk_BlockoBlockVersion_author_11 foreign key (author_id) references Person (id);
create index ix_BlockoBlockVersion_author_11 on BlockoBlockVersion (author_id);
alter table BlockoBlockVersion add constraint fk_BlockoBlockVersion_blocko__12 foreign key (blocko_block_id) references BlockoBlock (id);
create index ix_BlockoBlockVersion_blocko__12 on BlockoBlockVersion (blocko_block_id);
alter table Board add constraint fk_Board_picture_13 foreign key (picture_id) references FileRecord (id);
create index ix_Board_picture_13 on Board (picture_id);
alter table Board add constraint fk_Board_type_of_board_14 foreign key (type_of_board_id) references TypeOfBoard (id);
create index ix_Board_type_of_board_14 on Board (type_of_board_id);
alter table Board add constraint fk_Board_project_15 foreign key (project_id) references Project (id);
create index ix_Board_project_15 on Board (project_id);
alter table Board add constraint fk_Board_actual_c_program_ver_16 foreign key (actual_c_program_version_id) references VersionObject (id);
create index ix_Board_actual_c_program_ver_16 on Board (actual_c_program_version_id);
alter table Board add constraint fk_Board_actual_backup_c_prog_17 foreign key (actual_backup_c_program_version_id) references VersionObject (id);
create index ix_Board_actual_backup_c_prog_17 on Board (actual_backup_c_program_version_id);
alter table Board add constraint fk_Board_actual_boot_loader_18 foreign key (actual_boot_loader_id) references BootLoader (id);
create index ix_Board_actual_boot_loader_18 on Board (actual_boot_loader_id);
alter table BootLoader add constraint fk_BootLoader_type_of_board_19 foreign key (type_of_board_id) references TypeOfBoard (id);
create index ix_BootLoader_type_of_board_19 on BootLoader (type_of_board_id);
alter table BootLoader add constraint fk_BootLoader_main_type_of_bo_20 foreign key (main_type_of_board_id) references TypeOfBoard (id);
create index ix_BootLoader_main_type_of_bo_20 on BootLoader (main_type_of_board_id);
alter table CCompilation add constraint fk_CCompilation_version_objec_21 foreign key (c_compilation_version) references VersionObject (id);
create index ix_CCompilation_version_objec_21 on CCompilation (c_compilation_version);
alter table CCompilation add constraint fk_CCompilation_bin_compilati_22 foreign key (bin_compilation_file_id) references FileRecord (id);
create index ix_CCompilation_bin_compilati_22 on CCompilation (bin_compilation_file_id);
alter table CProgram add constraint fk_CProgram_project_23 foreign key (project_id) references Project (id);
create index ix_CProgram_project_23 on CProgram (project_id);
alter table CProgram add constraint fk_CProgram_type_of_board_24 foreign key (type_of_board_id) references TypeOfBoard (id);
create index ix_CProgram_type_of_board_24 on CProgram (type_of_board_id);
alter table CProgram add constraint fk_CProgram_type_of_board_def_25 foreign key (type_of_board_default_id) references TypeOfBoard (id);
create index ix_CProgram_type_of_board_def_25 on CProgram (type_of_board_default_id);
alter table CProgram add constraint fk_CProgram_type_of_board_tes_26 foreign key (type_of_board_test_id) references TypeOfBoard (id);
create index ix_CProgram_type_of_board_tes_26 on CProgram (type_of_board_test_id);
alter table CProgram add constraint fk_CProgram_example_library_27 foreign key (example_library_id) references VersionObject (id);
create index ix_CProgram_example_library_27 on CProgram (example_library_id);
alter table CProgramUpdatePlan add constraint fk_CProgramUpdatePlan_actuali_28 foreign key (actualization_procedure_id) references ActualizationProcedure (id);
create index ix_CProgramUpdatePlan_actuali_28 on CProgramUpdatePlan (actualization_procedure_id);
alter table CProgramUpdatePlan add constraint fk_CProgramUpdatePlan_board_29 foreign key (board_id) references Board (id);
create index ix_CProgramUpdatePlan_board_29 on CProgramUpdatePlan (board_id);
alter table CProgramUpdatePlan add constraint fk_CProgramUpdatePlan_c_progr_30 foreign key (c_program_version_for_update_id) references VersionObject (id);
create index ix_CProgramUpdatePlan_c_progr_30 on CProgramUpdatePlan (c_program_version_for_update_id);
alter table CProgramUpdatePlan add constraint fk_CProgramUpdatePlan_bootloa_31 foreign key (bootloader_id) references BootLoader (id);
create index ix_CProgramUpdatePlan_bootloa_31 on CProgramUpdatePlan (bootloader_id);
alter table CProgramUpdatePlan add constraint fk_CProgramUpdatePlan_binary__32 foreign key (binary_file_id) references FileRecord (id);
create index ix_CProgramUpdatePlan_binary__32 on CProgramUpdatePlan (binary_file_id);
alter table ChangePropertyToken add constraint fk_ChangePropertyToken_person_33 foreign key (person_id) references Person (id);
create index ix_ChangePropertyToken_person_33 on ChangePropertyToken (person_id);
alter table Employee add constraint fk_Employee_person_34 foreign key (person_id) references Person (id);
create index ix_Employee_person_34 on Employee (person_id);
alter table Employee add constraint fk_Employee_customer_35 foreign key (customer_id) references Customer (id);
create index ix_Employee_customer_35 on Employee (customer_id);
alter table FileRecord add constraint fk_FileRecord_boot_loader_36 foreign key (boot_loader_id) references BootLoader (id);
create index ix_FileRecord_boot_loader_36 on FileRecord (boot_loader_id);
alter table FileRecord add constraint fk_FileRecord_version_object_37 foreign key (version_object_id) references VersionObject (id);
create index ix_FileRecord_version_object_37 on FileRecord (version_object_id);
alter table FloatingPersonToken add constraint fk_FloatingPersonToken_person_38 foreign key (person_id) references Person (id);
create index ix_FloatingPersonToken_person_38 on FloatingPersonToken (person_id);
alter table GridTerminal add constraint fk_GridTerminal_person_39 foreign key (person_id) references Person (id);
create index ix_GridTerminal_person_39 on GridTerminal (person_id);
alter table GridWidget add constraint fk_GridWidget_author_40 foreign key (author_id) references Person (id);
create index ix_GridWidget_author_40 on GridWidget (author_id);
alter table GridWidget add constraint fk_GridWidget_type_of_widget_41 foreign key (type_of_widget_id) references TypeOfWidget (id);
create index ix_GridWidget_type_of_widget_41 on GridWidget (type_of_widget_id);
alter table GridWidget add constraint fk_GridWidget_producer_42 foreign key (producer_id) references Producer (id);
create index ix_GridWidget_producer_42 on GridWidget (producer_id);
alter table GridWidgetVersion add constraint fk_GridWidgetVersion_author_43 foreign key (author_id) references Person (id);
create index ix_GridWidgetVersion_author_43 on GridWidgetVersion (author_id);
alter table GridWidgetVersion add constraint fk_GridWidgetVersion_grid_wid_44 foreign key (grid_widget_id) references GridWidget (id);
create index ix_GridWidgetVersion_grid_wid_44 on GridWidgetVersion (grid_widget_id);
alter table HomerInstance add constraint fk_HomerInstance_cloud_homer__45 foreign key (cloud_homer_server_id) references HomerServer (id);
create index ix_HomerInstance_cloud_homer__45 on HomerInstance (cloud_homer_server_id);
alter table HomerInstanceRecord add constraint fk_HomerInstanceRecord_main_i_46 foreign key (main_instance_history_id) references HomerInstance (id);
create index ix_HomerInstanceRecord_main_i_46 on HomerInstanceRecord (main_instance_history_id);
alter table HomerInstanceRecord add constraint fk_HomerInstanceRecord_versio_47 foreign key (version_object_id) references VersionObject (id);
create index ix_HomerInstanceRecord_versio_47 on HomerInstanceRecord (version_object_id);
alter table HomerInstanceRecord add constraint fk_HomerInstanceRecord_actual_48 foreign key (actual_running_instance_id) references HomerInstance (id);
create index ix_HomerInstanceRecord_actual_48 on HomerInstanceRecord (actual_running_instance_id);
alter table Invitation add constraint fk_Invitation_owner_49 foreign key (owner_id) references Person (id);
create index ix_Invitation_owner_49 on Invitation (owner_id);
alter table Invitation add constraint fk_Invitation_project_50 foreign key (project_id) references Project (id);
create index ix_Invitation_project_50 on Invitation (project_id);
alter table Invoice add constraint fk_Invoice_product_51 foreign key (product_id) references Product (id);
create index ix_Invoice_product_51 on Invoice (product_id);
alter table InvoiceItem add constraint fk_InvoiceItem_invoice_52 foreign key (invoice_id) references Invoice (id);
create index ix_InvoiceItem_invoice_52 on InvoiceItem (invoice_id);
alter table Library add constraint fk_Library_project_53 foreign key (project_id) references Project (id);
create index ix_Library_project_53 on Library (project_id);
alter table Log add constraint fk_Log_file_54 foreign key (file_id) references FileRecord (id);
create index ix_Log_file_54 on Log (file_id);
alter table MProgram add constraint fk_MProgram_m_project_55 foreign key (m_project_id) references MProject (id);
create index ix_MProgram_m_project_55 on MProgram (m_project_id);
alter table MProgramInstanceParameter add constraint fk_MProgramInstanceParameter__56 foreign key (m_project_program_snapshot_id) references MProjectProgramSnapShot (id);
create index ix_MProgramInstanceParameter__56 on MProgramInstanceParameter (m_project_program_snapshot_id);
alter table MProgramInstanceParameter add constraint fk_MProgramInstanceParameter__57 foreign key (m_program_version_id) references VersionObject (id);
create index ix_MProgramInstanceParameter__57 on MProgramInstanceParameter (m_program_version_id);
alter table MProject add constraint fk_MProject_project_58 foreign key (project_id) references Project (id);
create index ix_MProject_project_58 on MProject (project_id);
alter table MProjectProgramSnapShot add constraint fk_MProjectProgramSnapShot_m__59 foreign key (m_project_id) references MProject (id);
create index ix_MProjectProgramSnapShot_m__59 on MProjectProgramSnapShot (m_project_id);
alter table Notification add constraint fk_Notification_person_60 foreign key (person_id) references Person (id);
create index ix_Notification_person_60 on Notification (person_id);
alter table PasswordRecoveryToken add constraint fk_PasswordRecoveryToken_pers_61 foreign key (person_id) references Person (id);
create index ix_PasswordRecoveryToken_pers_61 on PasswordRecoveryToken (person_id);
alter table PaymentDetails add constraint fk_PaymentDetails_customer_62 foreign key (customer_id) references Customer (id);
create index ix_PaymentDetails_customer_62 on PaymentDetails (customer_id);
alter table PaymentDetails add constraint fk_PaymentDetails_product_63 foreign key (productidpaymentdetails) references Product (id);
create index ix_PaymentDetails_product_63 on PaymentDetails (productidpaymentdetails);
alter table Person add constraint fk_Person_picture_64 foreign key (picture_id) references FileRecord (id);
create index ix_Person_picture_64 on Person (picture_id);
alter table Product add constraint fk_Product_customer_65 foreign key (customer_id) references Customer (id);
create index ix_Product_customer_65 on Product (customer_id);
alter table ProductExtension add constraint fk_ProductExtension_product_66 foreign key (product_id) references Product (id);
create index ix_ProductExtension_product_66 on ProductExtension (product_id);
alter table ProductExtension add constraint fk_ProductExtension_tariff_in_67 foreign key (tariff_included_id) references Tariff (id);
create index ix_ProductExtension_tariff_in_67 on ProductExtension (tariff_included_id);
alter table ProductExtension add constraint fk_ProductExtension_tariff_op_68 foreign key (tariff_optional_id) references Tariff (id);
create index ix_ProductExtension_tariff_op_68 on ProductExtension (tariff_optional_id);
alter table Project add constraint fk_Project_product_69 foreign key (product_id) references Product (id);
create index ix_Project_product_69 on Project (product_id);
alter table ProjectParticipant add constraint fk_ProjectParticipant_project_70 foreign key (project_id) references Project (id);
create index ix_ProjectParticipant_project_70 on ProjectParticipant (project_id);
alter table ProjectParticipant add constraint fk_ProjectParticipant_person_71 foreign key (person_id) references Person (id);
create index ix_ProjectParticipant_person_71 on ProjectParticipant (person_id);
alter table TypeOfBlock add constraint fk_TypeOfBlock_project_72 foreign key (project_id) references Project (id);
create index ix_TypeOfBlock_project_72 on TypeOfBlock (project_id);
alter table TypeOfBoard add constraint fk_TypeOfBoard_producer_73 foreign key (producer_id) references Producer (id);
create index ix_TypeOfBoard_producer_73 on TypeOfBoard (producer_id);
alter table TypeOfBoard add constraint fk_TypeOfBoard_processor_74 foreign key (processor_id) references Processor (id);
create index ix_TypeOfBoard_processor_74 on TypeOfBoard (processor_id);
alter table TypeOfBoard add constraint fk_TypeOfBoard_picture_75 foreign key (picture_id) references FileRecord (id);
create index ix_TypeOfBoard_picture_75 on TypeOfBoard (picture_id);
alter table TypeOfBoardBatch add constraint fk_TypeOfBoardBatch_type_of_b_76 foreign key (type_of_board_id) references TypeOfBoard (id);
create index ix_TypeOfBoardBatch_type_of_b_76 on TypeOfBoardBatch (type_of_board_id);
alter table TypeOfWidget add constraint fk_TypeOfWidget_project_77 foreign key (project_id) references Project (id);
create index ix_TypeOfWidget_project_77 on TypeOfWidget (project_id);
alter table VersionObject add constraint fk_VersionObject_author_78 foreign key (author_id) references Person (id);
create index ix_VersionObject_author_78 on VersionObject (author_id);
alter table VersionObject add constraint fk_VersionObject_library_79 foreign key (library_id) references Library (id);
create index ix_VersionObject_library_79 on VersionObject (library_id);
alter table VersionObject add constraint fk_VersionObject_c_program_80 foreign key (c_program_id) references CProgram (id);
create index ix_VersionObject_c_program_80 on VersionObject (c_program_id);
alter table VersionObject add constraint fk_VersionObject_default_prog_81 foreign key (default_program_id) references CProgram (id);
create index ix_VersionObject_default_prog_81 on VersionObject (default_program_id);
alter table VersionObject add constraint fk_VersionObject_b_program_82 foreign key (b_program_id) references BProgram (id);
create index ix_VersionObject_b_program_82 on VersionObject (b_program_id);
alter table VersionObject add constraint fk_VersionObject_m_program_83 foreign key (m_program_id) references MProgram (id);
create index ix_VersionObject_m_program_83 on VersionObject (m_program_id);



alter table Library_TypeOfBoard add constraint fk_Library_TypeOfBoard_Librar_01 foreign key (Library_id) references Library (id);

alter table Library_TypeOfBoard add constraint fk_Library_TypeOfBoard_TypeOf_02 foreign key (TypeOfBoard_id) references TypeOfBoard (id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshot_01 foreign key (MProjectProgramSnapShot_id) references MProjectProgramSnapShot (id);

alter table b_program_version_snapshots add constraint fk_b_program_version_snapshot_02 foreign key (VersionObject_id) references VersionObject (id);

alter table Person_SecurityRole add constraint fk_Person_SecurityRole_Person_01 foreign key (Person_id) references Person (id);

alter table Person_SecurityRole add constraint fk_Person_SecurityRole_Securi_02 foreign key (SecurityRole_id) references SecurityRole (id);

alter table Person_Permission add constraint fk_Person_Permission_Person_01 foreign key (Person_id) references Person (id);

alter table Person_Permission add constraint fk_Person_Permission_Permissi_02 foreign key (Permission_permission_key) references Permission (permission_key);

alter table SecurityRole_Permission add constraint fk_SecurityRole_Permission_Se_01 foreign key (SecurityRole_id) references SecurityRole (id);

alter table SecurityRole_Permission add constraint fk_SecurityRole_Permission_Pe_02 foreign key (Permission_permission_key) references Permission (permission_key);

alter table BoardFeature_TypeOfBoard add constraint fk_BoardFeature_TypeOfBoard_B_01 foreign key (BoardFeature_id) references BoardFeature (id);

alter table BoardFeature_TypeOfBoard add constraint fk_BoardFeature_TypeOfBoard_T_02 foreign key (TypeOfBoard_id) references TypeOfBoard (id);

alter table VersionObject_BProgramHwGroup add constraint fk_VersionObject_BProgramHwGr_01 foreign key (VersionObject_id) references VersionObject (id);

alter table VersionObject_BProgramHwGroup add constraint fk_VersionObject_BProgramHwGr_02 foreign key (BProgramHwGroup_id) references BProgramHwGroup (id);

# --- !Downs

drop table if exists ActualizationProcedure cascade;

drop table if exists BPair cascade;

drop table if exists BProgram cascade;

drop table if exists BProgramHwGroup cascade;

drop table if exists VersionObject_BProgramHwGroup cascade;

drop table if exists BlockoBlock cascade;

drop table if exists BlockoBlockVersion cascade;

drop table if exists Board cascade;

drop table if exists BootLoader cascade;

drop table if exists CCompilation cascade;

drop table if exists CProgram cascade;

drop table if exists CProgramUpdatePlan cascade;

drop table if exists ChangePropertyToken cascade;

drop table if exists CompilationServer cascade;

drop table if exists Customer cascade;

drop table if exists Employee cascade;

drop table if exists FileRecord cascade;

drop table if exists FloatingPersonToken cascade;

drop table if exists Garfield cascade;

drop table if exists GridTerminal cascade;

drop table if exists GridWidget cascade;

drop table if exists GridWidgetVersion cascade;

drop table if exists HomerInstance cascade;

drop table if exists HomerInstanceRecord cascade;

drop table if exists HomerServer cascade;

drop table if exists Invitation cascade;

drop table if exists Invoice cascade;

drop table if exists InvoiceItem cascade;

drop table if exists Library cascade;

drop table if exists Library_TypeOfBoard cascade;

drop table if exists Log cascade;

drop table if exists LoggyError cascade;

drop table if exists MProgram cascade;

drop table if exists MProgramInstanceParameter cascade;

drop table if exists MProject cascade;

drop table if exists MProjectProgramSnapShot cascade;

drop table if exists b_program_version_snapshots cascade;

drop table if exists Notification cascade;

drop table if exists PasswordRecoveryToken cascade;

drop table if exists PaymentDetails cascade;

drop table if exists Permission cascade;

drop table if exists Person_Permission cascade;

drop table if exists SecurityRole_Permission cascade;

drop table if exists Person cascade;

drop table if exists Person_SecurityRole cascade;

drop table if exists Processor cascade;

drop table if exists Producer cascade;

drop table if exists Product cascade;

drop table if exists ProductExtension cascade;

drop table if exists Project cascade;

drop table if exists ProjectParticipant cascade;

drop table if exists RequestLog cascade;

drop table if exists SecurityRole cascade;

drop table if exists Tariff cascade;

drop table if exists TypeOfBlock cascade;

drop table if exists TypeOfBoard cascade;

drop table if exists BoardFeature_TypeOfBoard cascade;

drop table if exists BoardFeature cascade;

drop table if exists TypeOfBoardBatch cascade;

drop table if exists TypeOfWidget cascade;

drop table if exists ValidationToken cascade;

drop table if exists VersionObject cascade;

drop sequence if exists InvoiceItem_seq;

drop sequence if exists PaymentDetails_seq;

