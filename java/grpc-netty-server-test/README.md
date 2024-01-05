# grpc-server

This spins up a netty server with grpc server. The grpc server listens to 9090 and supports plain text.

## To build

```sh
#Package

./mvnw package

#Docker image - spits out a docker image - grpc-server
./mvnw compile jib:dockerBuild
```

## To run

```sh
# -XX:MaxRAMPercentage=80 <- Tells the jvm to use 80% of available memory
docker run -it -d -p 8181:8080 -p 9191:9090 --name java --rm --cpuset-cpus 0-1 --memory 4gb -e JDK_JAVA_OPTIONS='-XX:MaxRAMPercentage=80' -e TRINO_URL="jdbc:trino://remoteserver:8080/tpch/sf1?SSL=false&admin=foo" io.github.parj.testrest.grpc.netty-grpc-server
```

If you want to interact via a curl like program - install [grpcurl](https://github.com/fullstorydev/grpcurl)

To then test -

```sh
grpcurl --plaintext localhost:9191 io.github.parj.testrest.OrdersServiceImpl.StreamOrders > /tmp/out.json`
```
