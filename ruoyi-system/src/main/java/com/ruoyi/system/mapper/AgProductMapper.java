package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AgProduct;

/**
 * Agricultural Product Mapper Interface
 * 
 * @author ruoyi
 */
public interface AgProductMapper {
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
     * Delete Agricultural Product
     * 
     * @param productId Product ID
     * @return Result
     */
    public int deleteAgProductById(Long productId);

    /**
     * Batch Delete Agricultural Product
     * 
     * @param productIds Product IDs to delete
     * @return Result
     */
    public int deleteAgProductByIds(Long[] productIds);

    /**
     * Atomic decrease stock
     * 
     * @param productId
     * @param quantity
     * @return rows affected (0 if stock insufficient)
     */
    public int decreaseStock(@org.apache.ibatis.annotations.Param("productId") Long productId,
            @org.apache.ibatis.annotations.Param("quantity") Integer quantity);

    /**
     * Atomic increase stock
     * 
     * @param productId
     * @param quantity
     * @return rows affected
     */
    public int increaseStock(@org.apache.ibatis.annotations.Param("productId") Long productId,
            @org.apache.ibatis.annotations.Param("quantity") Integer quantity);

    /**
     * Bulk Update Status by Farmer Name
     * 
     * @param farmer Farmer Name (Username)
     * @param status New Status
     */
    public int updateProductStatusByFarmer(@org.apache.ibatis.annotations.Param("farmer") String farmer,
            @org.apache.ibatis.annotations.Param("status") String status);
}
