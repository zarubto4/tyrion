
# --- !Ups


alter table typeofboardbatch
  add column description TEXT default '';


# --- !Downs

alter table typeofboardbatch
  drop column if exists description cascade;
