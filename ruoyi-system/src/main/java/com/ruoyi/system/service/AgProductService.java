package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AgProduct;

/**
 * Agricultural Product Service Interface
 * 
 * @author ruoyi
 */
public interface AgProductService {
    /**
     * Query Agricultural Product
     * 
     * @param productId Product ID
     * @return Agricultural Product
     */
    public AgProduct selectAgProductById(Long productId);

    /**
     * Query Agricultural Product List
     * 
     * @param agProduct Agricultural Product
     * @return Agricultural Product Collection
     */
    public List<AgProduct> selectAgProductList(AgProduct agProduct);

    /**
     * Add Agricultural Product
     * 
     * @param agProduct Agricultural Product
     * @return Result
     */
    public int insertAgProduct(AgProduct agProduct);

    /**
     * Update Agricultural Product
     * 
     * @param agProduct Agricultural Product
     * @return Result
     */
    public int updateAgProduct(AgProduct agProduct);

    /**
     * Batch Delete Agricultural Product
     * 
     * @param productIds Product IDs to delete
     * @return Result
     */
    public int deleteAgProductByIds(Long[] productIds);

    /**
     * Delete Agricultural Product Information
     * 
     * @param productId Product ID
     * @return Result
     */
    public int deleteAgProductById(Long productId);
}
