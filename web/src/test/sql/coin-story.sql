-- Bids above X USD/BTC in USD
select o."time", sum(o.amount * o.price *
  (case when o.currency = 'EUR' then 1.35 when o.currency = 'CNY' then 0.164 when o.currency = 'USD' then 1 else 0 end)
) as bid_total_usd, count(*), min(o.price) as above_price, 
	(select avg(t.last) from tick t where t.timestamp = o.time and t.currency = 'USD' and t.service = 'BitstampExchange') as current_price,
max(o.ordertype) as type, count(distinct (o.service || '###' || o.currency)) as services
from ord o
where o.ordertype = 'BID'
 and o.price >= 00
 and time > '2013-11-01 00:00:00'
-- and service = 'BitstampExchange'
group by o."time"
--having count(distinct o.service) >= 4
order by o."time" desc


-- Ask below X USD/BTC
select o."time", sum(o.amount) as ask_total_btc, count(*), max(o.price) as up_to_price, 
	(select avg(t.last) from tick t where t.timestamp = o.time and t.currency = 'USD' and t.service = 'BitstampExchange') as current_price,
max(o.ordertype) as type, count(distinct (o.service || '###' || o.currency)) as services
from ord o
where o.ordertype = 'ASK'
 and time > '2013-11-01 00:00:00'
-- and o.price <= 20000
-- and service = 'BitstampExchange'
group by o."time"
--having count(distinct o.service) >= 4
order by o."time" desc

select service, last, currency, timestamp
from tick 
where currency='EUR'
--and service = 'Bitcoin24Exchange'
order by timestamp desc, service, currency
limit 100

select service, last, currency, timestamp
from tick 
where currency='USD'
and service = 'MtGoxExchange'
order by timestamp desc, service, currency
limit 100


select service, last, currency, timestamp
from tick 
where currency='USD'
--and service = 'BitstampExchange'
order by timestamp desc, service, currency
limit 100

