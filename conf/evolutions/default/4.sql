
# --- !Ups

alter table HomerServer
  drop COLUMN IF EXISTS mqtt_username,
  drop COLUMN IF EXISTS mqtt_password;


alter table Board
  add column mqtt_username varchar(255) default '$2a$12$Nt/QGfrOst2rG4jeSKVtxeAsbm09zvJKikvQQjkIDFUGnsIUFwUIC',
  add column mqtt_password varchar(255) default '$2a$12$SJdmqnPiqjGCDrssaTPvx.Fkr/J9vC27cjD3yFQ3K6rKJoSxdEuuO';


# --- !Downs

alter table HomerServer
  add COLUMN mqtt_username varchar(255),
  add COLUMN mqtt_password varchar(255);


alter table Board
  drop column if exists mqtt_username cascade,
  drop column if exists mqtt_password cascade;