package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Shopping Cart Object ag_cart
 * 
 * @author ruoyi
 */
public class AgCart extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Cart ID */
    private Long cartId;

    /** User ID (Hidden unique identifier) */
    private Long userId;

    /** Product ID */
    private Long productId;

    /** Quantity */
    private Integer quantity;

    /** 收货人姓名 */
    @Xss(message = "收货人姓名不能包含脚本字符")
    private String receiverName;

    /** 联系电话 */
    @Xss(message = "联系电话不能包含脚本字符")
    private String receiverPhone;

    /** 收货地址 */
    @Xss(message = "收货地址不能包含脚本字符")
    private String receiverAddress;

    /** Product Info (Joined) */
    private String productName;
    private String productImg;
    private String productPrice;

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("cartId", getCartId())
                .append("userId", getUserId())
                .append("productId", getProductId())
                .append("quantity", getQuantity())
                .append("receiverName", getReceiverName())
                .append("receiverPhone", getReceiverPhone())
                .append("receiverAddress", getReceiverAddress())
                .append("createTime", getCreateTime())
                .toString();
    }
}
