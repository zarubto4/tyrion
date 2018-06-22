
# --- !Ups

alter table libraryVersion
  add column working_copy boolean default false not null;

alter table gridProgramVersion
  add column working_copy boolean default false not null;

alter table cProgramVersion
  add column working_copy boolean default false not null;

alter table bProgramVersion
  add column working_copy boolean default false not null;

alter table blockVersion
  add column working_copy boolean default false not null;

# --- !Downs

alter table libraryVersion
  drop column if exists working_copy;

alter table gridProgramVersion
  drop column if exists working_copy;

alter table cProgramVersion
  drop column if exists working_copy;

alter table bProgramVersion
  drop column if exists working_copy;

alter table blockVersion
  drop column if exists working_copy;
