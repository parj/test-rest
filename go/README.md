# Intro 

Golang program to benchmark how long it would take to stream a response back.

## Build

Build the docker image

```sh
docker build -t go-test .
```

## Run

```sh
#Specifically setting 2 CPU cores and 4GB of RAM
docker run -it -d -p 8181:8080 --name test-go --rm --cpuset-cpus 0-1 --memory 4gb -e TRINO_URL='"http://user@remoteserver:8080?catalog=tpch&schema=sf1' test-go
time curl http://localhost:8181/items -o /tmp/orders.json
```

## TODO

- [ ] Mop up go lang code
- [ ] Support for dynamic queries
- [ ] Add some tests 😑