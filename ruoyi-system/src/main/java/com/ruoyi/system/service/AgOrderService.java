package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AgOrder;

/**
 * AgOrder Service Interface
 * 
 * @author ruoyi
 */
public interface AgOrderService {
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
     * Create Order
     * 
     * @param agOrder AgOrder
     * @return Result
     */
    public int createOrder(AgOrder agOrder);

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
     * Batch Delete AgOrder
     * 
     * @param orderIds AgOrder IDs
     * @return Result
     */
    public int deleteAgOrderByIds(Long[] orderIds);

    /**
     * Delete AgOrder
     * 
     * @param orderId AgOrder ID
     * @return Result
     */
    public int deleteAgOrderById(Long orderId);
}
