
# --- !Ups

alter table model_board
  add column web_view boolean,
  add column web_port INTEGER;

update model_board set web_view = false where web_view isnull;
update model_board set web_port = 80 where web_port isnull;

# --- !Downs

alter table model_board
  drop column if exists web_view,
  drop column if exists web_view;
