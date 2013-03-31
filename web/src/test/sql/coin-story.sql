select service, count(*)
from ord group by service


select cast(price / 50 as integer) , sum(amount), "time"
from ord
where service = 'MtGoxExchange' and ordertype = 'BID'
group by "time", cast(price / 50 as integer)
having cast(price / 50 as integer) >= 2
order by cast(price / 50 as integer) desc, "time" desc

select cast(price / 50 as integer) , sum(amount), "time"
from ord
where service = 'MtGoxExchange' and ordertype = 'ASK'
and price < 1000
group by "time", cast(price / 50 as integer)
having cast(price / 50 as integer) <= 3
order by cast(price / 50 as integer) asc, "time" desc

