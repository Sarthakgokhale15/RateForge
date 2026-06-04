local bucket_key = KEYS[1]
local capacity = tonumber(ARGV[1])
local leak_rate = tonumber(ARGV[2])
local requested_units = tonumber(ARGV[3])
local now_millis = tonumber(ARGV[4])
local ttl_seconds = tonumber(ARGV[5])

local data = redis.call('HMGET', bucket_key, 'water', 'last_leak_millis')
local water = tonumber(data[1])
local last_leak_millis = tonumber(data[2])

if water == nil then
  water = 0
  last_leak_millis = now_millis
end

local elapsed_millis = math.max(0, now_millis - last_leak_millis)
local leaked = (elapsed_millis / 1000.0) * leak_rate
water = math.max(0, water - leaked)

local allowed = 0
local retry_after = 0
if (water + requested_units) <= capacity then
  water = water + requested_units
  allowed = 1
else
  local overflow = (water + requested_units) - capacity
  retry_after = math.ceil(overflow / leak_rate)
end

redis.call('HMSET', bucket_key, 'water', tostring(water), 'last_leak_millis', tostring(now_millis))
redis.call('EXPIRE', bucket_key, ttl_seconds)

return allowed .. ':' .. math.floor(math.max(0, capacity - water)) .. ':' .. retry_after
