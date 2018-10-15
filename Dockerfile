FROM openjdk:10-jdk-slim as builder
COPY . /tmp/pantheon
WORKDIR /tmp/pantheon
RUN ./gradlew installDist

FROM openjdk:10-jre-slim

# copy application from builder image
COPY --from=builder /tmp/pantheon/build/install/pantheon /opt/pantheon/

# List Exposed Ports
EXPOSE 8546 8545 30303

# specify default command
ENTRYPOINT ["/opt/pantheon/bin/pantheon"]

# How to use this image:

# first build it:
# docker build -t pantheon:latest .

# then run pantheon:
# as a simple node with sync on mainnet with a volume for data (keeps the database between runs) and that's all:
# docker run -d --mount source=pantheon_database,target=/opt/pantheon/database --name pantheon pantheon:latest
# or
# as a simple node with sync on mainnet with a volume for data (keeps the database between runs) and  HTTP/WS RPC access :
#docker run -d --mount source=pantheon_database,target=/opt/pantheon/database \
#--name pantheon -p 8545:8545 -p 8546:8546 pantheon:latest \
#--rpc-enabled --rpc-listen=0.0.0.0:8545 --rpc-cors-origins=* \
#--ws-enabled -ws-listen=0.0.0.0:8546

#then ispect the logs to see what happens on this node:
#docker logs -f pantheon