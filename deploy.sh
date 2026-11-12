#!/usr/bin/env bash
# ==============================================================================
# Script de Build e Deploy Rápido do Auth Service para Docker Hub e Render
# Uso: 
#   export DOCKER_USER="rlc02"
#   export DOCKER_TOKEN="seu_token"
#   export RENDER_API_KEY="seu_render_token"
#   ./deploy.sh
# ==============================================================================

set -e

DOCKER_USER="${DOCKER_USER:-rlc02}"
DOCKER_PASS="${DOCKER_TOKEN}"
RENDER_API_KEY="${RENDER_API_KEY}"
SERVICE="auth-service"
IMAGE_TAG="$DOCKER_USER/bingo-auth-service:latest"

echo "=========================================================="
echo " 1. Compilando $SERVICE com Maven"
echo "=========================================================="
mvn clean package -pl "$SERVICE" -am -DskipTests

echo ""
echo "=========================================================="
echo " 2. Autenticando no Docker Hub ($DOCKER_USER)"
echo "=========================================================="
if [ -n "$DOCKER_PASS" ]; then
  echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
else
  echo "DOCKER_TOKEN não definido. Usando sessão Docker existente..."
fi

echo ""
echo "=========================================================="
echo " 3. Buildando e Enviando Imagem Docker para Docker Hub"
echo "=========================================================="
docker build \
  -f Dockerfile.jvm \
  --build-arg SERVICE="$SERVICE" \
  -t "$IMAGE_TAG" .

echo "--> Enviando $IMAGE_TAG para o Docker Hub..."
docker push "$IMAGE_TAG"
echo "✔ $IMAGE_TAG publicado com sucesso no Docker Hub!"

echo ""
echo "=========================================================="
echo " 4. Acionando Deploy no Render via API"
echo "=========================================================="
if [ -n "$RENDER_API_KEY" ]; then
  echo "Buscando serviços no Render..."
  SERVICES_JSON=$(curl -s -H "Authorization: Bearer $RENDER_API_KEY" "https://api.render.com/v1/services?limit=10" || true)
  SERVICE_ID=$(echo "$SERVICES_JSON" | grep -o '"id":"srv-[^"]*"' | head -n 1 | cut -d'"' -f4 || true)
  
  if [ -n "$SERVICE_ID" ]; then
    echo "Disparando deploy para $SERVICE_ID..."
    curl -s -X POST \
      -H "Authorization: Bearer $RENDER_API_KEY" \
      -H "Content-Type: application/json" \
      -d '{"clearCache": "clear"}' \
      "https://api.render.com/v1/services/$SERVICE_ID/deploys"
    echo ""
    echo "Deploy acionado no Render com sucesso!"
  fi
fi

echo ""
echo "=========================================================="
echo " Processo Concluído com Sucesso!"
echo "=========================================================="
