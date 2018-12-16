
# --- !Ups

create table hardwarreleaseupdate (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  deleted                       boolean default false not null,
  project_id                    uuid not null,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  constraint pk_hardwarreleaseupdate primary key (id)
);


create table hardwarreleaseupdate_tag (
  hardwar_release_update_id     uuid not null,
  tag_id                        uuid not null,
  constraint pk_hardwarreleaseupdate_tag primary key (hardwar_release_update_id, tag_id)
);


alter table hardwarreleaseupdate_tag add constraint fk_hardwarreleaseupdate_tag_hardwarreleaseupdate foreign key (hardwar_release_update_id) references hardwarreleaseupdate (id) on delete restrict on update restrict;
create index ix_hardwarreleaseupdate_tag_hardwarreleaseupdate on hardwarreleaseupdate_tag (hardwar_release_update_id);

alter table hardwarreleaseupdate_tag add constraint fk_hardwarreleaseupdate_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_hardwarreleaseupdate_tag_tag on hardwarreleaseupdate_tag (tag_id);


# --- !Downs

drop table if exists hardwarreleaseupdate cascade;

alter table if exists hardwarreleaseupdate_tag drop constraint if exists fk_hardwarreleaseupdate_tag_hardwarreleaseupdate;
drop index if exists ix_hardwarreleaseupdate_tag_hardwarreleaseupdate;

alter table if exists hardwarreleaseupdate_tag drop constraint if exists fk_hardwarreleaseupdate_tag_tag;
drop index if exists ix_hardwarreleaseupdate_tag_tag;


drop table if exists hardwarreleaseupdate_tag cascade;
