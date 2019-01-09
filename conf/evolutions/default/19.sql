
# --- !Ups

alter table notification add column confirmation_id uuid;

# --- !Downs

alter table notification drop column if exists confirmation_id cascade;
