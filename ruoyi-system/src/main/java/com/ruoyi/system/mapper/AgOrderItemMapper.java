package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AgOrderItem;

/**
 * AgOrderItem Mapper Interface
 * 
 * @author ruoyi
 */
public interface AgOrderItemMapper {
    /**
     * Query AgOrderItem
     * 
     * @param orderItemId AgOrderItem ID
     * @return AgOrderItem
     */
    public AgOrderItem selectAgOrderItemById(Long orderItemId);

    /**
     * Query AgOrderItem List
     * 
     * @param agOrderItem AgOrderItem
     * @return AgOrderItem Collection
     */
    public List<AgOrderItem> selectAgOrderItemList(AgOrderItem agOrderItem);

    /**
     * Insert AgOrderItem
     * 
     * @param agOrderItem AgOrderItem
     * @return Result
     */
    public int insertAgOrderItem(AgOrderItem agOrderItem);

    /**
     * Update AgOrderItem
     * 
     * @param agOrderItem AgOrderItem
     * @return Result
     */
    public int updateAgOrderItem(AgOrderItem agOrderItem);

    /**
     * Delete AgOrderItem
     * 
     * @param orderItemId AgOrderItem ID
     * @return Result
     */
    public int deleteAgOrderItemById(Long orderItemId);

    /**
     * Batch Delete AgOrderItem
     * 
     * @param orderItemIds AgOrderItem IDs
     * @return Result
     */
    public int deleteAgOrderItemByIds(Long[] orderItemIds);

    /**
     * Batch Insert AgOrderItem
     * 
     * @param agOrderItemList List
     */
    public int batchAgOrderItem(List<AgOrderItem> agOrderItemList);

    /**
     * Delete by Order ID
     */
    public int deleteAgOrderItemByOrderIds(Long[] orderIds);
}
