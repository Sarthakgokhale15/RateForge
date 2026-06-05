#!/usr/bin/env bash
set -euo pipefail

TIER=${1:-free}
COUNT=${2:-15}

echo "Testing tier: $TIER, requests: $COUNT"
for i in $(seq 1 $COUNT); do
  resp=$(curl -s -D - -o /dev/null -H "X-User-Tier: $TIER" -H "X-User-Id: user-$TIER" http://localhost:8081/hello || true)
  status=$(echo "$resp" | head -n1 | awk '{print $2}')
  policy=$(echo "$resp" | grep -i "X-RateLimit-Policy" | awk -F": " '{print $2}' | tr -d '\r')
  remaining=$(echo "$resp" | grep -i "X-RateLimit-Remaining" | awk -F": " '{print $2}' | tr -d '\r')
  echo "#${i} status=${status:-200} policy=${policy:-} remaining=${remaining:-}" 
  sleep 0.2
done

echo "Done"
