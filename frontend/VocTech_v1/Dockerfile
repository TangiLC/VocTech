# Stage 1: Build de l'application Angular
FROM node:20-alpine AS build

# Arguments pour les labels dynamiques
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
ARG BUILD_NUMBER

WORKDIR /app

# Installation des dépendances système pour node-gyp si nécessaire
RUN apk add --no-cache python3 make g++

# Copie des fichiers de configuration
COPY package*.json ./

# Installation des dépendances
RUN npm ci --only=production=false

# Copie du code source
COPY . .

# Build de l'application pour la production
RUN npm run build

# Stage 2: Image de production avec Nginx
FROM nginx:alpine AS production

# Labels statiques
LABEL maintainer="tangi@le-cadre.net"
LABEL name="VocTech Archeology Frontend"
LABEL description="Frontend Angular pour l'application VocTech Archeology"
LABEL vendor="VocTech"

# Labels OCI avec valeurs dynamiques
LABEL org.opencontainers.image.title="VocTech Archeology Frontend"
LABEL org.opencontainers.image.description="Interface utilisateur Angular pour la gestion du vocabulaire technique d'archéologie"
LABEL org.opencontainers.image.version=${VERSION:-"0.0.0"}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${VCS_REF}
LABEL org.opencontainers.image.authors="tangi@le-cadre.net"
LABEL org.opencontainers.image.vendor="VocTech"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.source="https://github.com/TangiLC/VocTech"
LABEL org.opencontainers.image.documentation="https://github.com/TangiLC/VocTech/blob/main/README.md"
LABEL org.opencontainers.image.url="https://github.com/TangiLC/VocTech"

# Labels techniques
LABEL node.version="20"
LABEL angular.version="20.0.0"
LABEL framework="Angular"
LABEL build.number=${BUILD_NUMBER}

# Copie des fichiers buildés depuis le stage précédent
COPY --from=build /app/dist/voc-tech-repair/browser /usr/share/nginx/html

# Configuration Nginx personnalisée pour Angular
COPY nginx.conf /etc/nginx/nginx.conf

# Exposition du port 4200 (pour cohérence avec le développement)
EXPOSE 4200

# Commande de démarrage
CMD ["nginx", "-g", "daemon off;"]

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:4200/ || exit 1