
# --- !Ups

alter table model_board
  add column web_view boolean;

update model_board set web_view = false where web_view isnull;


# --- !Downs

alter table model_board
  drop column if exists web_view;
