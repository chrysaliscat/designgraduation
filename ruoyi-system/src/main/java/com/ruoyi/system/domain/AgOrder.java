package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;

/**
 * Agricultural Order Object ag_order
 * 
 * @author ruoyi
 */
public class AgOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Order ID */
    private Long orderId;

    /** Order Number */
    @Excel(name = "Order Number")
    private String orderNo;

    /** User ID */
    @Excel(name = "User ID")
    private Long userId;

    /** Total Amount */
    @Excel(name = "Total Amount")
    private BigDecimal totalAmount;

    /** Status (0=Pending, 1=Paid, 2=Shipped, 3=Completed, 4=Cancelled) */
    @Excel(name = "Status", readConverterExp = "0=Pending,1=Paid,2=Shipped,3=Completed,4=Cancelled")
    private String status;

    /** Receiver Name */
    @Excel(name = "Receiver Name")
    @Xss(message = "收货人姓名不能包含脚本字符")
    private String receiverName;

    /** Receiver Phone */
    @Excel(name = "Receiver Phone")
    @Xss(message = "收货人电话不能包含脚本字符")
    private String receiverPhone;

    /** Receiver Address */
    @Excel(name = "Receiver Address")
    @Xss(message = "收货地址不能包含脚本字符")
    private String receiverAddress;

    /** Buyer Name Snapshot */
    @Excel(name = "Buyer Name")
    private String buyerName;

    /** Payment Time */
    @Excel(name = "Payment Time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** Order Item Info */
    private List<AgOrderItem> orderItemList;

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Date getPayTime() {
        return payTime;
    }

    public List<AgOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<AgOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("orderNo", getOrderNo())
                .append("userId", getUserId())
                .append("totalAmount", getTotalAmount())
                .append("status", getStatus())
                .append("receiverName", getReceiverName())
                .append("receiverPhone", getReceiverPhone())
                .append("receiverAddress", getReceiverAddress())
                .append("buyerName", getBuyerName())
                .append("createTime", getCreateTime())
                .append("payTime", getPayTime())
                .append("orderItemList", getOrderItemList())
                .toString();
    }
}
