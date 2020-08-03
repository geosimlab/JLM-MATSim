select hhid,pnum,persontripnum, origtaz , origpurp ,desttaz ,destpurp,modecode,concat(finaldepartminute+180,' minutes')::interval  
from trips 
where hhid = 37840 and pnum = 4;
select * from taz600 t2  where taz ='5002'
select * from taz600 t where taz = 5214;
select concat(finaldepartminute,' minutes')::interval from trips t2; 
select * from persons where hhid in (select hhid from households h2 where hometaz >1000);
select * from links where length_met < 150 and type not in (9,10,66);
with first_table as(
select distinct hhid,pnum from trips where origtaz < 1000
), second_table as(
select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips 
) select second_table.* 
from first_table
left join second_table
using (hhid,pnum);



with first_table as(select distinct hhid,pnum from trips where origtaz in (3,14,15,304,409)), second_table as(select hhid,pnum,personTripNum,origpurp,finalDepartMinute,origtaz,modeCode from trips) select count(second_table.*) from first_table left join second_table using (hhid,pnum);
select lines from lines where lines not in (select line from detailed_headway);
select line from detailed_headway where line not in (select lines from lines);
select * from readable_headway rh where start_time like '21%';

