package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Order Item Object ag_order_item
 * 
 * @author ruoyi
 */
public class AgOrderItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long orderItemId;

    /** Order ID */
    @Excel(name = "Order ID")
    private Long orderId;

    /** Product ID */
    @Excel(name = "Product ID")
    private Long productId;

    /** Product Name Snapshot */
    @Excel(name = "Product Name")
    private String productName;

    /** Product Price Snapshot */
    @Excel(name = "Product Price")
    private BigDecimal productPrice;

    /** Quantity */
    @Excel(name = "Quantity")
    private Integer quantity;

    /** Subtotal */
    @Excel(name = "Subtotal")
    private BigDecimal subtotal;

    /** Seller Name (from ag_product -> sys_user) */
    @Excel(name = "Seller Name")
    private String sellerName;

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderItemId", getOrderItemId())
                .append("orderId", getOrderId())
                .append("productId", getProductId())
                .append("productName", getProductName())
                .append("productPrice", getProductPrice())
                .append("quantity", getQuantity())
                .append("subtotal", getSubtotal())
                .toString();
    }
}
