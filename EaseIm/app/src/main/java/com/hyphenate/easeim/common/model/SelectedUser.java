package com.hyphenate.easeim.common.model;

public class SelectedUser {
    private String name;
    private boolean isCustomer = false;

    public SelectedUser(String name) {
        this.name = name;
    }

    public SelectedUser(String name, boolean isCustomer) {
        this.name = name;
        this.isCustomer = isCustomer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustomer() {
        return isCustomer;
    }

    public void setCustomer(boolean customer) {
        isCustomer = customer;
    }
}
