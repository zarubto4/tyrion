
# --- !Ups

alter table blob
  add column storage_type  varchar(255) default 'AzureBlob',
  add column link  varchar(500) default null;

alter table notification add column confirmation_id uuid;



# --- !Downs

alter table blob
  drop column if exists storage_type cascade,
  drop column if exists link cascade;

alter table notification drop column if exists confirmation_id cascade;
