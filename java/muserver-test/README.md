# muserver-test

This spins up a [MuServer](https://muserver.io) - which runs netty. 

## To build

```sh
#Package

./mvnw package

#Docker image - spits out a docker image - muserver-test
./mvnw compile jib:dockerBuild
```

## To run

```sh
# -XX:MaxRAMPercentage=80 <- Tells the jvm to use 80% of available memory
docker run -it -d -p 8181:8080 --name java --rm --cpuset-cpus 0-1 --memory 4gb -e JDK_JAVA_OPTIONS='-XX:MaxRAMPercentage=80' -e TRINO_URL="jdbc:trino://remoteserver:8080/tpch/sf1?SSL=false&admin=foo" muserver-test
```