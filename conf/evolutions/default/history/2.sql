# --- !Ups

alter table model_general_tariff_label rename to model_tariff_label;
alter table model_tariff_label rename general_tariff_id to tariff_id;
alter table model_tariff_label drop column if exists extensions_id cascade;
drop index if exists ix_model_general_tariff_label_39 cascade;
drop index if exists ix_model_general_tariff_label_40 cascade;

alter table model_general_tariff rename to model_tariff;
alter table model_tariff rename tariff_name to name;
alter table model_tariff rename tariff_description to description;
alter table model_tariff rename identificator to identifier;
alter table model_tariff rename required_payment_mode to payment_mode_required;
alter table model_tariff rename required_payment_method to payment_method_required;
alter table model_tariff rename required_paid_that to payment_required;

alter table model_tariff
  alter column credit_for_beginning type bigint,
drop column if exists price_in_usd;

create table model_product_extension (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  color                     varchar(255),
  type                      varchar(8),
  config                    varchar(255),
  order_position            integer,
  active                    boolean,
  removed                   boolean,
  created                   timestamp,
  product_id                varchar(255),
  tariff_included_id        varchar(255),
  tariff_optional_id        varchar(255),
  constraint ck_model_product_extension_type check (type in ('Project','Log','Database')),
  constraint pk_model_product_extension primary key (id));

alter table model_product rename product_individual_name to name;
alter table model_product rename general_tariff_id to tariff_id;
alter table model_product rename date_of_create to created;
alter table model_product rename on_demand_active to on_demand;
alter table model_product rename remaining_credit to credit;

alter table model_product
  add column business_model varchar(8),
  alter credit type bigint,
add column financial_history TEXT,
add constraint ck_model_product_business_model check (business_model in ('saas','lifelong','fee'));

alter table model_invoice
  add column gw_url varchar(255),
  add column proforma_id bigint,
  add column proforma_pdf_url varchar(255),
  add column paid timestamp,
  add column overdue timestamp,
  alter column status type varchar(8),
add column warning varchar(12),
drop constraint if exists ck_model_invoice_status,
add constraint ck_model_invoice_status check (status in ('canceled','overdue','pending','paid')),
add constraint ck_model_invoice_warning check (warning in ('none','first','zero_balance','deactivation','second'));

alter table model_invoice rename facturoid_invoice_id to fakturoid_id;
alter table model_invoice rename facturoid_pdf_url to fakturoid_pdf_url;
alter table model_invoice rename date_of_create to created;

alter table model_invoice_item alter column unit_price type bigint;

alter table model_blocko_block
  add order_position integer,
  add removed_by_user boolean;

alter table model_type_of_block
  add order_position integer,
  add removed_by_user boolean;

alter table model_type_of_widget
  add order_position integer,
  add removed_by_user boolean;

alter table model_blocko_block_version add removed_by_user boolean;
alter table model_board add date_of_user_registration timestamp;
alter table model_cprogram_update_plan add count_of_tries integer;
alter table model_grid_widget add order_position integer;
alter table model_homer_instance add removed_by_user boolean;
alter table model_mprogram add removed_by_user boolean;
alter table model_type_of_board add removed_by_user boolean;

create table model_type_of_board_features (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_model_type_of_board_features primary key (id))
;

create table model_type_of_board_features_mod (
  model_type_of_board_features_id varchar(255) not null,
  model_type_of_board_id         varchar(255) not null,
  constraint pk_model_type_of_board_features_mod primary key (model_type_of_board_features_id, model_type_of_board_id))
;

alter table model_ccompilation
  drop constraint if exists ck_model_ccompilation_status,
add constraint ck_model_ccompilation_status check (status in ('file_with_code_not_found','json_code_is_broken','successfully_compiled_and_restored','compilation_in_progress','compilation_server_error','hardware_unstable','server_was_offline','successfully_compiled_not_restored','compiled_with_code_errors','undefined'));

