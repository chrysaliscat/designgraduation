package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.AgCartMapper;
import com.ruoyi.system.mapper.AgProductMapper;
import com.ruoyi.system.domain.AgCart;
import com.ruoyi.system.domain.AgProduct;
import com.ruoyi.system.service.IAgCartService;

/**
 * Shopping Cart Service Implementation
 * 
 * @author ruoyi
 */
@Service
public class AgCartServiceImpl implements IAgCartService {
    @Autowired
    private AgCartMapper agCartMapper;

    @Autowired
    private AgProductMapper agProductMapper;

    /**
     * Query Cart List
     * 
     * @param agCart Query Params
     * @return Cart List
     */
    @Override
    public List<AgCart> selectAgCartList(AgCart agCart) {
        return agCartMapper.selectAgCartList(agCart);
    }

    @Override
    public AgCart selectAgCartById(Long cartId) {
        return agCartMapper.selectAgCartById(cartId);
    }

    /**
     * Add to Cart
     * 
     * @param agCart Cart
     * @return Result
     */
    @Override
    public int insertAgCart(AgCart agCart) {
        validateCartQuantity(agCart.getQuantity());
        AgProduct product = validateCartProduct(agCart);

        // 1. Check if product already exists in cart for this user
        AgCart existingCart = agCartMapper.selectAgCartByProductId(agCart);
        if (existingCart != null) {
            // 2. If exists, update quantity and receiver info
            int newQuantity = existingCart.getQuantity() + agCart.getQuantity();
            validateProductStock(product, newQuantity);
            existingCart.setQuantity(newQuantity);
            existingCart.setReceiverName(agCart.getReceiverName());
            existingCart.setReceiverPhone(agCart.getReceiverPhone());
            existingCart.setReceiverAddress(agCart.getReceiverAddress());
            return agCartMapper.updateAgCart(existingCart);
        }
        // 3. If new, insert
        agCart.setCreateTime(DateUtils.getNowDate());
        return agCartMapper.insertAgCart(agCart);
    }

    /**
     * Update Cart
     * 
     * @param agCart Cart
     * @return Result
     */
    @Override
    public int updateAgCart(AgCart agCart) {
        if (agCart.getCartId() == null) {
            throw new ServiceException("购物车记录不存在");
        }
        validateCartQuantity(agCart.getQuantity());

        AgCart existingCart = agCartMapper.selectAgCartById(agCart.getCartId());
        validateCartOwnership(existingCart);
        return agCartMapper.updateAgCart(agCart);
    }

    /**
     * Delete Cart Item
     * 
     * @param cartId Cart ID
     * @return Result
     */
    @Override
    public int deleteAgCartById(Long cartId) {
        AgCart existingCart = agCartMapper.selectAgCartById(cartId);
        validateCartOwnership(existingCart);
        return agCartMapper.deleteAgCartById(cartId);
    }

    /**
     * Clear Cart for User
     * 
     * @param userId User ID
     * @return Result
     */
    @Override
    public int deleteAgCartByUserId(Long userId) {
        return agCartMapper.deleteAgCartByUserId(userId);
    }

    private void validateCartQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ServiceException("购物车商品数量必须大于 0");
        }
    }

    private AgProduct validateCartProduct(AgCart agCart) {
        if (agCart == null || agCart.getProductId() == null) {
            throw new ServiceException("请选择要加入购物车的商品");
        }
        AgProduct product = agProductMapper.selectAgProductById(agCart.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在或已下架");
        }
        if (!"0".equals(product.getStatus())) {
            throw new ServiceException("商品 [" + product.getProductName() + "] 已下架，无法加入购物车");
        }
        validateProductStock(product, agCart.getQuantity());
        return product;
    }

    private void validateProductStock(AgProduct product, Integer quantity) {
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new ServiceException("商品 [" + product.getProductName() + "] 库存不足");
        }
    }

    private void validateCartOwnership(AgCart agCart) {
        if (agCart == null) {
            throw new ServiceException("购物车记录不存在");
        }
        if (!SecurityUtils.isAdmin() && !agCart.getUserId().equals(SecurityUtils.getUserId())) {
            throw new ServiceException("无权操作其他用户的购物车");
        }
    }
}
