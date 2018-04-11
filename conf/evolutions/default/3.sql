
# --- !Ups

alter table cprogram
  ADD COLUMN IF NOT EXISTS original_id uuid;

alter table widget
  ADD COLUMN IF NOT EXISTS original_id uuid;

alter table block
  ADD COLUMN IF NOT EXISTS original_id uuid;

# --- !Downs

alter table cprogram
  drop column if exists original_id;

alter table widget
  drop column if exists original_id;

alter table block
  drop column if exists original_id;
