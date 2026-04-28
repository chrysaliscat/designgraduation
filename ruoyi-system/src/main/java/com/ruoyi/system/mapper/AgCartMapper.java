package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AgCart;

/**
 * Shopping Cart Mapper
 * 
 * @author ruoyi
 */
public interface AgCartMapper {
    /**
     * Query Cart List
     * 
     * @param agCart Query Params
     * @return Cart List
     */
    public List<AgCart> selectAgCartList(AgCart agCart);

    /**
     * Query Cart Item by ID
     * 
     * @param cartId Cart ID
     * @return Cart Item
     */
    public AgCart selectAgCartById(Long cartId);

    /**
     * Add to Cart
     * 
     * @param agCart Cart
     * @return Result
     */
    public int insertAgCart(AgCart agCart);

    /**
     * Update Cart (Quantity)
     * 
     * @param agCart Cart
     * @return Result
     */
    public int updateAgCart(AgCart agCart);

    /**
     * Delete Cart Item
     * 
     * @param cartId Cart ID
     * @return Result
     */
    public int deleteAgCartById(Long cartId);

    /**
     * Clear Cart for User
     * 
     * @param userId User ID
     * @return Result
     */
    public int deleteAgCartByUserId(Long userId);

    public AgCart selectAgCartByProductId(AgCart agCart);
}
