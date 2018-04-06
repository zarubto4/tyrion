
# --- !Ups

alter table hardwareupdate
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check (state in('NOT_YET_STARTED', 'PROHIBITED_BY_CONFIG', 'IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')) not valid,
  validate constraint ck_hardwareupdate_state;

# --- !Downs
alter table hardwareupdate
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check (state in('NOT_YET_STARTED','IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')) not valid;

update hardwareupdate set state = 'OBSOLETE' where state = 'PROHIBITED_BY_CONFIG';

alter table hardwareupdate
  validate constraint ck_hardwareupdate_state;