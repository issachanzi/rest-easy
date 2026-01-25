#!/bin/bash

CMD="curl --request POST \
  --url http://localhost:7070/api/TodoItem \
  --header 'content-type: application/json' \
  --data '{
  \"text\": \"Walk the dog\"
}
'"

echo "$CMD"
eval "$CMD"

