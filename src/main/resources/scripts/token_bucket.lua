local bucket_key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local requested_tokens = tonumber(ARGV[3])
local now_millis = tonumber(ARGV[4])
local ttl_seconds = tonumber(ARGV[5])

local data = redis.call('HMGET', bucket_key, 'tokens', 'last_refill_millis')
local tokens = tonumber(data[1])
local last_refill_millis = tonumber(data[2])

if tokens == nil then
  tokens = capacity
  last_refill_millis = now_millis
end

local elapsed_millis = math.max(0, now_millis - last_refill_millis)
local refill = (elapsed_millis / 1000.0) * refill_rate
tokens = math.min(capacity, tokens + refill)

local allowed = 0
local retry_after = 0
if tokens >= requested_tokens then
  tokens = tokens - requested_tokens
  allowed = 1
else
  local missing = requested_tokens - tokens
  retry_after = math.ceil(missing / refill_rate)
end

redis.call('HMSET', bucket_key, 'tokens', tostring(tokens), 'last_refill_millis', tostring(now_millis))
redis.call('EXPIRE', bucket_key, ttl_seconds)

return allowed .. ':' .. math.floor(tokens) .. ':' .. retry_after
