
# --- !Ups

alter table model_board
  add column IF NOT EXISTS developer_kit BOOLEAN;

update model_board set developer_kit = false where developer_kit isnull;

# --- !Downs

alter table model_board
  drop column if exists developer_kit;
