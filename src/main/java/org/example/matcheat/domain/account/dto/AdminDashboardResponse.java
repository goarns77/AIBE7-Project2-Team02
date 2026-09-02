package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.service.AdminAccountService;

public record AdminDashboardResponse(long totalUsers, long pendingSellerApplications) {
    public static AdminDashboardResponse from(AdminAccountService.DashboardResult result) {
        return new AdminDashboardResponse(result.totalUsers(), result.pendingSellerApplications());
    }
}
