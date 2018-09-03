
# --- !Ups
--- Preserves product and customer items in the database, but remove most of the details!!
--- (At the time this evolution was applied to production database, they were no useful data here anyway.)

alter table if exists product drop constraint if exists fk_product_customer_id;
drop index if exists ix_product_customer_id;

alter table tariffextension add column consumption TEXT;

alter table tariff rename column company_details_required to owner_details_required;
alter table tariff drop column if exists payment_method_required;

create table extensionfinancialevent (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  product_extension_id          uuid,
  event_start                   timestamptz,
  event_end                     timestamptz,
  consumption                   TEXT,
  invoice_id                    uuid,
  deleted                       boolean default false not null,
  constraint pk_extensionfinancialevent primary key (id)
);

create table productevent (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  product_id                    uuid,
  reference                     uuid,
  event_type                    varchar(30),
  read_permission               varchar(5),
  detail                        TEXT,
  deleted                       boolean default false not null,
  constraint ck_productevent_event_type check ( event_type in ('PRODUCT_CREATED', 'PRODUCT_DELETED',
  'INVOICE_CREATED', 'INVOICE_CONFIRMED', 'INVOICE_ISSUED', 'INVOICE_PAYMENT_RECEIVED',
  'EXTENSION_CREATED', 'EXTENSION_ACTIVATED', 'EXTENSION_DEACTIVATED', 'EXTENSION_DELETED')),
    constraint ck_productevent_read_permission check ( read_permission in ('USER', 'ADMIN')),
  constraint pk_productevent primary key (id)
);

create table payment_details (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  payment_methods               varchar(255),
  payment_method                varchar(13),
  on_demand                     boolean default false not null,
  monthly_limit                 numeric,
  deleted                       boolean default false not null,
  constraint ck_payment_details_payment_method check ( payment_method in ('INVOICE_BASED','CREDIT_CARD')),
  constraint pk_payment_details primary key (id)
);

alter table product rename customer_id to owner_id;
alter table product drop column if exists fakturoid_subject_id,
                    drop column if exists gopay_id,
                    drop column if exists on_demand,
                    drop column if exists method,
                    drop column if exists financial_history,
                    drop column if exists client_billing,
                    drop column if exists client_billing_invoice_parameters,
                    add column integrator_client_id uuid,
                    add column payment_details_id uuid,
  drop constraint ck_product_business_model,
  add constraint ck_product_business_model check ( business_model in ('ALPHA', 'INTEGRATOR','INTEGRATION','SAAS','FEE')),
  add constraint uq_product_integrator_client_id unique (integrator_client_id),
  add constraint uq_product_payment_details_id unique (payment_details_id);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--- insert empty payment_details to every row
with ids as (
    update product set payment_details_id = uuid_generate_v4() returning payment_details_id
)
insert into payment_details(
    id, created, updated, removed, payment_methods, payment_method, on_demand, monthly_limit, deleted)
    select payment_details_id, current_timestamp, current_timestamp, null, 'INVOICE_BASED|CREDIT_CARD', 'INVOICE_BASED', false, 0, false from ids;

--- now payment_details can be not null
alter table product alter column payment_details_id set not null;

alter table invoice add column total_price_without_vat numeric,
                    add column total_price_with_vat numeric,
                    add column currency varchar(3),
                    add column issued timestamptz,
                    alter column status type varchar(11),
                    add column public_html_url varchar(255),
  drop constraint ck_invoice_method,
  add constraint ck_invoice_method check ( method in ('INVOICE_BASED','CREDIT_CARD')),
  drop constraint ck_invoice_status,
  add constraint ck_invoice_status check ( status in ('CANCELED','PAID','PENDING','OVERDUE', 'UNCONFIRMED', 'UNFINISHED'));

alter table invoiceitem alter column quantity type numeric,
                        alter column unit_price type numeric,
                        drop column if exists currency;

alter table if exists paymentdetails drop constraint if exists fk_paymentdetails_customer_id;
alter table if exists paymentdetails drop constraint if exists fk_paymentdetails_productidpaymentdetails;
drop table if exists paymentdetails;


create table contact (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  fakturoid_subject_id          bigint,
  gopay_id                      bigint,
  company_account               boolean default false not null,
  name                          varchar(255),
  company_authorized_email      varchar(255),
  company_authorized_phone      varchar(255),
  company_web                   varchar(255),
  company_registration_no       varchar(255),
  company_vat_number            varchar(255),
  street                        varchar(255),
  street_number                 varchar(255),
  city                          varchar(255),
  zip_code                      varchar(255),
  country                       varchar(255),
  invoice_email                 varchar(255),
  bank_account                  varchar(255),
  deleted                       boolean default false not null,
  constraint pk_contact primary key (id)
);

alter table customer add column contact_id uuid,
                     drop column if exists fakturoid_subject_id,
                     add constraint uq_customer_contact_id unique (contact_id);

create table integratorclient (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  contact_id                    uuid,
  deleted                       boolean default false not null,
  constraint uq_integratorclient_contact_id unique (contact_id),
  constraint pk_integratorclient primary key (id)
);

alter table extensionfinancialevent add constraint fk_extensionfinancialevent_product_extension_id foreign key (product_extension_id) references productextension (id) on delete restrict on update restrict;
create index ix_extensionfinancialevent_product_extension_id on extensionfinancialevent (product_extension_id);

