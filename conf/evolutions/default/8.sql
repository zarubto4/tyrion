


# --- !Ups
alter table model_person
  add column facebook_oauth_id varchar(255),
  add column github_oauth_id varchar(255);
  
# --- !Downs

alter table model_person
  drop column if exists facebook_oauth_id cascade,
  drop column if exists github_oauth_id cascade;