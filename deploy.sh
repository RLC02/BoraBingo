#!/usr/bin/env bash
# ==============================================================================
# Script de Build e Deploy Local/Resiliente para Docker Hub e Render
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

SERVICES=("gateway-service" "auth-service" "bingo-sala-service" "cartela-service")

echo "=========================================================="
echo " 1. Compilando os Microsserviços com Maven"
echo "=========================================================="
mvn clean package -DskipTests

echo ""
echo "=========================================================="
echo " 2. Autenticando no Docker Hub ($DOCKER_USER)"
echo "=========================================================="
if [ -n "$DOCKER_PASS" ]; then
  echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
else
  echo "DOCKER_TOKEN não definido no ambiente. Usando sessão atual do Docker..."
fi

echo ""
echo "=========================================================="
echo " 3. Buildando e Enviando Imagens Docker (Linux/AMD64 para Render)"
echo "=========================================================="
for SVC in "${SERVICES[@]}"; do
  IMAGE_TAG="$DOCKER_USER/bingo-$SVC:latest"
  echo "--> Processando $SVC ($IMAGE_TAG)..."
  
  # Build
  docker build \
    --platform linux/amd64 \
    -f Dockerfile.jvm \
    --build-arg SERVICE="$SVC" \
    -t "$IMAGE_TAG" .
    
  # Push
  echo "Enviando $IMAGE_TAG para o Docker Hub..."
  docker push "$IMAGE_TAG"
  echo "✔ $SVC publicado com sucesso!"
done

echo ""
echo "=========================================================="
echo " 4. Acionando Deploy no Render via API"
echo "=========================================================="
if [ -n "$RENDER_API_KEY" ]; then
  echo "Buscando serviços ativos no Render..."
  SERVICES_JSON=$(curl -s -H "Authorization: Bearer $RENDER_API_KEY" "https://api.render.com/v1/services?limit=20" || true)
  SERVICE_IDS=$(echo "$SERVICES_JSON" | grep -o '"id":"srv-[^"]*"' | cut -d'"' -f4 | sort -u || true)
  
  if [ -n "$SERVICE_IDS" ]; then
    for SRV in $SERVICE_IDS; do
      echo "Disparando deploy para $SRV..."
      curl -s -X POST \
        -H "Authorization: Bearer $RENDER_API_KEY" \
        -H "Content-Type: application/json" \
        -d '{"clearCache": "clear"}' \
        "https://api.render.com/v1/services/$SRV/deploys"
      echo ""
    done
  else
    echo "Nenhum serviço encontrado via API ou serviços configurados por Deploy Hooks/Auto-deploy."
  fi
fi

echo ""
echo "=========================================================="
echo " Concluído com Sucesso!"
echo "=========================================================="