alter table extensionfinancialevent add constraint fk_extensionfinancialevent_invoice_id foreign key (invoice_id) references invoice (id) on delete restrict on update restrict;
create index ix_extensionfinancialevent_invoice_id on extensionfinancialevent (invoice_id);

alter table productevent add constraint fk_productevent_product_id foreign key (product_id) references product (id) on delete restrict on update restrict;
create index ix_productevent_product_id on productevent (product_id);

alter table product add constraint fk_product_owner_id foreign key (owner_id) references customer (id) on delete restrict on update restrict;
create index ix_product_owner_id on product (owner_id);

alter table product add constraint fk_product_integrator_client_id foreign key (integrator_client_id) references integratorclient (id) on delete restrict on update restrict;

alter table integratorclient add constraint fk_integratorclient_contact_id foreign key (contact_id) references contact (id) on delete restrict on update restrict;

alter table product add constraint fk_product_payment_details_id foreign key (payment_details_id) references payment_details (id) on delete restrict on update restrict;

alter table customer add constraint fk_customer_contact_id foreign key (contact_id) references contact (id) on delete restrict on update restrict;

# --- !Downs
--- Works only for an empty database!

alter table if exists extensionfinancialevent drop constraint if exists fk_extensionfinancialevent_product_extension_id;
drop index if exists ix_extensionfinancialevent_product_extension_id;

alter table if exists extensionfinancialevent drop constraint if exists fk_extensionfinancialevent_invoice_id;
drop index if exists ix_extensionfinancialevent_invoice_id;

alter table if exists productevent drop constraint if exists fk_productevent_product_id;
drop index if exists ix_productevent_product_id;

alter table if exists product drop constraint if exists fk_product_integrator_client_id;

alter table if exists product drop constraint if exists fk_product_payment_details_id;

alter table if exists customer drop constraint if exists fk_customer_contact_id;

alter table if exists integratorclient drop constraint if exists fk_integratorclient_contact_id;

alter table if exists product drop constraint if exists fk_product_owner_id;
drop index if exists ix_product_owner_id;

alter table tariffextension drop column if exists consumption;

alter table tariff rename column owner_details_required to company_details_required;
alter table tariff add column payment_method_required boolean default false not null;

alter table product rename owner_id to customer_id;
alter table product add column fakturoid_subject_id varchar(255),
                    add column gopay_id bigint,
                    add column on_demand boolean default false not null,
                    add column method varchar(13),
                    add column financial_history TEXT,
                    add column client_billing boolean default false not null,
                    add column client_billing_invoice_parameters varchar(255),
                    drop column integrator_client_id,
                    drop column payment_details_id,
  add constraint ck_product_method check ( method in ('CREDIT','BANK_TRANSFER','CREDIT_CARD','FREE')),
  drop constraint ck_product_business_model,
  add constraint ck_product_business_model check ( business_model in ('INTEGRATOR','INTEGRATION','SAAS','FEE','ALPHA','CAL'));

alter table invoice drop column if exists total_price_without_vat,
                    drop column if exists total_price_with_vat,
                    drop column if exists currency,
                    drop column issued,
                    alter column status type varchar(8),
                    drop column public_html_url,
  drop constraint ck_invoice_method,
  add constraint ck_invoice_method check ( method in ('CREDIT','BANK_TRANSFER','CREDIT_CARD','FREE')),
  drop constraint ck_invoice_status,
  add constraint ck_invoice_status check ( status in ('CANCELED','PAID','PENDING','OVERDUE'));

alter table invoiceitem alter column quantity type bigint,
                        alter column unit_price type bigint,
                        add column currency varchar(3);

drop table if exists extensionfinancialevent cascade;

drop table if exists productevent cascade;

drop table if exists contact;

drop table if exists integratorclient cascade;

drop table if exists payment_details cascade;

create table paymentdetails (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  customer_id                   uuid,
  productidpaymentdetails       uuid,
  company_account               boolean default false not null,
  company_name                  varchar(255),
  company_authorized_email      varchar(255),
  company_authorized_phone      varchar(255),
  company_web                   varchar(255),
  company_registration_no       varchar(255),
  company_vat_number            varchar(255),
  full_name                     varchar(255),
  street                        varchar(255),
  street_number                 varchar(255),
  city                          varchar(255),
  zip_code                      varchar(255),
  country                       varchar(255),
  invoice_email                 varchar(255),
  bank_account                  varchar(255),
  deleted                       boolean default false not null,
  constraint uq_paymentdetails_customer_id unique (customer_id),
  constraint uq_paymentdetails_productidpaymentdetails unique (productidpaymentdetails),
  constraint pk_paymentdetails primary key (id)
);

alter table customer drop constraint uq_customer_contact_id;

alter table customer drop column contact_id,
                     add column fakturoid_subject_id varchar(255);

alter table paymentdetails add constraint fk_paymentdetails_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;

alter table paymentdetails add constraint fk_paymentdetails_productidpaymentdetails foreign key (productidpaymentdetails) references product (id) on delete restrict on update restrict;

alter table product add constraint fk_product_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;

create index ix_product_customer_id on product (customer_id);