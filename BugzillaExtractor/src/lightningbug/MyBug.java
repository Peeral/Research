/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lightningbug;

import java.sql.Timestamp;
//import java.util.Objects;

/**
 *
 * @author Administrator
 */
public class MyBug {
    private Integer id;
    private String product;
    private String component;
    private String resolution;
    private String status;
    private String summary;
    private String assignee;
    private Timestamp reported;
    private Timestamp closed;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getClosed() {
        return closed;
    }

    public void setClosed(Timestamp closed) {
        this.closed = closed;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Timestamp getReported() {
        return reported;
    }

    public void setReported(Timestamp reported) {
        this.reported = reported;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    
    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 23 * hash + this.id;
//        hash = 23 * hash + Objects.hashCode(this.status);
//        return hash;
//    }
       
    public boolean equals(Object obj) {
        if (obj instanceof MyBug ){
            MyBug myBug = (MyBug)obj;
            return (myBug.getId()== this.id);
        }
        return false;        
    }
}
