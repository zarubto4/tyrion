
# --- !Ups

alter table gsm

  drop json_history,
  drop daily_traffic_threshold,
  drop daily_traffic_threshold_exceeded_limit,
  drop monthly_traffic_threshold,
  drop monthly_traffic_threshold_exceeded_limit,
  drop total_traffic_threshold,

  drop total_traffic_threshold_exceeded_limit,
  add column weekly_traffic_threshold_notify_type boolean default false not null,
  add column daily_statistic boolean default false not null,
  add column weekly_statistic boolean default false not null,
  add column monthly_statistic boolean default false not null;


alter table permission
  drop constraint ck_permission_entity_type,
  add constraint ck_permission_entity_type check (entity_type in ('ARTICLE','AUTHORIZATION_TOKEN','BOOTLOADER','COMPILER','GARFIELD',
                                                                  'PERSON','PRODUCT','PROJECT','FIRMWARE','FIRMWARE_VERSION',
                                                                  'LIBRARY','LIBRARY_VERSION','WIDGET','WIDGET_VERSION',
                                                                  'GRID_PROJECT','GRID_PROGRAM','GRID_PROGRAM_VERSION',
                                                                  'BLOCK','BLOCK_VERSION','BLOCKO_PROGRAM','BLOCKO_PROGRAM_VERSION',
                                                                  'INSTANCE','INSTANCE_SNAPSHOT','HARDWARE','HARDWARE_GROUP','HARDWARE_UPDATE',
                                                                  'HARDWARE_ENTITY',
                                                                  'INVITATION','INVOICE','PROCESSOR','PRODUCER','NOTIFICATION','ROLE',
                                                                  'UPDATE_PROCEDURE','PRODUCT_EXTENSION','ERROR','TARIFF','TARIFF_EXTENSION',
                                                                  'PAYMENT_DETAILS','HOMER','HARDWARE_BATCH','HARDWARE_TYPE','CUSTOMER',
                                                                  'EMPLOYEE','GSM','CONTACT','INTEGRATOR_CLIENT'));

# --- !Downs

alter table gsm

  add column if not exists json_history TEXT,

  drop column if exists weekly_traffic_threshold_notify_type,
  drop column if exists daily_statistic,
  drop column if exists weekly_statistic,

  drop column if exists monthly_statistic,

  add column if not exists daily_traffic_threshold bigint,
  add column if not exists daily_traffic_threshold_exceeded_limit boolean default false not null,
  add column if not exists monthly_traffic_threshold bigint,
  add column if not exists monthly_traffic_threshold_exceeded_limit boolean default false not null,
  add column if not exists total_traffic_threshold bigint,
  add column if not exists total_traffic_threshold_exceeded_limit boolean default false not null;


alter table permission
  drop constraint ck_permission_entity_type,
  add constraint ck_permission_entity_type check (entity_type in ('ARTICLE','AUTHORIZATION_TOKEN','BOOTLOADER','COMPILER','GARFIELD',
                                                                  'PERSON','PRODUCT','PROJECT','FIRMWARE','FIRMWARE_VERSION',
                                                                  'LIBRARY','LIBRARY_VERSION','WIDGET','WIDGET_VERSION',
                                                                  'GRID_PROJECT','GRID_PROGRAM','GRID_PROGRAM_VERSION',
                                                                  'BLOCK','BLOCK_VERSION','BLOCKO_PROGRAM','BLOCKO_PROGRAM_VERSION',
                                                                  'INSTANCE','INSTANCE_SNAPSHOT','HARDWARE','HARDWARE_GROUP','HARDWARE_UPDATE',
                                                                  'INVITATION','INVOICE','PROCESSOR','PRODUCER','NOTIFICATION','ROLE',
                                                                  'UPDATE_PROCEDURE','PRODUCT_EXTENSION','ERROR','TARIFF','TARIFF_EXTENSION',
                                                                  'PAYMENT_DETAILS','HOMER','HARDWARE_BATCH','HARDWARE_TYPE','CUSTOMER',
                                                                  'EMPLOYEE','GSM','CONTACT','INTEGRATOR_CLIENT'));