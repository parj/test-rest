package io.gihub.parj.testrest;

import io.github.parj.testrest.grpc.Orders;
import io.github.parj.testrest.grpc.OrdersServiceImplGrpc;

import io.github.parj.testrest.grpc.StreamOrdersRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@GrpcService
@Component
public class OrdersGrpcService extends OrdersServiceImplGrpc.OrdersServiceImplImplBase {
    @Value("${trino.url}")
    private String trinoUrl;

    @Override
    public void streamOrders(StreamOrdersRequest request, StreamObserver<Orders> responseObserver) {
        try (
             Connection connection = DriverManager.getConnection(trinoUrl);
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
