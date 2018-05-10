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

create table gsm_tag (
  gsm_id                   uuid not null,
  tag_id                   uuid not null,
  constraint pk_gsm_tag primary key (gsm_id,tag_id)
);

alter table gsm add constraint fk_gsm_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_gsm_project_id on gsm (project_id);

alter table gsm_tag add constraint fk_gsm_tag_gsm foreign key (gsm_id) references gsm (id) on delete restrict on update restrict;
create index ix_gsm_tag_gsm on gsm_tag (gsm_id);

alter table gsm_tag add constraint fk_gsm_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_gsm_tag_tag on gsm_tag (tag_id);



# --- !Downs

alter table if exists gsm drop constraint if exists fk_gsm_project_id;
drop index if exists ix_gsm_project_id;

alter table if exists gsm_tag drop constraint if exists fk_gsm_tag_gsm;
drop index if exists ix_gsm_tag_gsm;

alter table if exists gsm_tag drop constraint if exists fk_gsm_tag_tag;
drop index if exists ix_gsm_tag_tag;

drop table if exists gsm cascade;

drop table if exists gsm_tag cascade;