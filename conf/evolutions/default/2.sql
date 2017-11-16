
# --- !Ups

update CCompilation firmware_build_datetime set firmware_build_datetime = null;
alter table CCompilation alter column firmware_build_datetime type timestamp using firmware_build_datetime::timestamp with time zone;
update CCompilation firmware_build_datetime set firmware_build_datetime = now();

# --- !Downs

alter table CCompilation alter column firmware_build_datetime type varchar(255) using to_char(firmware_build_datetime, 'yyyy-MM-ddTHH:mm:ss.SSSZ')