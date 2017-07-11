
# --- !Ups

alter table model_product
  add column removed_by_user boolean;

alter table model_project
  add column removed_by_user boolean;

update model_product set removed_by_user = false where removed_by_user isnull;
update model_project set removed_by_user = false where removed_by_user isnull;

alter table model_homer_instance
  rename column blocko_instance_name to id;

alter table model_homer_instance
  add column name varchar(255),
  add column description text;

alter table model_board
  alter column personal_description type varchar(255),
  alter column generation_description type text;

alter table model_board
  rename column personal_description to name;

alter table model_board
  rename column generation_description to description;

alter table model_board
  rename column virtual_instance_under_project_blocko_instance_name to virtual_instance_under_project_id;

alter table model_bprogram
  rename column instance_blocko_instance_name to instance_id;

alter table model_homer_instance_record
  rename column main_instance_history_blocko_instance_name to main_instance_history_id;

alter table model_homer_instance_record
  rename column actual_running_instance_blocko_instance_name to actual_running_instance_id;

alter table model_project
    rename column private_instance_blocko_instance_name to private_instance_id;

drop index if exists ix_model_bprogram_instance_6;
drop index if exists ix_model_board_virtual_instan_18;
drop index if exists ix_model_homer_instance_recor_48;
drop index if exists ix_model_homer_instance_recor_50;
drop index if exists ix_model_project_private_inst_65;

alter table model_bprogram
  drop constraint if exists fk_model_bprogram_instance_6,
  add constraint fk_model_bprogram_instance_6 foreign key (instance_id) references model_homer_instance (id);

create index ix_model_bprogram_instance_6 on model_bprogram (instance_id);

alter table model_board
  drop constraint if exists fk_model_board_virtual_instan_18,
  add constraint fk_model_board_virtual_instan_18 foreign key (virtual_instance_under_project_id) references model_homer_instance (id);

create index ix_model_board_virtual_instan_18 on model_board (virtual_instance_under_project_id);

alter table model_homer_instance_record
  drop constraint if exists fk_model_homer_instance_recor_48,
  add constraint fk_model_homer_instance_recor_48 foreign key (main_instance_history_id) references model_homer_instance (id);

create index ix_model_homer_instance_recor_48 on model_homer_instance_record (main_instance_history_id);

alter table model_homer_instance_record
  drop constraint if exists fk_model_homer_instance_recor_50,
  add constraint fk_model_homer_instance_recor_50 foreign key (actual_running_instance_id) references model_homer_instance (id);

create index ix_model_homer_instance_recor_50 on model_homer_instance_record (actual_running_instance_id);

alter table model_project
  drop constraint if exists fk_model_project_private_inst_65,
  add constraint fk_model_project_private_inst_65 foreign key (private_instance_id) references model_homer_instance (id);

create index ix_model_project_private_inst_65 on model_project (private_instance_id);

# --- !Downs

alter table model_product drop column if exists removed_by_user;
alter table model_project drop column if exists removed_by_user;

alter table model_homer_instance
  rename column id to blocko_instance_name;

alter table model_homer_instance
  drop column if exists name,
  drop column if exists description;

alter table model_board
  rename column name to personal_description;

alter table model_board
  rename column description to generation_description;

alter table model_board
  alter column personal_description type text,
  alter column generation_description type varchar(255);

alter table model_board
  rename column virtual_instance_under_project_id to virtual_instance_under_project_blocko_instance_name;

alter table model_bprogram
  rename column instance_id to instance_blocko_instance_name;

alter table model_homer_instance_record
  rename column main_instance_history_id to main_instance_history_blocko_instance_name;

alter table model_homer_instance_record
  rename column actual_running_instance_id to actual_running_instance_blocko_instance_name;

alter table model_project
  rename column private_instance_id to private_instance_blocko_instance_name;

drop index if exists ix_model_bprogram_instance_6;
drop index if exists ix_model_board_virtual_instan_18;
drop index if exists ix_model_homer_instance_recor_48;
drop index if exists ix_model_homer_instance_recor_50;
drop index if exists ix_model_project_private_inst_65;

alter table model_bprogram
  drop constraint if exists fk_model_bprogram_instance_6,
  add constraint fk_model_bprogram_instance_6 foreign key (instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);

create index ix_model_bprogram_instance_6 on model_bprogram (instance_blocko_instance_name);

alter table model_board
  drop constraint if exists fk_model_board_virtual_instan_18,
  add constraint fk_model_board_virtual_instan_18 foreign key (virtual_instance_under_project_blocko_instance_name) references model_homer_instance (blocko_instance_name);

create index ix_model_board_virtual_instan_18 on model_board (virtual_instance_under_project_blocko_instance_name);

alter table model_homer_instance_record
  drop constraint if exists fk_model_homer_instance_recor_48,
  add constraint fk_model_homer_instance_recor_48 foreign key (main_instance_history_blocko_instance_name) references model_homer_instance (blocko_instance_name);

create index ix_model_homer_instance_recor_48 on model_homer_instance_record (main_instance_history_blocko_instance_name);

alter table model_homer_instance_record
  drop constraint if exists fk_model_homer_instance_recor_50,
  add constraint fk_model_homer_instance_recor_50 foreign key (actual_running_instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);

create index ix_model_homer_instance_recor_50 on model_homer_instance_record (actual_running_instance_blocko_instance_name);

alter table model_project
  drop constraint if exists fk_model_project_private_inst_65,
  add constraint fk_model_project_private_inst_65 foreign key (private_instance_blocko_instance_name) references model_homer_instance (blocko_instance_name);

create index ix_model_project_private_inst_65 on model_project (private_instance_blocko_instance_name);