# --- !Ups

ALTER TABLE gsm
  ADD column msi_number bigint,
  DROP column msinumber;


# --- !Downs

ALTER TABLE gsm
  ADD column msinumber varchar(255),
  DROP column msi_number;