drop table if exists model_example_model_name cascade;
drop table if exists GeneralTariffExt cascade;
drop table if exists GeneralTariffExt_model_product cascade;

alter table model_tariff
  drop constraint if exists uq_model_general_tariff_identifi,
drop constraint if exists pk_model_general_tariff cascade,
add constraint uq_model_tariff_identifier unique (identifier),
add constraint pk_model_tariff primary key (id);

alter table model_tariff_label
  drop constraint if exists pk_model_general_tariff_label cascade,
drop constraint if exists fk_model_general_tariff_label_39 cascade,
add constraint pk_model_tariff_label primary key (id),
add constraint fk_model_tariff_label_tariff_39 foreign key (tariff_id) references model_tariff (id),
drop constraint if exists fk_model_general_tariff_label_40;

create index ix_model_tariff_label_tariff_39 on model_tariff_label (tariff_id);

alter table model_product_extension add constraint fk_model_product_extension_ta_37 foreign key (tariff_included_id) references model_tariff (id);
create index ix_model_product_extension_ta_37 on model_product_extension (tariff_included_id);
alter table model_product_extension add constraint fk_model_product_extension_ta_38 foreign key (tariff_optional_id) references model_tariff (id);
create index ix_model_product_extension_ta_38 on model_product_extension (tariff_optional_id);
alter table model_product_extension add constraint fk_model_product_extension_pr_61 foreign key (product_id) references model_product (id);
create index ix_model_product_extension_pr_61 on model_product_extension (product_id);

alter table model_product add constraint fk_model_product_tariff_64 foreign key (tariff_id) references model_tariff (id);

alter table model_type_of_board_features_mod add constraint fk_model_type_of_board_featur_01 foreign key (model_type_of_board_features_id) references model_type_of_board_features (id);
alter table model_type_of_board_features_mod add constraint fk_model_type_of_board_featur_02 foreign key (model_type_of_board_id) references model_type_of_board (id);

drop index if exists ix_model_product_general_tari_64;
create index ix_model_product_tariff_64 on model_product (tariff_id);

# --- !Downs

alter table model_tariff_label rename to model_general_tariff_label;
alter table model_general_tariff_label rename column tariff_id to general_tariff_id;
alter table model_general_tariff_label add column extensions_id varchar(255);

alter table model_tariff rename name to tariff_name;
alter table model_tariff rename description to tariff_description;
alter table model_tariff rename identifier to identificator;
alter table model_tariff rename payment_mode_required to required_payment_mode;
alter table model_tariff rename payment_method_required to required_payment_method;
alter table model_tariff rename payment_required to required_paid_that;

alter table model_tariff
  alter column credit_for_beginning type float,
add column price_in_usd float;

alter table model_tariff rename to model_general_tariff;

drop table if exists model_product_extension cascade;

alter table model_product rename name to product_individual_name;
alter table model_product rename tariff_id to general_tariff_id;
alter table model_product rename created to date_of_create;
alter table model_product rename on_demand to on_demand_active;
alter table model_product rename credit to remaining_credit;

alter table model_product
  drop column if exists business_model,
alter remaining_credit type float,
drop column if exists financial_history,
drop constraint if exists ck_model_product_business_model;

alter table model_invoice rename fakturoid_id to facturoid_invoice_id;
alter table model_invoice rename fakturoid_pdf_url to facturoid_pdf_url;
alter table model_invoice rename created to date_of_create;

alter table model_invoice
  drop column if exists gw_url,
drop column if exists proforma_id,
drop column if exists proforma_pdf_url,
alter column status type varchar(14),
drop column if exists warning,
drop column if exists paid,
drop column if exists overdue,
drop constraint if exists ck_model_invoice_status,
add constraint ck_model_invoice_status check (status in ('paid','cancelled','created_waited','sent')),
drop constraint if exists ck_model_invoice_warning;

alter table model_invoice_item
  alter column unit_price type float;

