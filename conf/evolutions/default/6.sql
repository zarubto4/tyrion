
# --- !Ups

alter table model_version_object
  drop column if exists qr_token cascade;

drop table if exists m_project_program_snapshots cascade;

create table model_mprogram_instance_paramete(

  id                            varchar(255) not null,
  connection_token              varchar(255),
  snapshot_settings             varchar(45),
  m_program_version_id          varchar(255),
  m_project_program_snapshot_id varchar(255),
  constraint pk_model_mprogram_instance_param primary key (id),
  constraint ck_model_mprogram_instance_param check (snapshot_settings in('absolutely_public', 'public_with_token', 'only_for_project_members', 'only_for_project_members_and_imitated_emails'))
);


alter table model_mprogram_instance_paramete add constraint fk_model_mprogram_instance_pa_80 foreign key (m_program_version_id) references model_version_object (id);
create index ix_model_mprogram_instance_pa_80 on model_mprogram_instance_paramete (m_program_version_id);
alter table model_mprogram_instance_paramete add constraint fk_model_mprogram_instance_pa_81 foreign key (m_project_program_snapshot_id) references model_mproject_program_snap_shot (id);
create index ix_model_mprogram_instance_pa_81 on model_mprogram_instance_paramete (m_project_program_snapshot_id);

# --- !Downs

alter table model_version_object
  add column qr_token VARCHAR(255);

drop table if exists model_mprogram_instance_paramete cascade;

drop index if exists ix_model_mprogram_instance_pa_80 cascade;
drop index if exists ix_model_mprogram_instance_pa_81 cascade;

create table m_project_program_snapshots (
  model_mproject_program_snap_shot_id varchar(40) not null,
  model_version_object_id        varchar(255) not null,
  constraint pk_m_project_program_snapshots primary key (model_mproject_program_snap_shot_id, model_version_object_id))
;

alter table m_project_program_snapshots add constraint fk_m_project_program_snapshot_01 foreign key (model_mproject_program_snap_shot_id) references model_mproject_program_snap_shot (id);

alter table m_project_program_snapshots add constraint fk_m_project_program_snapshot_02 foreign key (model_version_object_id) references model_version_object (id);