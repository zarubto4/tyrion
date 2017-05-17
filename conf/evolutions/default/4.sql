
/* Verze 1.9.2 */

# --- !Ups
ALTER TABLE model_library RENAME removed TO removed_by_user;


# --- !Downs
ALTER TABLE model_library RENAME removed_by_user TO removed;