package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;

/**
 * Agricultural Product Object ag_product
 * 
 * @author ruoyi
 */
public class AgProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** Product ID */
    private Long productId;

    /** Category ID */
    @Excel(name = "Category ID")
    private Long categoryId;

    /** Product Name */
    @Excel(name = "Product Name")
    @Xss(message = "商品名称不能包含脚本字符")
    private String productName;

    /** Product Image */
    @Excel(name = "Product Image")
    private String productImage;

    /** Price */
    @Excel(name = "Price")
    private BigDecimal price;

    /** Stock Quantity */
    @Excel(name = "Stock Quantity")
    private Integer stock;

    /** Origin */
    @Excel(name = "Origin")
    @Xss(message = "产地不能包含脚本字符")
    private String origin;

    /** Description */
    @Excel(name = "Description")
    @Xss(message = "商品描述不能包含脚本字符")
    private String description;

    /** Farmer/Origin Name */
    @Excel(name = "Farmer")
    @Xss(message = "农户信息不能包含脚本字符")
    private String farmer;

    /** Batch Number */
    @Excel(name = "Batch No")
    @Xss(message = "批次号不能包含脚本字符")
    private String batchNo;

    /** Production Date */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "Production Date", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** Status (0=Normal, 1=Off-shelf) */
    @Excel(name = "Status")
    private String status;

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStock() {
        return stock;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setFarmer(String farmer) {
        this.farmer = farmer;
    }

    public String getFarmer() {
        return farmer;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("productId", getProductId())
                .append("categoryId", getCategoryId())
                .append("productName", getProductName())
                .append("productImage", getProductImage())
                .append("price", getPrice())
                .append("stock", getStock())
                .append("origin", getOrigin())
                .append("farmer", getFarmer())
                .append("batchNo", getBatchNo())
                .append("productionDate", getProductionDate())
                .append("description", getDescription())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
