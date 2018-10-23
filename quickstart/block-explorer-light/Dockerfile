FROM node:9-alpine

WORKDIR /build

RUN apk update && \
    apk upgrade && \
    apk add git && \
    apk add python && \
    apk add build-base
ADD package.json package-lock.json ./
RUN yarn global add serve

RUN npm install


ADD . .

RUN npm run build

RUN mv dist /app
RUN cp entrypoint.sh /app/entrypoint.sh
RUN rm -rf /build

WORKDIR /app
RUN chmod +x entrypoint.sh


EXPOSE 8080
ENTRYPOINT ["/bin/sh", "/app/entrypoint.sh"]
