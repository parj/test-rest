package io.github.parj.testrest.grpc.netty;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;


import io.github.parj.testrest.grpc.Orders;
import io.github.parj.testrest.grpc.OrdersServiceImplGrpc;
import io.github.parj.testrest.grpc.StreamOrdersRequest;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;


public class App {
    static Logger logger = Logger.getLogger(App.class.getName());
    private static String TRINO_URL;

    public static void main( String[] args ) throws IOException, InterruptedException {
        if (!System.getenv().containsKey("TRINO_URL"))
            throw new RuntimeException("TRINO_URL environment variable not set");

        TRINO_URL = System.getenv("TRINO_URL");

        int port = 8080;

        Server server = ServerBuilder.forPort(port)
                .addService(new OrdersGrpcService())
                .addService(ProtoReflectionService.newInstance())
                .build();

        server.start();
        System.out.println("Server started on port " + port);
        logger.info("Server started on port " + port);

        server.awaitTermination();
    }

    static class OrdersGrpcService extends OrdersServiceImplGrpc.OrdersServiceImplImplBase {
        @Override
        public void streamOrders(StreamOrdersRequest request, StreamObserver<Orders> responseObserver) {
            try (
                    Connection connection = DriverManager.getConnection(TRINO_URL);
                    Statement statement = connection.createStatement();
                    ResultSet rs = statement.executeQuery("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders");
            ) {

                //TODO: Fix this to output JSON Array
                while (rs.next()) {
                    Orders order = Orders.newBuilder()
                            .setOrderkey(rs.getInt("orderkey"))
                            .setCustkey(rs.getInt("custkey"))
                            .setOrderstatus(rs.getString("orderstatus"))
                            .setTotalprice(rs.getDouble("totalprice"))
                            .setOrderdate(rs.getString("orderdate"))
                            .setOrderpriority(rs.getString("orderpriority"))
                            .setClerk(rs.getString("clerk"))
                            .setShippriority(rs.getInt("shippriority"))
                            .setComment(rs.getString("comment"))
                            .build();

                    responseObserver.onNext(order);
                }
                responseObserver.onCompleted();
            } catch (SQLException e) {
                throw new RuntimeException("Error streaming items", e);
            }
        }
    }
}
