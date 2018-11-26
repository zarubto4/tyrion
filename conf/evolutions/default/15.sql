
# --- !Ups


create table hardwaregroup_tag (
  hardwaregroup_id              uuid not null,
  tag_id                        uuid not null,
  constraint pk_hardwaregroup_tag primary key (hardwaregroup_id,tag_id)
);


alter table hardwaregroup_tag add constraint fk_hardwaregroup_tag_hardwaregroup foreign key (hardwaregroup_id) references hardwaregroup (id) on delete restrict on update restrict;
create index ix_hardwaregroup_tag_hardwaregroup on hardwaregroup_tag (hardwaregroup_id);

alter table hardwaregroup_tag add constraint fk_hardwaregroup_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_hardwaregroup_tag_tag on hardwaregroup_tag (tag_id);


# --- !Downs

alter table if exists hardwaregroup_tag drop constraint if exists fk_hardwaregroup_tag_hardwaregroup;
drop index if exists ix_hardwaregroup_tag_hardwaregroup;

alter table if exists hardwaregroup_tag drop constraint if exists fk_hardwaregroup_tag_tag;
drop index if exists ix_hardwaregroup_tag_tag;

drop table if exists hardwaregroup_tag cascade;