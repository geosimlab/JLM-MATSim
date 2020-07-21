select hhid,pnum,persontripnum, origtaz , origpurp ,desttaz ,destpurp,modecode,concat(finaldepartminute+180,' minutes')::interval  
from trips 
where hhid = 37840 and pnum = 4;
select * from taz600 t2  where taz ='5002'
select * from taz600 t where taz = 5214;
select concat(finaldepartminute,' minutes')::interval from trips t2; 
select * from persons where hhid in (select hhid from households h2 where hometaz >1000);
select * from links where length_met < 150 and type not in (9,10,66);