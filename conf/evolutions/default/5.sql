
/* Verze 1.9.4 */

# --- !Ups

alter table model_loggy_error
  add column date_of_create timestamp;


# --- !Downs

alter table model_loggy_error
  DROP column date_of_create;