
# --- !Ups

alter table widgetversion
  add column working_copy boolean default false not null;

# --- !Downs

alter table widgetversion
  drop column if exists working_copy;
