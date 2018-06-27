
# --- !Ups

alter table widget
  add column working_copy boolean default false not null;

# --- !Downs

alter table widget
  drop column if exists working_copy;
