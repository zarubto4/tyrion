
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

# --- !Downs

alter table gsm

  add column if not exists json_history TEXT,

  drop column if exists weekly_traffic_threshold_notify_type,
  drop column if exists daily_statistic,
  drop column if exists weekly_statistic,

  drop column if exists monthly_statistic,

  add column if not exists daily_traffic_threshold_exceeded_limit boolean default false not null,
  add column if not exists monthly_traffic_threshold bigint,
  add column if not exists monthly_traffic_threshold_exceeded_limit boolean default false not null,
  add column if not exists total_traffic_threshold bigint,
  add column if not exists total_traffic_threshold_exceeded_limit boolean default false not null;