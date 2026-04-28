package com.ruoyi.web.controller.agri;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.AgOrder;
import com.ruoyi.system.service.AgOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * Order Management Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/agri/order")
public class AgOrderController extends BaseController {
    @Autowired
    private AgOrderService agOrderService;

    /**
     * Query Order List
     */
    @PreAuthorize("@ss.hasPermi('agri:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgOrder agOrder) {
        applyOrderScope(agOrder);
        startPage();
        List<AgOrder> list = agOrderService.selectAgOrderList(agOrder);
        return getDataTable(list);
    }

    private void applyOrderScope(AgOrder agOrder) {
        // Data Scope Logic
        Long userId = com.ruoyi.common.utils.SecurityUtils.getUserId();
        boolean isAdmin = com.ruoyi.common.utils.SecurityUtils.isAdmin();

        // 1. Admin sees everything
        if (isAdmin) {
            // No filter
        }
        // 2. Buyers/Sellers - Filter by permissions or logic
        else {
            // Check roles
            boolean isBuyer = com.ruoyi.common.utils.SecurityUtils.getLoginUser().getUser().getRoles().stream()
                    .anyMatch(r -> "agri_buyer".equals(r.getRoleKey()));
            boolean isSeller = com.ruoyi.common.utils.SecurityUtils.getLoginUser().getUser().getRoles().stream()
                    .anyMatch(r -> "agri_seller".equals(r.getRoleKey()));

            if (isBuyer) {
                // Buyer sees their own orders
                agOrder.setUserId(userId);
            } else if (isSeller) {
                // Seller sees orders containing their products
                agOrder.getParams().put("sellerId", com.ruoyi.common.utils.SecurityUtils.getUsername());
            } else {
                // Fallback: See nothing or own
                agOrder.setUserId(userId);
            }
        }
    }

    /**
     * Export Order List
     */
    @PreAuthorize("@ss.hasPermi('agri:order:export')")
    @Log(title = "Order Management", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(AgOrder agOrder) {
        applyOrderScope(agOrder);
        List<AgOrder> list = agOrderService.selectAgOrderList(agOrder);
        ExcelUtil<AgOrder> util = new ExcelUtil<AgOrder>(AgOrder.class);
        return util.exportExcel(list, "Order Data");
    }

    /**
     * Get Detailed Order Info
     */
    @PreAuthorize("@ss.hasPermi('agri:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        return AjaxResult.success(agOrderService.selectAgOrderById(orderId));
    }

    /**
     * Add New Order
     */
    @PreAuthorize("@ss.hasPermi('agri:order:add')")
    @Log(title = "Order Management", businessType = BusinessType.INSERT)
    @RepeatSubmit(message = "系统正在处理您的订单，请勿频繁点击提交！")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgOrder agOrder) {
        // Set the Order Owner to the current user (Buyer)
        agOrder.setUserId(com.ruoyi.common.utils.SecurityUtils.getUserId());
        // Also set CreateBy for record keeping
        agOrder.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());

        int rows = agOrderService.createOrder(agOrder);
        AjaxResult ajax = toAjax(rows);
        ajax.put("data", agOrder.getOrderId());
        ajax.put("orderNo", agOrder.getOrderNo());
        ajax.put("totalAmount", agOrder.getTotalAmount());
        return ajax;
    }

    /**
     * Delete Order
     */
    @PreAuthorize("@ss.hasPermi('agri:order:edit')")
    @Log(title = "Order Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgOrder agOrder) {
        return toAjax(agOrderService.updateAgOrder(agOrder));
    }

    /**
     * Delete Order
     */
    @PreAuthorize("@ss.hasPermi('agri:order:remove')")
    @Log(title = "Order Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(agOrderService.deleteAgOrderByIds(orderIds));
    }
}
