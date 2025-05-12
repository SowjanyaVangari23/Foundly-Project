package com.foundly.app2.dto;

public class DashboardSummaryDTO {
    private long totalUsers;
    private long totalEmployees;
    private long totalItemReports;
    private long totalTransactions;

    public DashboardSummaryDTO(long totalUsers, long totalEmployees, long totalItemReports, long totalTransactions) {
        this.totalUsers = totalUsers;
        this.totalEmployees = totalEmployees;
        this.totalItemReports = totalItemReports;
        this.totalTransactions = totalTransactions;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalItemReports() {
        return totalItemReports;
    }

    public void setTotalItemReports(long totalItemReports) {
        this.totalItemReports = totalItemReports;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
