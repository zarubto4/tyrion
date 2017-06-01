
# --- !Ups

alter table b_program_version_snapshots DROP CONSTRAINT fk_b_program_version_snapshot_01 CASCADE;
alter table model_mprogram_instance_paramete DROP CONSTRAINT fk_model_mprogram_instance_pa_81 CASCADE;

alter table b_program_version_snapshots      alter column model_mproject_program_snap_shot_id type uuid using cast(model_mproject_program_snap_shot_id as uuid);
alter table model_mprogram_instance_paramete alter column m_project_program_snapshot_id type uuid using cast(m_project_program_snapshot_id as uuid);
alter table model_mprogram_instance_paramete alter column id type uuid using cast(id as uuid);
alter table model_mproject_program_snap_shot alter column id type uuid using cast(id as uuid);

alter table b_program_version_snapshots ADD CONSTRAINT fk_b_program_version_snapshot_01 foreign key (model_mproject_program_snap_shot_id) references model_mproject_program_snap_shot (id);
alter table model_mprogram_instance_paramete add constraint fk_model_mprogram_instance_pa_81 foreign key (m_project_program_snapshot_id) references model_mproject_program_snap_shot (id);


create sequence model_mproject_program_snap__seq;
create sequence model_mprogram_instance_para_seq;

# --- !Downs

alter table b_program_version_snapshots DROP CONSTRAINT fk_b_program_version_snapshot_01 CASCADE;
alter table model_mprogram_instance_paramete DROP CONSTRAINT fk_model_mprogram_instance_pa_81 CASCADE;

alter table model_mprogram_instance_paramete alter column m_project_program_snapshot_id type VARCHAR(255) using cast(m_project_program_snapshot_id as VARCHAR(255));
alter table b_program_version_snapshots      alter column model_mproject_program_snap_shot_id type VARCHAR(255) using cast(model_mproject_program_snap_shot_id as VARCHAR(255));
alter table model_mprogram_instance_paramete alter column id type VARCHAR(255) using cast(id as VARCHAR(255));
alter table model_mproject_program_snap_shot alter column id type VARCHAR(255) using cast(id as VARCHAR(255));

alter table b_program_version_snapshots ADD CONSTRAINT fk_b_program_version_snapshot_01 foreign key (model_mproject_program_snap_shot_id) references model_mproject_program_snap_shot (id);
alter table model_mprogram_instance_paramete add constraint fk_model_mprogram_instance_pa_81 foreign key (m_project_program_snapshot_id) references model_mproject_program_snap_shot (id);

drop sequence model_mproject_program_snap__seq CASCADE;
drop sequence model_mprogram_instance_para_seq CASCADE;