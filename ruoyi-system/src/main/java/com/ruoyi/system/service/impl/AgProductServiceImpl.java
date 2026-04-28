package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.AgProductMapper;
import com.ruoyi.system.domain.AgProduct;
import com.ruoyi.system.service.AgProductService;

/**
 * Agricultural Product Service Business Layer Processing
 * 
 * @author ruoyi
 */
@Service("agProductService")
public class AgProductServiceImpl implements AgProductService {
    @Autowired
    private AgProductMapper agProductMapper;

    /**
     * Query Agricultural Product
     * 
     * @param productId Product ID
     * @return Agricultural Product
     */
    @Override
    public AgProduct selectAgProductById(Long productId) {
        return agProductMapper.selectAgProductById(productId);
    }

    /**
     * Query Agricultural Product List
     * 
     * @param agProduct Agricultural Product
     * @return Agricultural Product Collection
     */
    @Override
    public List<AgProduct> selectAgProductList(AgProduct agProduct) {
        return agProductMapper.selectAgProductList(agProduct);
    }

    /**
     * Add Agricultural Product
     * 
     * @param agProduct Agricultural Product
     * @return Result
     */
    @Override
    public int insertAgProduct(AgProduct agProduct) {
        validateProduct(agProduct);
        agProduct.setCreateTime(DateUtils.getNowDate());
        agProduct.setCreateBy(SecurityUtils.getUsername());
        return agProductMapper.insertAgProduct(agProduct);
    }

    /**
     * Update Agricultural Product
     * 
     * @param agProduct Agricultural Product
     * @return Result
     */
    @Override
    public int updateAgProduct(AgProduct agProduct) {
        validateProduct(agProduct);
        AgProduct dbProduct = agProductMapper.selectAgProductById(agProduct.getProductId());
        validateProductOwnership(dbProduct);
        agProduct.setUpdateTime(DateUtils.getNowDate());
        agProduct.setUpdateBy(SecurityUtils.getUsername());
        agProduct.setCreateBy(dbProduct.getCreateBy());
        return agProductMapper.updateAgProduct(agProduct);
    }

    /**
     * Batch Delete Agricultural Product
     * 
     * @param productIds Product IDs to delete
     * @return Result
     */
    @Override
    public int deleteAgProductByIds(Long[] productIds) {
        for (Long productId : productIds) {
            validateProductOwnership(agProductMapper.selectAgProductById(productId));
        }
        return agProductMapper.deleteAgProductByIds(productIds);
    }

    /**
     * Delete Agricultural Product Information
     * 
     * @param productId Product ID
     * @return Result
     */
    @Override
    public int deleteAgProductById(Long productId) {
        validateProductOwnership(agProductMapper.selectAgProductById(productId));
        return agProductMapper.deleteAgProductById(productId);
    }

    private void validateProduct(AgProduct agProduct) {
        if (agProduct == null) {
            throw new ServiceException("商品信息不能为空");
        }
        if (StringUtils.isBlank(agProduct.getProductName())) {
            throw new ServiceException("商品名称不能为空");
        }
        if (agProduct.getPrice() == null || agProduct.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("商品价格必须大于 0");
        }
        if (agProduct.getStock() == null || agProduct.getStock() < 0) {
            throw new ServiceException("商品库存不能小于 0");
        }
    }

    private void validateProductOwnership(AgProduct agProduct) {
        if (agProduct == null) {
            throw new ServiceException("商品不存在或已删除");
        }
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getUsername().equals(agProduct.getCreateBy())) {
            throw new ServiceException("无权操作其他卖家的商品");
        }
    }
}
