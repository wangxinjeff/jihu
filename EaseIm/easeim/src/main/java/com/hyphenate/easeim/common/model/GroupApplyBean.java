package com.hyphenate.easeim.common.model;

public class GroupApplyBean {

    private String customerName;
    private String groupName;
    private String inviterName;
    private long timeStamp;
    private boolean isOperated;
    private String operatedResult;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isOperated() {
        return isOperated;
    }

    public void setOperated(boolean operated) {
        isOperated = operated;
    }

    public String getOperatedResult() {
        return operatedResult;
    }

    public void setOperatedResult(String operatedResult) {
        this.operatedResult = operatedResult;
    }
}
