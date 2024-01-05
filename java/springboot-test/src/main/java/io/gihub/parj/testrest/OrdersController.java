package io.gihub.parj.testrest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.List;

@RestController
public class OrdersController {
    private final OrdersRepository ordersRepository;

    @Value("${trino.url}")
    private String trinoUrl;

    public OrdersController(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    // Endpoint to get items in JSON format
    @GetMapping("/orders")
    public List<Orders> getItems() {
        return ordersRepository.findAll();
    }

    @GetMapping(value = "/stream/v2/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> streamV2Items() {
        StreamingResponseBody responseBody = outputStream -> {
            try (OutputStream out = outputStream;
                 Connection connection = DriverManager.getConnection(trinoUrl);
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders");
            ) {
                out.write("[\n".getBytes());

                while (rs.next()) {
                    String jsonResponse = new StringBuilder()
                            .append("{\"orderkey\":" + rs.getInt("orderkey") + ",\"custkey\":" + rs.getInt("custkey"))
                            .append(",\"orderstatus \":\"" + rs.getString("orderstatus") + "\",\"totalprice\":" + rs.getDouble("totalprice"))
                            .append(",\"orderdate\":\"" + rs.getString("orderdate") + "\",\"orderpriority\":\"" + rs.getString("orderpriority"))
                            .append("\",\"clerk\":\"" + rs.getString("clerk") + "\",\"shippriority\":" + rs.getInt("shippriority"))
                            .append(",\"comment\":\"" + rs.getString("comment") + "\"}\n").toString();
                    out.write(jsonResponse.getBytes());
                    out.flush();
                }

                out.write("]\n".getBytes());
            } catch (IOException | SQLException e) {
                throw new RuntimeException("Error streaming items", e);
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);

    }

    // Endpoint to stream items in JSON format
    @GetMapping(value = "/stream/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> streamItems() {
        List<Orders> orders = ordersRepository.findAll();

        StreamingResponseBody responseBody = outputStream -> {
            try (OutputStream out = outputStream) {
                out.write("[\n".getBytes());

                for (Orders item : orders) {
                    String jsonResponse = new StringBuilder()
                            .append("{\"orderkey\":" + item.getOrderkey() + ",\"custkey\":" + item.getCustkey())
                            .append(",\"orderstatus \":\"" + item.getOrderstatus() + "\",\"totalprice\":" + item.getTotalprice())
                            .append(",\"orderdate\":\"" + item.getOrderdate() + "\",\"orderpriority\":\"" + item.getOrderpriority())
                            .append("\",\"clerk\":\"" + item.getClerk() + "\",\"shippriority\":" + item.getShippriority())
                            .append(",\"comment\":\"" + item.getComment() + "\"}\n").toString();
                    out.write(jsonResponse.getBytes());
                    out.flush();
                }

                out.write("]\n".getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error streaming items", e);
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }
}
