
# --- !Ups


alter table versionobject
  add column additional_configuration TEXT;

# --- !Downs

alter table versionobject
  drop column additional_configuration cascade;
