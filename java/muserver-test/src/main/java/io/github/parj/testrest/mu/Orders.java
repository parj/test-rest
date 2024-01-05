package io.github.parj.testrest.mu;

import org.json.JSONObject;

public class Orders {
    private int orderkey;

    private int custkey;

    private String orderstatus;

    private double totalprice;

    private String orderdate;

    private String orderpriority;

    private String clerk;

    private int shippriority;

    private String comment;

    public Orders(int orderkey, int custkey, String orderstatus, double totalprice, String orderdate, String orderpriority, String clerk, int shippriority, String comment) {        this.orderkey =orderkey;
        this.custkey =custkey;
        this.orderstatus =orderstatus;
        this.totalprice =totalprice;
        this.orderdate =orderdate;
        this.orderpriority =orderpriority;
        this.clerk =clerk;
        this.shippriority =shippriority;
        this.comment =comment;
    }

    public int getOrderkey() {
        return orderkey;
    }

    public void setOrderkey(int orderkey) {
        this.orderkey = orderkey;
    }

    public int getCustkey() {
        return custkey;
    }

    public void setCustkey(int custkey) {
        this.custkey = custkey;
    }

    public String getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(String orderstatus) {
        this.orderstatus = orderstatus;
    }

    public double getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(double totalprice) {
        this.totalprice = totalprice;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public String getOrderpriority() {
        return orderpriority;
    }

    public void setOrderpriority(String orderpriority) {
        this.orderpriority = orderpriority;
    }

    public String getClerk() {
        return clerk;
    }

    public void setClerk(String clerk) {
        this.clerk = clerk;
    }

    public int getShippriority() {
        return shippriority;
    }

    public void setShippriority(int shippriority) {
        this.shippriority = shippriority;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("orderkey", orderkey)
                .put("custkey", custkey)
                .put("orderstatus", orderstatus)
                .put("totalprice", totalprice)
                .put("orderdate", orderdate)
                .put("orderpriority", orderpriority)
                .put("clerk", clerk)
                .put("shippriority", shippriority)
                .put("comment", comment);
    }
}