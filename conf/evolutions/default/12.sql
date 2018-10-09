
# --- !Ups

delete from role_permission;
delete from person_role;
delete from person_permission;
delete from permission;

alter table permission
  drop column if exists name,
  drop column if exists description,
  add column action varchar(8),
  add column entity_type varchar(22),
  add constraint ck_permission_action check (action in ('CREATE','READ','UPDATE','DELETE','ACTIVATE','INVITE','PUBLISH','DEPLOY')),
  add constraint ck_permission_entity_type check (entity_type in ('ARTICLE','AUTHORIZATION_TOKEN','BOOTLOADER','COMPILER','GARFIELD',
                                                                  'PERSON','PRODUCT','PROJECT','FIRMWARE','FIRMWARE_VERSION',
                                                                  'LIBRARY','LIBRARY_VERSION','WIDGET','WIDGET_VERSION',
                                                                  'GRID_PROJECT','GRID_PROGRAM','GRID_PROGRAM_VERSION',
                                                                  'BLOCK','BLOCK_VERSION','BLOCKO_PROGRAM','BLOCKO_PROGRAM_VERSION',
                                                                  'INSTANCE','INSTANCE_SNAPSHOT','HARDWARE','HARDWARE_GROUP','HARDWARE_UPDATE',
                                                                  'INVITATION','INVOICE','PROCESSOR','PRODUCER','NOTIFICATION','ROLE',
                                                                  'UPDATE_PROCEDURE','PRODUCT_EXTENSION','ERROR','TARIFF','TARIFF_EXTENSION',
                                                                  'PAYMENT_DETAILS','HOMER','HARDWARE_BATCH','HARDWARE_TYPE','CUSTOMER',
                                                                  'EMPLOYEE','GSM','CONTACT','INTEGRATOR_CLIENT'));

alter table role
  add column project_id uuid,
  add column default_role boolean default false not null;

alter table role add constraint fk_role_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_role_project_id on role (project_id);

alter table person_role rename to role_person;

alter table bootloader add column file_id uuid;

update bootloader as t set
  file_id = c.id
  from (select id, boot_loader_id from blob)
  as c(id, boot_loader_id)
  where c.boot_loader_id = t.id;

alter table blob drop column if exists boot_loader_id cascade;

alter table bootloader
  add constraint uq_bootloader_file_id unique (file_id),
  add constraint fk_bootloader_file_id foreign key (file_id) references blob (id) on delete restrict on update restrict;

alter table projectparticipant rename to project_person;

alter table project_person
  drop column if exists id cascade,
  drop column if exists created cascade,
  drop column if exists updated cascade,
  drop column if exists removed cascade,
  drop column if exists deleted cascade,
  drop column if exists state cascade,
  alter column project_id set not null,
  alter column person_id set not null,
  add constraint pk_project_person primary key (project_id, person_id);

alter table project_person rename constraint fk_projectparticipant_project_id to fk_project_person_project_id;
alter table project_person rename constraint fk_projectparticipant_person_id to fk_project_person_person_id;

alter index ix_projectparticipant_project_id rename to ix_project_person_project_id;
alter index ix_projectparticipant_person_id rename to ix_project_person_person_id;

# --- !Downs

delete from role_permission;
delete from role_person;
delete from permission;

alter table permission
  drop column if exists action cascade,
  drop column if exists entity_type cascade,
  add column name varchar(255),
  add column description TEXT;

delete from role where project_id is not null;

alter table role
  drop column if exists project_id cascade,
  drop column if exists default_role cascade;

drop index if exists ix_role_project_id;

alter table role_person rename to person_role;

alter table blob add column boot_loader_id uuid;

update blob as t set
  boot_loader_id = c.id
  from (select id, file_id from bootloader)
         as c(id, file_id)
where c.file_id = t.id;

alter table bootloader drop column if exists file_id cascade;

alter table blob
  add constraint uq_blob_boot_loader_id unique (boot_loader_id),
  add constraint fk_blob_boot_loader_id foreign key (boot_loader_id) references bootloader (id) on delete restrict on update restrict;

alter table project_person rename to projectparticipant;

alter table projectparticipant
  drop constraint if exists pk_project_person,
  add column id uuid,
  add column created timestamptz,
  add column updated timestamptz,
  add column removed timestamptz,
  add column deleted boolean default false,
  add column state varchar(7),
  alter column project_id drop not null,
  alter column person_id drop not null;

update projectparticipant set
    id = uuid_generate_v4(),
    created = now(),
    updated = now(),
    deleted = false,
    state = 'OWNER'
  where id is null;

alter table projectparticipant
  alter column id set not null,
  alter column deleted set not null,
  add constraint pk_projectparticipant primary key (id),
  add constraint ck_projectparticipant_state check ( state in ('OWNER','INVITED','ADMIN','MEMBER'));

alter table projectparticipant rename constraint fk_project_person_project_id to fk_projectparticipant_project_id;
alter table projectparticipant rename constraint fk_project_person_person_id to fk_projectparticipant_person_id;

alter index ix_project_person_project_id rename to ix_projectparticipant_project_id;
alter index ix_project_person_person_id rename to ix_projectparticipant_person_id;