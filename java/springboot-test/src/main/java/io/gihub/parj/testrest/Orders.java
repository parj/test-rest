package io.gihub.parj.testrest;

import jakarta.persistence.*;

import java.io.Serializable;
@Entity(name = "orders")
@Table(name = "orders")
public class Orders implements Serializable {
    @Id
    @Column(name = "orderkey")
    private int orderkey;

    @Column(name = "custkey")
    private int custkey;

    @Column(name = "orderstatus")
    private String orderstatus;

    @Column(name = "totalprice")
    private double totalprice;

    @Column(name = "orderdate")
    private String orderdate;

    @Column(name = "orderpriority")
    private String orderpriority;

    @Column(name = "clerk")
    private String clerk;

    @Column(name = "shippriority")
    private int shippriority;

    @Column(name = "comment")
    private String comment;

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
}