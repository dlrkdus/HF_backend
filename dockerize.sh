BUILD_POSITION=$1
MODE=$2

# docker container stop hf-backend-app || true
# docker container rm hf-backend-app || true

if [ "$MODE" = "no-auth" ]; then
  docker-compose -f docker-compose.yml --env-file envs --profile blue-noauth down --rmi all
else
  docker-compose -f docker-compose.yml --env-file envs --profile blue down --rmi all
fi

if [ "$BUILD_POSITION" = "no-container" ]; then
  ./gradlew clean build -x test
fi

docker build -t rudeh1253/hf-backend:latest .

if [ "$MODE" = "no-auth" ]; then
  docker-compose -f docker-compose.yml --env-file envs --profile blue-noauth up -d --build
else
  docker-compose -f docker-compose.yml --env-file envs --profile blue up -d --build
fi