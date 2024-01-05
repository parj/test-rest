# springboot-test

This spins up a Springboot server that servers four things.
* End point that servers up results (JSON format) as is via JPA - `/orders`
* Streams the results (JSON format) using JPA - `/stream/orders`
* Streams the results (JSON format) without JPA - `/stream/v2/orders`
* GRPC end point for streaming results. 
  - The grpc server listens to 9090 and supports plain text. 
  - This can be queried using [grpcurl](https://github.com/fullstorydev/grpcurl). 
  - To then test - `grpcurl --plaintext localhost:9191 io.github.parj.testrest.OrdersServiceImpl.StreamOrders > /tmp/out.json`

GZIP is also supported, if the results need to be zipped, a header needs to be set, ex. `curl -H "Accept-Encoding: gzip" http://localhost:8181/stream/v2/orders -o orders.json.gz`

## To build

```sh
#Package

./mvnw package

#Docker image - spits out a docker image - springboot-test
./mvnw compile jib:dockerBuild
```

## To run

```sh
# -XX:MaxRAMPercentage=80 <- Tells the jvm to use 80% of available memory
docker run -it -d -p 8181:8080 -p 9191:9090 --name java --rm --cpuset-cpus 0-1 --memory 4gb -e JDK_JAVA_OPTIONS='-XX:MaxRAMPercentage=80' -e TRINO_URL="jdbc:trino://remoteserver:8080/tpch/sf1?SSL=false&admin=foo" springboot-test
```
