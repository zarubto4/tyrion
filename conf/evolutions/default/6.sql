

/* Verze 1.9.5 */

# --- !Ups

alter table model_version_object
  DROP column if exists qr_token CASCADE;

drop table if exists m_project_program_snapshots cascade;

CREATE table model_mprogram_instance_parameter(

  id                        VARCHAR(40) NOT NULL,
  connection_token          VARCHAR(255),

  CONSTRAINT ck_model_mprogram_instance_parameter_snapshot_settings CHECK (state IN
                                                                           ('absolutely_public', 'public_with_token', 'only_for_project_members', 'only_for_project_members_and_imitated_emails')),

  CONSTRAINT pk_model_mprogram_instance_parameter primary key (id),

  m_program_version_id          VARCHAR(40),
  m_project_program_snapshot_id VARCHAR(40),

  constraint  fk_model_mprogram_instance_parameter_m_program_version_id foreign key (m_program_version_id) references model_version_object (id),
  constraint  fk_model_mprogram_instance_parameter_m_project_program_snapshot_id foreign key (m_project_program_snapshot_id) references model_mproject_program_snap_shot (id)

);

create index ix_model_mprogram_instance_parameter_m_program_version_id  on model_mprogram_instance_parameter (m_program_version_id);
create index ix_model_mprogram_instance_parameter_m_project_program_snapshot_id  on model_mprogram_instance_parameter (m_project_program_snapshot_id);


# --- !Downs

alter table model_version_object
  ADD column qr_token VARCHAR(255);

create table m_project_program_snapshots (
  model_mproject_program_snap_shot_id varchar(40) not null,
  model_version_object_id        varchar(255) not null,
  constraint pk_m_project_program_snapshots primary key (model_mproject_program_snap_shot_id, model_version_object_id))
;

drop table if exists model_mprogram_instance_parameter cascade;