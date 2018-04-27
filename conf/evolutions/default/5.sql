# --- !Ups

create table gsm (
 id                            uuid not null,
 created                       timestamptz,
 updated                       timestamptz,
 removed                       timestamptz,
 name                          varchar(255),
 description                   TEXT,
 MSINumber                     varchar(255),
 provider                      varchar(255),
 private_additional_information varchar(255),
 registration_hash             uuid,
 author_id                     uuid,
 project_id                    uuid,
 deleted                       boolean default false not null,
 constraint pk_gsm primary key (id)
);

alter table gsm add constraint fk_gsm_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_gsm_project_id on gsm (project_id);

# --- !Downs

alter table if exists gsm drop constraint if exists fk_gsm_project_id;
drop index if exists ix_gsm_project_id;
