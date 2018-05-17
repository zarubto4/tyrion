# --- !Ups

ALTER TABLE gsm
  ADD column daily_traffic_threshold bigint,
  ADD column daily_traffic_threshold_exceeded_limit boolean default TRUE not null,
  ADD column daily_traffic_threshold_notify_type boolean default TRUE not null,

  ADD column monthly_traffic_threshold bigint,
  ADD column monthly_traffic_threshold_exceeded_limit boolean default TRUE not null,
  ADD column monthly_traffic_threshold_notify_type boolean default TRUE not null,

  ADD column total_traffic_threshold bigint,
  ADD column total_traffic_threshold_exceeded_limit boolean default TRUE not null,
  ADD column total_traffic_threshold_notify_type boolean default TRUE not null,

  ADD column msi_number bigint,
  ADD column json_history TEXT,
  DROP column if EXISTS msinumber,
  ADD column blocked boolean default TRUE not null;



# --- !Downs

ALTER TABLE gsm
  DROP column if exists daily_traffic_threshold,
  DROP column if exists daily_traffic_threshold_exceeded_limit,
  DROP column if exists daily_traffic_threshold_notify_type,

  DROP column if exists monthly_traffic_threshold,
  DROP column if exists monthly_traffic_threshold_exceeded_limit,
  DROP column if exists monthly_traffic_threshold_notify_type,

  DROP column if exists total_traffic_threshold,
  DROP column if exists total_traffic_threshold_exceeded_limit,
  DROP column if exists total_traffic_threshold_notify_type,

  ADD column if not exists msinumber varchar(255),
  DROP column if exists  msi_number,

  DROP column if exists  json_history,

  DROP column if exists  blocked;
