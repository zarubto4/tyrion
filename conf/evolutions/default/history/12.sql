
# --- !Ups

alter table model_board
  add column if not exists web_view boolean,
  add column if not exists web_port INTEGER,
  add column if not exists picture_id varchar(255),
  drop constraint if exists uq_model_board_picture_i,
  add constraint uq_model_board_picture_i unique (picture_id);

alter table model_person
  RENAME COLUMN azure_picture_link to alternative_picture_link;

update model_board set web_view = false where web_view isnull;
update model_board set web_port = 80 where web_port isnull;

# --- !Downs

alter table model_board
  drop column if exists web_view,
  drop column if exists web_port,
  drop column if exists picture_id;

alter table model_board
  drop constraint if exists uq_model_board_picture_i;

alter table model_person
  rename column alternative_picture_link to azure_picture_link;