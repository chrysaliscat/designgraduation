package com.ruoyi.web.controller.agri;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.AgProduct;

/**
 * Agricultural Data Statistics Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/data")
public class AgriDataController extends BaseController {

    @Autowired
    private com.ruoyi.system.mapper.AgOrderMapper agOrderMapper;

    @Autowired
    private com.ruoyi.system.mapper.AgProductMapper agProductMapper;

    @Autowired
    private ISysUserService userService;

    /**
     * Get Home Dashboard Data
     */
    @GetMapping("/dashboard")
    public AjaxResult getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Prepare Filter Object
        com.ruoyi.system.domain.AgOrder query = new com.ruoyi.system.domain.AgOrder();
        Long userId = SecurityUtils.getUserId();

        if (SecurityUtils.isAdmin(userId)) {
            // Admin: No filter
        } else {
            // Determine if Seller (Assume strict RBAC, non-admins are sellers/buyers)
            // Check roles to be precise if needed, but for now:
            // If not admin, we filter by createBy for Products/Sales

            // For Order Stats/Trend, we need to filter by SellerID (username)
            Map<String, Object> params = new HashMap<>();
            params.put("sellerId", SecurityUtils.getUsername());
            query.setParams(params);
        }

        // 1. Core Metrics (Real Data from DB)
        // Returns {orderCount=..., totalSales=...}
        Map<String, Object> stats = agOrderMapper.selectOrderStats(query);

        BigDecimal totalSales = BigDecimal.ZERO;
        Long orderCount = 0L;

        if (stats != null) {
            if (stats.get("totalSales") != null) {
                totalSales = new BigDecimal(stats.get("totalSales").toString());
            }
            if (stats.get("orderCount") != null) {
                orderCount = Long.valueOf(stats.get("orderCount").toString());
            }
        }

        // Logic for "Third Card": Admin sees User Count, Seller/Buyer sees Product
        // Count
        String userLabel = "用户总数";
        long userCount = 0;

        if (SecurityUtils.isAdmin(userId)) {
            // Admin: Show Total System Users
            userCount = userService.selectUserList(new SysUser()).size();
        } else {
            // Seller/Buyer: Show My Products Count
            userLabel = "我的商品";
            AgProduct p = new AgProduct();
            p.setCreateBy(SecurityUtils.getUsername());
            userCount = agProductMapper.selectAgProductList(p).size();
        }

        data.put("totalSales", totalSales);
        data.put("orderCount", orderCount);
        data.put("userCount", userCount);
        data.put("userLabel", userLabel);

        // 2. Sales Trend (Real Data)
        List<String> dates = new ArrayList<>();
        List<BigDecimal> salesTrend = new ArrayList<>();

        // Fetch from DB
        List<Map<String, Object>> trendData = agOrderMapper.selectSalesTrend(query);
        Map<String, BigDecimal> trendMap = new HashMap<>();
        if (trendData != null) {
            for (Map<String, Object> m : trendData) {
                if (m.get("date") != null && m.get("value") != null) {
                    trendMap.put(m.get("date").toString(), new BigDecimal(m.get("value").toString()));
                }
            }
        }

        // Fill last 7 days (including today)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String d = sdf.format(cal.getTime());
            dates.add(d);
            salesTrend.add(trendMap.getOrDefault(d, BigDecimal.ZERO));
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        data.put("trendDates", dates);
        data.put("trendSales", salesTrend);

        // 3. Category Ratio - (Pending Real Implementation, sending empty for now)
        data.put("categoryRatio", new ArrayList<>());

        // 4. Top Products - Real Data
        List<Map<String, Object>> topProductsRaw = agOrderMapper.selectTopProducts(query);
        List<Map<String, Object>> topProducts = new ArrayList<>();
        if (topProductsRaw != null) {
            for (Map<String, Object> m : topProductsRaw) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", m.get("name"));
                item.put("count", m.get("count")); // ensure key matches frontend
                topProducts.add(item);
            }
        }
        data.put("topProducts", topProducts);

        return AjaxResult.success(data);
    }
}
