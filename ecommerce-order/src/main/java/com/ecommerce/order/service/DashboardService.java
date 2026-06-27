package com.ecommerce.order.service;

import com.ecommerce.dto.response.DashboardVO;

public interface DashboardService {

    DashboardVO getDashboardData();

    byte[] exportDashboardExcel();
}