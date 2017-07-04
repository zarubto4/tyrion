
# --- !Ups

alter table model_board
  add column connected_server_id VARCHAR(255),
  add COLUMN connected_instance_id VARCHAR(255),
  add column database_synchronize BOOLEAN,
  drop constraint if exists fk_model_board_virtual_instan_18,
  drop constraint if exists fk_model_board_connected_serv_19,
  DROP COLUMN IF EXISTS connected_server_unique_identificator,
  DROP COLUMN IF EXISTS virtual_instance_under_project_id;


drop index if exists ix_model_board_virtual_instan_18;
drop index if exists ix_model_board_connected_serv_19;


update model_board set database_synchronize = TRUE where database_synchronize isnull;



alter table public.model_homer_server
    add COLUMN json_additional_parameter text;


alter TABLE model_homer_instance
  add column project_id VARCHAR(255);


alter TABLE model_project
  DROP COLUMN IF EXISTS private_instance_blocko_instance_name,
  drop constraint if exists fk_model_project_private_inst_65,
  drop constraint if exists uq_model_project_private_instanc;

drop index if exists ix_model_project_private_inst_65;

# --- !Downs

alter table model_board
  drop column if exists connected_server_id,
  drop column if exists connected_instance_id,
  drop column if exists database_synchronize,
  validate constraint fk_model_board_virtual_instan_18,
  add constraint fk_model_board_virtual_instan_18 foreign key (virtual_instance_under_project_id) references model_homer_instance (id);

create index ix_model_board_virtual_instan_18 on model_board (virtual_instance_under_project_id);

alter table public.model_homer_server
  drop COLUMN IF EXISTS json_additional_parameter;

alter table model_homer_instance
  drop column if exists project_id;


alter table model_project
  add constraint fk_model_project_private_inst_65 foreign key (private_instance_id) references model_homer_instance (id);


create index ix_model_project_private_inst_65 on model_project (private_instance_id);
