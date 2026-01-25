CMD="docker run --rm --name rest-easy-demo-pg -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_DB=app -p 5432:5432 postgres"

echo "$CMD"
eval "$CMD"

