package io.github.parj.testrest.mu;

import io.muserver.*;
import io.muserver.rest.RestHandlerBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static String TRINO_URL;

    public static void main(String[] args) {
        if (!System.getenv().containsKey("TRINO_URL"))
            throw new RuntimeException("TRINO_URL environment variable not set");

        //Example export TRINO_URL="jdbc:trino://localhost:8080/tpch/sf100000?user=foo"
        TRINO_URL = System.getenv("TRINO_URL");

        MuServer server = MuServerBuilder.httpServer()
                .addHandler(RestHandlerBuilder.restHandler(new StreamOrders()))
                .addHandler(RestHandlerBuilder.restHandler(new FindOrders()))
                .withGzipEnabled(true)
                .withHttpPort(8080)
                .start();

        System.out.println("Started server at " + server.uri());
        log.info("Started server at {}", server.uri());
    }

    @Path("/orders")
    static class FindOrders {
        @GET
        @Path("/all")
        @Produces("application/json")
        public Response getOrders() {
            System.out.println("Get all orders, without streaming");

            JSONArray result = new JSONArray();

            try (
                    Connection connection = DriverManager.getConnection(TRINO_URL);
                    Statement statement = connection.createStatement();
                    ResultSet rs = statement.executeQuery("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders");
            ) {

                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("orderkey", rs.getInt("orderkey"));
                    json.put("custkey", rs.getInt("custkey"));
                    json.put("orderstatus", rs.getString("orderstatus"));
                    json.put("totalprice", rs.getDouble("totalprice"));
                    json.put("orderdate", rs.getString("orderdate"));
                    json.put("orderpriority", rs.getString("orderpriority"));
                    json.put("clerk", rs.getString("clerk"));
                    json.put("shippriority", rs.getInt("shippriority"));
                    json.put("comment", rs.getString("comment"));
                    result.put(json);
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
            Response resp = Response.ok(result.toString()).build();
            resp.close();
            return resp;
        }
    }

    @Path("/stream/orders")
    public static class StreamOrders {
        @GET
        @Path("/all")
        @Produces("application/json")
        public Response get() {
            System.out.println("Get all orders");

            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    try (
                            Connection connection = DriverManager.getConnection(TRINO_URL);
                            Statement statement = connection.createStatement();
                            ResultSet rs = statement.executeQuery("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders");
                    ) {
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        writer.write("[\n");
                        int count = 0;
                        while (rs.next()) {
                            if (count > 0) {
                                writer.write(",\n");
                            }

                            // This is very slightly slower
//                            JSONObject json = new JSONObject();
//                            json.put("orderkey", rs.getInt("orderkey"));
//                            json.put("custkey", rs.getInt("custkey"));
//                            json.put("orderstatus", rs.getString("orderstatus"));
//                            json.put("totalprice", rs.getDouble("totalprice"));
//                            json.put("orderdate", rs.getString("orderdate"));
//                            json.put("orderpriority", rs.getString("orderpriority"));
//                            json.put("clerk", rs.getString("clerk"));
//                            json.put("shippriority", rs.getInt("shippriority"));
//                            json.put("comment", rs.getString("comment"));
//                            writer.write(json.toString());

                            String jsonR = new StringBuilder()
                                    .append("{\"orderkey\":" + rs.getInt("orderkey") + ",\"custkey\":" + rs.getInt("custkey"))
                                    .append(",\"orderstatus \":\"" + rs.getString("orderstatus") + "\",\"totalprice\":" + rs.getDouble("totalprice"))
                                    .append(",\"orderdate\":\"" + rs.getString("orderdate") + "\",\"orderpriority\":\"" + rs.getString("orderpriority"))
                                    .append("\",\"clerk\":\"" + rs.getString("clerk") + "\",\"shippriority\":" + rs.getInt("shippriority"))
                                    .append(",\"comment\":\"" + rs.getString("comment") + "\"}").toString();
                            writer.write(jsonR);
                            count++;

                        }
                        writer.write("\n]");
                        writer.flush();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            };
            return Response.ok(stream).build();
        }
    }
}
