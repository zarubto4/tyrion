
/* Verze 1.9.2 */

# --- !Ups
ALTER TABLE model_library
  RENAME removed TO removed_by_user;

alter table model_library
  drop column if exists markdown_description cascade;

# --- !Downs
ALTER TABLE model_library
  RENAME removed_by_user TO removed;

alter table model_library
  add column markdown_description Text;
