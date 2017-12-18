
# --- !Ups


alter table typeofboard
  drop column revision cascade;


# --- !Downs

alter table typeofboard
  add column revision varchar(255) default '';;
