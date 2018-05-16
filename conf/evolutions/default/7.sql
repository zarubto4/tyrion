# --- !Ups

ALTER TABLE gsm
  ADD column msi_number bigint,
  ADD column json_bootloader_core_configuration TEXT,
  ADD column blocked boolean default TRUE not null,
  DROP column msinumber,
  DROP column private_additional_information;


# --- !Downs

ALTER TABLE gsm
  ADD column msinumber varchar(255),
  ADD column private_additional_information TEXT,
  DROP column msi_number,
  DROP column blocked;
