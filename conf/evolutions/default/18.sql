
# --- !Ups

alter table hardwareupdate
  add column type varchar(41),
  add constraint ck_hardwareupdate_type check ( type in (
                                                            'MANUALLY_BY_USER_INDIVIDUAL',
                                                            'MANUALLY_RELEASE_MANAGER',
                                                            'MANUALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_INSTANCE',
                                                            'AUTOMATICALLY_BY_USER_ALWAYS_UP_TO_DATE',
                                                            'AUTOMATICALLY_BY_SERVER_ALWAYS_UP_TO_DATE'
                                                            ));


alter table hardwareupdate validate constraint ck_hardwareupdate_type;

alter table gsm
  add column sim_type varchar(4),
  add constraint ck_gsm_sim_type check ( sim_type in (
  'CARD',
  'CHIP'
  ));

alter table gsm validate constraint ck_gsm_sim_type;


# --- !Downs

alter table hardwareupdate
  drop column if exists type cascade,
  drop constraint if exists ck_hardwareupdate_type cascade;


alter table gsm
  drop column if exists sim_type cascade,
  drop constraint if exists ck_gsm_sim_type cascade;