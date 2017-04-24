
# --- !Ups

ALTER TABLE model_blocko_block ADD order_position integer;
ALTER TABLE model_board ADD date_of_user_registration timestamp;


ALTER TABLE model_blocko_block DROP FOREIGN KEY ck_model_ccompilation_status;

ALTER TABLE model_cprogram_update_plan ADD count_of_tries integer;

ALTER TABLE model_grid_widget ADD order_position integer;
ALTER TABLE model_type_of_widget ADD removed_by_user integer;

ALTER TABLE model_homer_instance ADD removed_by_user boolean;

ALTER TABLE model_type_of_block ADD order_position integer;
ALTER TABLE model_type_of_block ADD removed_by_user integer;


ALTER TABLE model_mprogram ADD removed_by_user boolean;

create table model_type_of_board_features (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_model_type_of_board_features primary key (id))
;

ALTER TABLE model_type_of_widget ADD order_position integer;

create table model_type_of_board_features_mod (
  model_type_of_board_features_id varchar(255) not null,
  model_type_of_board_id         varchar(255) not null,
  constraint pk_model_type_of_board_features_mod primary key (model_type_of_board_features_id, model_type_of_board_id))
;

alter table model_type_of_board_features_mod add constraint fk_model_type_of_board_featur_01 foreign key (model_type_of_board_features_id) references model_type_of_board_features (id);
alter table model_type_of_board_features_mod add constraint fk_model_type_of_board_featur_02 foreign key (model_type_of_board_id) references model_type_of_board (id);

# --- !Downs