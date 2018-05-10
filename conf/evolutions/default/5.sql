# --- !Ups

create table article (
  id                            uuid not null,
  created                       timestamptz,
  updated                       timestamptz,
  removed                       timestamptz,
  name                          varchar(255),
  description                   TEXT,
  mark_down_text                TEXT,
  author_id                     uuid,
  deleted                       boolean default false not null,
  constraint pk_article primary key (id)
);

create table article_tag (
  article_id                    uuid not null,
  tag_id                        uuid not null,
  constraint pk_article_tag primary key (article_id,tag_id)
);

alter table article_tag add constraint fk_article_tag_article foreign key (article_id) references article (id) on delete restrict on update restrict;
create index ix_article_tag_article on article_tag (article_id);

alter table article_tag add constraint fk_article_tag_tag foreign key (tag_id) references tag (id) on delete restrict on update restrict;
create index ix_article_tag_tag on article_tag (tag_id);



# --- !Downs

alter table if exists article_tag drop constraint if exists fk_article_tag_article;
drop index if exists ix_article_tag_article;

alter table if exists article_tag drop constraint if exists fk_article_tag_tag;
drop index if exists ix_article_tag_tag;

drop table if exists article cascade;
drop table if exists article_tag cascade;