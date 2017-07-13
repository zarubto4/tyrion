
# --- !Ups

alter table model_tariff
  rename column payment_method_required to payment_details_required;

create table model_employee (
  id                        varchar(255) not null,
  customer_id               varchar(255),
  created                   timestamp,
  person_id                 varchar(255),
  state                     varchar(7),
  constraint ck_model_employee_state check (state in ('owner','member','invited','admin')),
  constraint pk_model_employee primary key (id))
;

create table model_customer (
  id                        varchar(255) not null,
  person_id                 varchar(255),
  fakturoid_subject_id      varchar(255),
  created                   timestamp,
  removed_by_user           boolean,
  company                   boolean,
  state                     varchar(7),
  constraint ck_model_customer_state check (state in ('owner','member','invited','admin')),
  constraint pk_model_customer primary key (id))
;

insert into model_customer (id, person_id, fakturoid_subject_id, created, removed_by_user, company, state) select person_id, person_id, null, now(), false, false, 'owner' from model_payment_details where productidpaymentdetails notnull group by person_id;

alter table model_product
  add column customer_id varchar(255);

update model_product as t set
  customer_id = c.id
  from (select id, person_id from model_customer)
  as c(id, person_id)
  where (select productidpaymentdetails from model_payment_details where person_id = c.person_id and productidpaymentdetails = t.id) = t.id;

alter table model_payment_details
  drop column if exists person_id cascade,
  add column customer_id varchar(255),
  drop constraint if exists fk_model_payment_details_pers_61 cascade;

drop index if exists ix_model_payment_details_pers_61 cascade;

alter table model_customer add constraint fk_model_customer_person_83 foreign key (person_id) references model_person (id);
create index ix_model_customer_person_83 on model_customer (person_id);

alter table model_payment_details add constraint fk_model_payment_details_cust_61 foreign key (customer_id) references model_customer (id);
create index ix_model_payment_details_cust_61 on model_payment_details (customer_id);

alter table model_product add constraint fk_model_product_customer_82 foreign key (customer_id) references model_customer (id);
create index ix_model_product_customer_82 on model_product (customer_id);

alter table model_employee add constraint fk_model_employee_customer_84 foreign key (customer_id) references model_customer (id);
create index ix_model_employee_customer_84 on model_employee (customer_id);

alter table model_employee add constraint fk_model_employee_person_85 foreign key (person_id) references model_person (id);
create index ix_model_employee_person_85 on model_employee (person_id);

# --- !Downs

alter table model_tariff
  rename column payment_details_required to payment_method_required;

alter table model_payment_details
  add column person_id varchar(255);

update model_payment_details as t set
  person_id = c.person_id
  from (select id, person_id from model_customer)
  as c(id, person_id)
  where t.productidpaymentdetails in (select id from model_product where customer_id = c.id);

drop table if exists model_employee cascade;
drop table if exists model_customer cascade;

alter table model_payment_details
  drop column if exists customer_id cascade,
  drop constraint if exists fk_model_payment_details_cust_61 cascade;

alter table model_product
  drop column if exists customer_id cascade,
  drop constraint if exists fk_model_product_customer_82 cascade;

drop index if exists ix_model_product_customer_82 cascade;
drop index if exists ix_model_customer_person_83 cascade;
drop index if exists ix_model_employee_customer_84 cascade;
drop index if exists ix_model_employee_person_85 cascade;
drop index if exists ix_model_payment_details_cust_61 cascade;

alter table model_payment_details add constraint fk_model_payment_details_pers_61 foreign key (person_id) references model_person (id);
create index ix_model_payment_details_pers_61 on model_payment_details (person_id);