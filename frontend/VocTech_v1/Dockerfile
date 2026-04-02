# Étape 1 : Build SSR Angular (application + serveur)
FROM node:20-alpine AS build

# Labels dynamiques (optionnels)
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
ARG BUILD_NUMBER

WORKDIR /app

# Dépendances système pour node-gyp
RUN apk add --no-cache python3 make g++

# Copie des fichiers de config
COPY package*.json ./

# Installation complète (dev + prod)
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

# Copie du code source
COPY . .

# Build SSR Angular (client + serveur)
RUN npm run build:ssr

# Étape 2 : Image de production basée sur Node.js (sans nginx)
FROM node:20-alpine AS production

WORKDIR /app

# Copie uniquement les fichiers nécessaires du build précédent
COPY --from=build /app/dist ./dist
COPY --from=build /app/package*.json ./

# Réinstaller uniquement les dépendances nécessaires en prod
RUN if [ -f package-lock.json ]; then npm ci --omit=dev; else npm install --omit=dev; fi

# Expose le port utilisé par Express
EXPOSE 4000

ENV PORT=4000
ENV BACKEND_URL=http://backend:8082

# Labels (facultatif)
LABEL maintainer="tangi@le-cadre.net"
LABEL org.opencontainers.image.title="VocTech Archeology Frontend SSR"
LABEL org.opencontainers.image.version=${VERSION:-"0.0.0"}
LABEL org.opencontainers.image.created=${BUILD_DATE}
LABEL org.opencontainers.image.revision=${VCS_REF}
LABEL org.opencontainers.image.authors="tangi@le-cadre.net"
LABEL org.opencontainers.image.vendor="VocTech"

# Démarrage du serveur SSR
CMD ["npm", "run", "serve:ssr:VocTech"]
