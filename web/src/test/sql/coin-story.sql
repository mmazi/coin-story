select service, count(*)
from ord group by service


-- Ask below X USD/BTC
select  "time", sum(amount),  count(*), max(price), max(ordertype), max(service)
from ord
where currency = 'USD'
 and service = 'MtGoxExchange'
 and ordertype = 'ASK'
 and price <= 160
group by "time"
order by "time" desc

-- Bids above X USD/BTC in USD
select  "time", sum(amount * price),  count(*), min(price), max(ordertype), max(service)
from ord
where currency = 'USD'
 and service = 'MtGoxExchange'
 and ordertype = 'BID'
 and price >= 100
group by "time"
order by "time" desc




select * from tick