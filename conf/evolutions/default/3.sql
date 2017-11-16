
# --- !Ups

alter table Board
  add column json_bootloader_core_configuration text,
  drop column if exists web_view cascade,
  drop column if exists web_port cascade;

# --- !Downs

alter table Board
  drop column if exists json_bootloader_core_configuration cascade,
  add column web_view boolean default false,
  add column web_port integer;

update Board set web_view = false;