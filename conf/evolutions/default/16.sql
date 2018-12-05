
# --- !Ups

alter table hardwareupdate
  add column planned timestamptz,
  add column tracking_id uuid,
  drop column if exists actualization_procedure_id cascade,
  drop constraint if exists fk_hardwareupdate_actualization_procedure_id cascade,
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check ( state in ('PENDING','RUNNING','COMPLETE','CANCELED','OBSOLETE','FAILED')) not valid;

alter table hardwareupdate rename column date_of_finish to finished;

update hardwareupdate as t set state = c.new_state
from (values
             ('NOT_YET_STARTED', 'PENDING'),
             ('IN_PROGRESS', 'RUNNING'),
             ('BIN_FILE_MISSING', 'FAILED'),
             ('PROHIBITED_BY_CONFIG', 'FAILED'),
             ('NOT_UPDATED', 'FAILED'),
             ('WAITING_FOR_DEVICE', 'FAILED'),
             ('INSTANCE_INACCESSIBLE', 'FAILED'),
             ('HOMER_SERVER_IS_OFFLINE', 'FAILED'),
             ('HOMER_SERVER_NEVER_CONNECTED', 'FAILED'),
             ('CRITICAL_ERROR', 'FAILED')
     ) as c(old_state, new_state)
where c.old_state = t.state;

alter table hardwareupdate validate constraint ck_hardwareupdate_state;

update hardwareupdate set planned = now() where planned isnull;

drop index if exists ix_hardwareupdate_actualization_procedure_id cascade;
drop index if exists ix_updateprocedure_instance_id cascade;
drop table if exists updateprocedure cascade;

# --- !Downs

create table updateprocedure (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  state                         varchar(21),
  instance_id                   uuid,
  date_of_planing               timestamptz,
  date_of_finish                timestamptz,
  type_of_update                varchar(41),
  project_id                    uuid,
  size                          integer,
  deleted                       boolean default false not null,
  constraint ck_updateprocedure_state check ( state in ('SUCCESSFULLY_COMPLETE','IN_PROGRESS','COMPLETE','NOT_START_YET','CANCELED','COMPLETE_WITH_ERROR')),
  constraint ck_updateprocedure_type_of_update check ( type_of_update in ('AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE','AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE','MANUALLY_RELEASE_MANAGER','MANUALLY_BY_USER_BLOCKO_GROUP_ON_TIME','MANUALLY_BY_USER','MANUALLY_BY_USER_BLOCKO_GROUP')),
  constraint pk_updateprocedure primary key (id)
);

alter table hardwareupdate
  drop column if exists planned cascade,
  drop column if exists tracking_id cascade,
  add column actualization_procedure_id uuid,
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check (state in('NOT_YET_STARTED', 'PROHIBITED_BY_CONFIG', 'IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')) not valid;

alter table hardwareupdate rename column finished to date_of_finish;

update hardwareupdate as t set state = c.new_state
from (values
             ('PENDING', 'NOT_YET_STARTED'),
             ('RUNNING', 'IN_PROGRESS'),
             ('FAILED', 'CRITICAL_ERROR')
     ) as c(old_state, new_state)
where c.old_state = t.state;

alter table hardwareupdate validate constraint ck_hardwareupdate_state;

alter table updateprocedure add constraint fk_updateprocedure_instance_id foreign key (instance_id) references instancesnapshot (id) on delete restrict on update restrict;
create index ix_updateprocedure_instance_id on updateprocedure (instance_id);

alter table hardwareupdate add constraint fk_hardwareupdate_actualization_procedure_id foreign key (actualization_procedure_id) references updateprocedure (id) on delete restrict on update restrict;
create index ix_hardwareupdate_actualization_procedure_id on hardwareupdate (actualization_procedure_id);
