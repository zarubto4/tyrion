
# --- !Ups

create table hardwarereleaseupdate (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  deleted                       boolean default false not null,
  project_id                    uuid not null,
  name                          varchar(255),
  description                   TEXT,
  author_id                     uuid,
  constraint pk_hardwarereleaseupdate primary key (id)
);


create table hardwarereleaseupdate_tag (
  hardware_release_update_id     uuid not null,
  tag_id                        uuid not null,
  constraint pk_hardwarereleaseupdate_tag primary key (hardware_release_update_id, tag_id)
);


alter table hardwarereleaseupdate_tag add constraint fk_hardwarereleaseupdate_tag_hardwarereleaseupdate foreign key (hardware_release_update_id) references hardwarereleaseupdate (id) on delete restrict on update restrict;
create index ix_hardwarereleaseupdate_tag_hardwarereleaseupdate on hardwarereleaseupdate_tag (hardware_release_update_id);

alter table hardwarereleaseupdate_tag add constraint fk_hardwarereleaseupdate_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_hardwarereleaseupdate_tag_tag on hardwarereleaseupdate_tag (tag_id);


# --- !Downs

drop table if exists hardwarereleaseupdate cascade;

alter table if exists hardwarereleaseupdate_tag drop constraint if exists fk_hardwarereleaseupdate_tag_hardwarereleaseupdate;
drop index if exists ix_hardwarereleaseupdate_tag_hardwarereleaseupdate;

alter table if exists hardwarereleaseupdate_tag drop constraint if exists fk_hardwarereleaseupdate_tag_tag;
drop index if exists ix_hardwarereleaseupdate_tag_tag;


drop table if exists hardwarereleaseupdate_tag cascade;
