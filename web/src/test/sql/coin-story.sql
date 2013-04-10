select service, count(*)
from ord group by service


-- Bids above X USD/BTC in USD
select o."time", sum(o.amount * o.price *
  (case when o.currency = 'EUR' then 1.3 when o.currency = 'USD' then 1 else 0 end)
), count(*), min(o.price) as above_price, 
	(select avg(t.last) from tick t where t.timestamp = o.time and t.currency = 'USD' and t.service = 'MtGoxExchange') as current_price,
max(o.ordertype) as type, count(distinct o.service) as services
from ord o
where o.ordertype = 'BID'
 and o.price >= 00
group by o."time"
having count(distinct o.service) >= 4
order by o."time" desc


-- Ask below X USD/BTC
select o."time", sum(o.amount), count(*), max(o.price) as up_to_price, 
	(select avg(t.last) from tick t where t.timestamp = o.time and t.currency = 'USD' and t.service = 'MtGoxExchange') as current_price,
max(o.ordertype) as type, count(distinct o.service) as services
from ord o
where o.ordertype = 'ASK'
-- and o.price <= 20000
group by o."time"
having count(distinct o.service) >= 4
order by o."time" desc

