# builder temporary image with JDK
FROM openjdk:8-jdk-slim as builder
# copy all pantheon source to the image
COPY . /tmp/pantheon
WORKDIR /tmp/pantheon
# build the distribution
RUN ./gradlew installDist

# final image with only jre
FROM openjdk:8-jre-slim
# copy application from builder image
COPY --from=builder /tmp/pantheon/build/install/pantheon /opt/pantheon/
# List Exposed Ports
EXPOSE 8546 8545 30303
# specify default command
ENTRYPOINT ["/opt/pantheon/bin/pantheon"]

# How to use this image:

# first build it (use the name and tig you like) :
# docker build -t MyPantheon:myTag .

# then run pantheon:
# as a simple node with sync on mainnet with a volume for data (keeps the database between runs) and that's all:
# docker run -d --mount source=pantheon_database,target=/opt/pantheon/database --name myPantheon myPantheon:myTag
# or
# as a simple node with sync on mainnet with a volume for data (keeps the database between runs) and  HTTP/WS RPC access :
# docker run -d --mount source=pantheon_database,target=/opt/pantheon/database \
# --name myPantheon -p 8545:8545 -p 8546:8546 myPantheon:myTag \
# --rpc-enabled --rpc-listen=0.0.0.0:8545 --rpc-cors-origins=mydomain.tld \
# --ws-enabled -ws-listen=0.0.0.0:8546