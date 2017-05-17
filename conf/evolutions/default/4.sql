
/* Verze 1.9.2 */

# --- !Ups
ALTER TABLE model_library
  RENAME removed TO removed_by_user;

alter table model_library
  drop column if exists markdown_description cascade;


alter table model_grid_widget
  add column removed_by_user BOOLEAN DEFAULT FALSE;

alter table model_grid_widget_version
  add column removed_by_user BOOLEAN DEFAULT FALSE;


# --- !Downs
ALTER TABLE model_library
  RENAME removed_by_user TO removed;

alter table model_library
  add column markdown_description Text;


alter table model_grid_widget
  DROP column removed_by_user CASCADE;

alter table model_grid_widget_version
  DROP column removed_by_user CASCADE;