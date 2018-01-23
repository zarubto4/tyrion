
# --- !Ups

alter table HomerServer
  add column server_version varchar(255),
  add column rest_api_port integer;

# --- !Downs

alter table HomerServer
  drop column if exists server_version cascade,
  drop column if exists rest_api_port cascade;

