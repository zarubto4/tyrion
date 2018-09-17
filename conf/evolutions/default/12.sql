
# --- !Ups

delete from role_permission;
delete from person_permission;
delete from permission;

alter table permission
  drop column if exists name,
  drop column if exists description,
  add column action varchar(8),
  add column entity_type varchar(22),
  add constraint ck_permission_action check (action in ('CREATE','READ','UPDATE','DELETE','ACTIVATE','INVITE','PUBLISH','DEPLOY')),
  add constraint ck_permission_entity_type check (entity_type in ('PERSON','PRODUCT','PROJECT','FIRMWARE','FIRMWARE_VERSION','LIBRARY','LIBRARY_VERSION','WIDGET','WIDGET_VERSION','GRID_PROJECT','GRID_PROGRAM','GRID_PROGRAM_VERSION','BLOCK','BLOCK_VERSION','BLOCKO_PROGRAM','BLOCKO_PROGRAM_VERSION','INSTANCE','INSTANCE_SNAPSHOT','HARDWARE','HARDWARE_GROUP'));

alter table role
  add column project_id uuid;

alter table role add constraint fk_role_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_role_project_id on role (project_id);

alter table person_role rename to role_person;

# --- !Downs

delete from role_permission;
delete from permission;

alter table permission
  drop column if exists action cascade,
  drop column if exists entity_type cascade,
  add column name varchar(255),
  add column description TEXT;

delete from role where project_id is not null;

alter table role
  drop column if exists project_id cascade;

drop index if exists ix_role_project_id;

alter table role_person rename to person_role;