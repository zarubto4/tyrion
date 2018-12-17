
# --- !Ups

alter table hardwareupdate
  add column tracking_id_snapshot_id uuid,
  add column tracking_id_instance_id uuid,
  add column tracking_id_project_id uuid,
  add column tracking_release_procedure_id uuid,

  add column type_of_update                varchar(120),
  add constraint ck_hardwareupdate_type_of_update check ( type_of_update in (
                                                            'MANUALLY_BY_USER_INDIVIDUAL',
                                                            'MANUALLY_RELEASE_MANAGER',
                                                            'MANUALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE',
                                                            'AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE'
                                                            ));


alter table hardwareupdate validate constraint ck_hardwareupdate_type_of_update;

alter table gsm
  add column sim_type                varchar(120),
  add constraint ck_gsm_sim_type check ( sim_type in (
  'CARD',
  'CHIP'
  ));

alter table gsm validate constraint ck_gsm_sim_type;


# --- !Downs

alter table hardwareupdate
  drop column if exists type_of_update cascade,
  drop column if exists tracking_id_snapshot_id cascade,
  drop column if exists tracking_id_instance_id cascade,
  drop column if exists tracking_id_project_id cascade,
  drop column if exists tracking_release_procedure_id cascade,
  drop constraint if exists ck_hardwareupdate_type_of_update cascade;


alter table gsm
  drop column if exists sim_type cascade,
  drop constraint if exists ck_gsm_sim_type cascade;