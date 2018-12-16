
# --- !Ups

alter table hardwareupdate
  add column tracking_id_snapshot_id uuid,
  add column tracking_id_instance_id uuid,
  add column tracking_id_project_id uuid,
  add column tracking_release_procedure_id uuid,

  add column type_of_update                varchar(120),
  add constraint ck_updateprocedure_type_of_update check ( type_of_update in (
                                                            'MANUALLY_BY_USER_INDIVIDUAL',
                                                            'MANUALLY_RELEASE_MANAGER',
                                                            'MANUALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE',
                                                            'AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE'
                                                            ));

alter table hardwareupdate validate constraint ck_updateprocedure_type_of_update;

# --- !Downs

alter table hardwareupdate
  drop column if exists type_of_update cascade,
  drop column if exists tracking_id_snapshot_id cascade,
  drop column if exists tracking_id_instance_id cascade,
  drop column if exists tracking_id_project_id cascade,
  drop column if exists tracking_release_procedure_id cascade,
  drop constraint if exists ck_updateprocedure_type_of_update cascade;
