select service, count(*)
from ord group by service


-- Ask below X USD/BTC
select o."time", sum(o.amount), count(*), max(o.price) as up_to_price, max(t.last) as current_price, max(o.ordertype) as type, max(o.service) as service
from ord o
left join tick t on t.timestamp = o.time and t.currency = o.currency and t.service = o.service
where o.currency = 'USD'
 and o.service = 'MtGoxExchange'
 and o.ordertype = 'ASK'
 and o.price <= 16000
group by o."time"
order by o."time" desc

-- Bids above X USD/BTC in USD
select o."time", sum(o.amount * o.price), count(*), min(o.price) as above_price, max(t.last) as current_price, max(o.ordertype) as type, max(o.service) as service
from ord o
left join tick t on t.timestamp = o.time and t.currency = o.currency and t.service = o.service
where o.currency = 'USD'
 and o.service = 'MtGoxExchange'
 and o.ordertype = 'BID'
 and o.price >= 00
group by o."time"
order by o."time" desc


