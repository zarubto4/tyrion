

# --- !Ups

alter TABLE hardwareupdate
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check (type in('NOT_YET_STARTED', 'PROHIBITED_BY_CONFIG', 'IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')) not valid;

# --- !Downs
alter TABLE hardwareupdate
  drop constraint ck_hardwareupdate_state,
  add constraint ck_hardwareupdate_state check (type in('NOT_YET_STARTED','IN_PROGRESS','NOT_UPDATED','COMPLETE','INSTANCE_INACCESSIBLE','WAITING_FOR_DEVICE','CANCELED','BIN_FILE_MISSING','OBSOLETE','HOMER_SERVER_IS_OFFLINE','CRITICAL_ERROR','HOMER_SERVER_NEVER_CONNECTED')) not valid;
