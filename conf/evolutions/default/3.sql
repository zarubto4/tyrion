
# --- !Ups

alter table cprogram
  add column original_id uuid;

alter table widget
  add column original_id uuid;

alter table block
  add column original_id uuid;

# --- !Downs

alter table cprogram
  drop column if exists original_id;

alter table widget
  drop column if exists original_id;

alter table block
  drop column if exists original_id;
