
# --- !Ups

alter table gsm

  add column imsi varchar(255),
  add column iccid varchar(255);


# --- !Downs

alter table gsm
 drop imsi,
 drop iccid;

