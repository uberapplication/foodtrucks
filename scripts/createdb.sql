create database foodtrucks encoding 'UTF-8' lc_collate 'en_US.UTF-8' lc_ctype 'en_US.UTF-8' template template0;

\c foodtrucks ;
create extension postgis;
create extension postgis_topology;


create table food_trucks(
  id serial not null primary key,
  location_id integer not null,
  applicant varchar not null,
  facility_type varchar not null,
  status varchar not null,
  food_items text not null,
  geom  geometry(point, 4326)
);

CREATE INDEX ON food_trucks USING GIST(geom);