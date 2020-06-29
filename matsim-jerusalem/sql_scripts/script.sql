--alter table households 
--add primary key (hhid);
--alter table persons 
--add primary key (hhid,pnum);
--alter table trips
--add primary key (hhid,pnum,persontripnum );
alter table households
add constraint hhid_unique UNIQUE(hhid);
alter table persons 
add constraint for_households foreign key (hhid) references households(hhid);
--alter table trips
--add constraint for_persons foreign key (hhid,pnum) references households(hhid,pnum);