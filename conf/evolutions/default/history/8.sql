
# --- !Ups

alter table model_person
  add column facebook_oauth_id varchar(255),
  add column github_oauth_id varchar(255);

alter table model_floating_person_token
  drop column if exists notification_subscriber;

alter table model_loggy_error
  add column repetition bigint,
  add column stack_trace text,
  add column cause text;

update model_loggy_error set repetition = 1 where repetition isnull;

alter table model_loggy_error
  rename column date_of_create to created;

update model_loggy_error set created = now() where created isnull;

delete from model_loggy_error;

alter table model_product_extension
  alter column config type text,
  drop constraint ck_model_product_extension_type,
  add constraint ck_model_product_extension_type check (type in('project','database','log','rest_api','support','instance','homer_server','participant')) not valid;

alter table model_product_extension
  rename column config to configuration;

update model_product_extension as t set
  type = c.new_type
from (values
  ('Project', 'project'),
  ('Database', 'database'),
  ('Log', 'log'),
  ('RestApi', 'rest_api')
     ) as c(old_type, new_type)
where c.old_type = t.type;

alter table model_product_extension
  validate constraint ck_model_product_extension_type;

# --- !Downs

alter table model_person
  drop column if exists facebook_oauth_id cascade,
  drop column if exists github_oauth_id cascade;

alter table model_floating_person_token
  add column notification_subscriber boolean;

update model_floating_person_token set notification_subscriber = false;

alter table model_loggy_error
  drop column if exists repetition,
  drop column if exists stack_trace,
  drop column if exists cause;

alter table model_loggy_error
  rename column created to date_of_create;

alter table model_product_extension
  alter column configuration type varchar(255),
  drop constraint ck_model_product_extension_type,
  add constraint ck_model_product_extension_type check (type in('Project','Database','Log','RestApi')) not valid;

alter table model_product_extension
  rename column configuration to config;

update model_product_extension as t set
  type = c.old_type
from (values
  ('Project', 'project'),
  ('Database', 'database'),
  ('Log', 'log'),
  ('RestApi', 'rest_api')
     ) as c(old_type, new_type)
where c.new_type = t.type;

alter table model_product_extension
  validate constraint ck_model_product_extension_type;