create table GeneralTariffExt (
  id                        varchar(255) not null,
  name                      varchar(255),
  description               varchar(255),
  order_position            integer,
  active                    boolean,
  color                     varchar(255),
  price_in_usd              float,
  general_tariff_included_id varchar(255),
  general_tariff_optional_id varchar(255),
  constraint pk_GeneralTariffExt primary key (id));

alter table model_blocko_block
  drop if exists order_position cascade,
drop if exists removed_by_user cascade;

alter table model_type_of_block
  drop if exists order_position cascade,
drop if exists removed_by_user cascade;

alter table model_type_of_widget
  drop if exists order_position cascade,
drop if exists removed_by_user cascade;

alter table model_blocko_block_version drop if exists removed_by_user cascade;
alter table model_board drop if exists date_of_user_registration cascade;
alter table model_cprogram_update_plan drop if exists count_of_tries cascade;
alter table model_grid_widget drop if exists order_position cascade;
alter table model_homer_instance drop if exists removed_by_user cascade;
alter table model_mprogram drop if exists removed_by_user cascade;
alter table model_type_of_board drop if exists removed_by_user cascade;

create table model_example_model_name (
  id                        varchar(255) not null,
  date_of_create            timestamp,
  constraint pk_model_example_model_name primary key (id))
;

drop table if exists model_type_of_board_features_mod cascade;
drop table if exists model_type_of_board_features cascade;

alter table model_ccompilation
  drop constraint if exists ck_model_ccompilation_status,
add constraint ck_model_ccompilation_status check (status in ('file_with_code_not_found','json_code_is_broken','successfully_compiled_and_restored','compilation_in_progress','compilation_server_error','server_was_offline','successfully_compiled_not_restored','compiled_with_code_errors','undefined'));


alter table model_general_tariff_label
  drop constraint if exists pk_model_tariff_label cascade,
drop constraint if exists fk_model_tariff_label_39 cascade,
add constraint pk_model_general_tariff_label primary key (id),
add constraint fk_model_general_tariff_label_39 foreign key (general_tariff_id) references model_general_tariff (id),
add constraint fk_model_general_tariff_label_40 foreign key (extensions_id) references GeneralTariffExt (id);

drop index if exists ix_model_tariff_label_tariff_39;
create index ix_model_general_tariff_label_39 on model_general_tariff_label (general_tariff_id);

create index ix_model_general_tariff_label_40 on model_general_tariff_label (extensions_id);

alter table model_general_tariff
  drop constraint if exists uq_model_tariff_identifier,
drop constraint if exists pk_model_tariff cascade,
add constraint uq_model_general_tariff_identifi unique (identificator),
add constraint pk_model_general_tariff primary key (id);

create table GeneralTariffExt_model_product (
  GeneralTariffExt_id            varchar(255) not null,
  model_product_id               varchar(255) not null,
  constraint pk_GeneralTariffExt_model_product primary key (GeneralTariffExt_id, model_product_id));

alter table GeneralTariffExt add constraint fk_GeneralTariffExt_general_t_37 foreign key (general_tariff_included_id) references model_general_tariff (id);
create index ix_GeneralTariffExt_general_t_37 on GeneralTariffExt (general_tariff_included_id);
alter table GeneralTariffExt add constraint fk_GeneralTariffExt_general_t_38 foreign key (general_tariff_optional_id) references model_general_tariff (id);
create index ix_GeneralTariffExt_general_t_38 on GeneralTariffExt (general_tariff_optional_id);

alter table GeneralTariffExt_model_product add constraint fk_GeneralTariffExt_model_pro_01 foreign key (GeneralTariffExt_id) references GeneralTariffExt (id);
alter table GeneralTariffExt_model_product add constraint fk_GeneralTariffExt_model_pro_02 foreign key (model_product_id) references model_product (id);

alter table model_product add constraint fk_model_product_general_tari_64 foreign key (general_tariff_id) references model_general_tariff (id);

drop index if exists ix_model_product_tariff_64;
create index ix_model_product_general_tari_64 on model_product (general_tariff_id);