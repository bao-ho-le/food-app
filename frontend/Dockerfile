FROM node:20

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

ARG NEXT_PUBLIC_SPRING_URL

ENV NEXT_PUBLIC_SPRING_URL=${NEXT_PUBLIC_SPRING_URL}

RUN npm run build

EXPOSE 3000

CMD ["npm", "start"]