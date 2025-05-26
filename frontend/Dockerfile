# Билд стадия
FROM node:16-alpine AS build
WORKDIR /act_frontend
COPY package*.json ./
RUN NODE_ENV=development npm i
COPY . .
RUN npm run build

# Финальная стадия
FROM nginx:alpine
COPY --from=build /act_frontend/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]