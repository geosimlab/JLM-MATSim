select hhid,pnum,persontripnum, origtaz , origpurp ,desttaz ,destpurp,modecode,concat(finaldepartminute+180,' minutes')::interval  
from trips 
where hhid = 97506 and pnum = 3;
select * from taz600 t where taz = 5214;
select concat(finaldepartminute,' minutes')::interval from trips t2; 