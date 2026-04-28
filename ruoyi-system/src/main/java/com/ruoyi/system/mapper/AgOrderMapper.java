package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AgOrder;

/**
 * AgOrder Mapper Interface
 * 
 * @author ruoyi
 */
public interface AgOrderMapper {
    /**
     * Bulk Update Order Status by User ID and Current Status
     */
    public int updateOrderStatusByUserId(@org.apache.ibatis.annotations.Param("userId") Long userId,
            @org.apache.ibatis.annotations.Param("oldStatus") String oldStatus,
            @org.apache.ibatis.annotations.Param("newStatus") String newStatus);

    /**
     * Update order status by ID when it is still in the expected status.
     */
    public int updateOrderStatusById(@org.apache.ibatis.annotations.Param("orderId") Long orderId,
            @org.apache.ibatis.annotations.Param("oldStatus") String oldStatus,
            @org.apache.ibatis.annotations.Param("newStatus") String newStatus);

    /**
     * Query pending orders older than the configured timeout.
     */
    public List<AgOrder> selectExpiredPendingOrders(@org.apache.ibatis.annotations.Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * Query AgOrder
     * 
     * @param orderId AgOrder ID
     * @return AgOrder
     */
    public AgOrder selectAgOrderById(Long orderId);

    /**
     * Query AgOrder List
     * 
     * @param agOrder AgOrder
     * @return AgOrder Collection
     */
    public List<AgOrder> selectAgOrderList(AgOrder agOrder);

    /**
     * Insert AgOrder
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    public int insertAgOrder(AgOrder agOrder);

    /**
     * Update AgOrder
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    public int updateAgOrder(AgOrder agOrder);

    /**
     * Delete AgOrder
     * 
     * @param orderId AgOrder ID
     * @return Result
     */
    public int deleteAgOrderById(Long orderId);

    /**
     * Batch Delete AgOrder
     * 
     * @param orderIds AgOrder IDs
     * @return Result
     */
    public int deleteAgOrderByIds(Long[] orderIds);

    /**
     * Get Order Statistics (Count, Total Sales)
     * 
     * @return Map
     */
    public java.util.Map<String, Object> selectOrderStats(AgOrder agOrder);

    public java.util.List<java.util.Map<String, Object>> selectSalesTrend(AgOrder agOrder);

    public java.util.List<java.util.Map<String, Object>> selectTopProducts(AgOrder agOrder);

    /**
     * Count orders that belong to the current seller
     * 
     * @param orderId Order ID
     * @param sellerId Seller username
     * @return count
     */
    public int countSellerOwnedOrder(@org.apache.ibatis.annotations.Param("orderId") Long orderId,
            @org.apache.ibatis.annotations.Param("sellerId") String sellerId);

}
