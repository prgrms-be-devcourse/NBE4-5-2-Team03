FROM node:lts-alpine

WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
COPY tsconfig.json ./
COPY components.json ./
COPY next.config.ts ./
COPY postcss.config.mjs ./

RUN npm install --legacy-peer-deps

COPY src/ ./src
COPY public/ ./public

RUN npm run build

EXPOSE 3000

ENV NODE_ENV=production

CMD ["npm", "run", "start"]

