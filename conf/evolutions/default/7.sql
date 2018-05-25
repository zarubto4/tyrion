# --- !Ups

alter table gsm
  add column daily_traffic_threshold bigint,
  add column daily_traffic_threshold_exceeded_limit boolean default false not null,
  add column daily_traffic_threshold_notify_type boolean default false not null,
  add column monthly_traffic_threshold bigint,
  add column monthly_traffic_threshold_exceeded_limit boolean default false not null,
  add column monthly_traffic_threshold_notify_type boolean default false not null,
  add column total_traffic_threshold bigint,
  add column total_traffic_threshold_exceeded_limit boolean default false not null,
  add column total_traffic_threshold_notify_type boolean default false not null,
  add column msi_number bigint,
  add column json_history TEXT,
  add column blocked boolean default false not null,
  drop column if exists MSINumber,
  drop column if exists private_additional_information;

# --- !Downs

alter table gsm
  drop column if exists daily_traffic_threshold,
  drop column if exists daily_traffic_threshold_exceeded_limit,
  drop column if exists daily_traffic_threshold_notify_type,
  drop column if exists monthly_traffic_threshold,
  drop column if exists monthly_traffic_threshold_exceeded_limit,
  drop column if exists monthly_traffic_threshold_notify_type,
  drop column if exists total_traffic_threshold,
  drop column if exists total_traffic_threshold_exceeded_limit,
  drop column if exists total_traffic_threshold_notify_type,
  drop column if exists msi_number,
  drop column if exists json_history,
  drop column if exists blocked,
  add column MSINumber varchar(255),
  add column private_additional_information varchar(255);
