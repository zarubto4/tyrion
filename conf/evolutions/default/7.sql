
# --- !Ups

alter table model_tariff
  drop column if exists payment_mode_required cascade,
  drop column if exists mode_annually cascade,
  drop column if exists mode_credit cascade,
  drop column if exists free_tariff cascade,
  add column business_model varchar(11),
  add constraint ck_model_tariff_business_model check(business_model in('alpha','saas','fee','cal','integrator','integration'));

update model_tariff as t set
  business_model = c.business_model
from (values
  ('alpha', 'alpha'),
  ('geek', 'saas'),
  ('business_1', 'saas'),
  ('business_2', 'saas')
     ) as c(identifier, business_model)
where c.identifier = t.identifier;

alter table model_payment_details
  add column bank_account varchar(255);

alter table model_product
  add column configuration text,
  drop column if exists tariff_id cascade,
  drop column if exists mode cascade,
  drop column if exists monthly_day_period cascade,
  drop column if exists monthly_year_period cascade,
  drop constraint if exists ck_model_product_mode cascade,
  drop constraint if exists fk_model_product_tariff_64 cascade,
  drop constraint if exists ck_model_product_business_model,
  add constraint ck_model_product_business_model check(business_model in('alpha','saas','fee','cal','integrator','integration')) not valid;

update model_product as t set
  business_model = c.business_model
  from (values
    ('free', 'alpha')
     ) as c(method, business_model)
  where c.method = t.method;

alter table model_product validate constraint ck_model_product_business_model;

drop index if exists ix_model_product_tariff_64 cascade;

# --- !Downs

alter table model_tariff
  add column payment_mode_required boolean,
  add column mode_annually boolean,
  add column mode_credit boolean,
  add column free_tariff boolean,
  drop column if exists business_model cascade,
  drop constraint if exists ck_model_tariff_business_model cascade;

alter table model_payment_details
  drop column if exists bank_account;

alter table model_product
  drop column if exists configuration cascade,
  add column tariff_id varchar(255),
  add column mode varchar(10),
  add column monthly_day_period integer,
  add column monthly_year_period integer,
  add constraint ck_model_product_mode check (mode in ('per_credit','monthly','annual','free')),
  add constraint fk_model_product_tariff_64 foreign key (tariff_id) references model_tariff(id),
  drop constraint if exists ck_model_product_business_model,
  add constraint ck_model_product_business_model check(business_model in('saas','fee','lifelong')) not valid;

update model_product as t set
  business_model = c.new
  from (values
    ('alpha', 'free'),
    ('cal', 'lifelong')
      ) as c(old, new)
  where c.old = t.business_model;

alter table model_product validate constraint ck_model_product_business_model;

create index ix_model_product_tariff_64 on model_product(tariff_id);