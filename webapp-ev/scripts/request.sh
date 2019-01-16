#!/bin/bash
# Helper script that create a ev request.
# Use like ./request.sh <energy> <date> <time> <duration>
# E.g.: ./request.sh 99.9 2019-01-16 23:00 5

URL=http://localhost:8080/app/api/v1/request/create

ARG_1=$1
ARG_2=$2
ARG_3=$3
ARG_4=$4

generate_json() {
cat <<EOF
{
    "energy": "$ARG_1",
    "date": "$ARG_2",
    "time": "$ARG_3",
    "duration": "$ARG_4"
}
EOF
}

curl \
-H "Content-Type: application/json" \
-d "$(generate_json)" \
$URL

echo ""
