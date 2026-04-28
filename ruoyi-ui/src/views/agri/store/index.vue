<template>
  <div class="app-container">
    <el-alert
      title="🚀 助农脱贫，源头直采！为您提供最新鲜的绿色农产品。"
      type="success"
      class="mb8"
      show-icon>
    </el-alert>

    <!-- Search Form -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px" class="search-form">
      <el-form-item label="农产品" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入农产品名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="productList.length === 0" description="暂无农产品上架，敬请期待..."></el-empty>

    <el-row :gutter="20" type="flex" style="flex-wrap: wrap;">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in productList" :key="product.productId" style="margin-bottom: 20px; display: flex;">
        <el-card class="product-card" :body-style="{ padding: '0px', height: '100%', display: 'flex', flexDirection: 'column' }" shadow="hover" style="width: 100%;">
          <div style="height: 220px; overflow: hidden; position: relative;">
             <img :src="getImageUrl(product.productImage) || 'https://via.placeholder.com/300x200?text=No+Image'" class="image" @click="goDetail(product)" style="cursor: pointer;">
             <div v-if="product.stock <= 0" class="sold-out-overlay">
                 <span>已售罄</span>
             </div>
          </div>
          <div style="padding: 14px; flex: 1; display: flex; flexDirection: column; justify-content: space-between;">
            <div class="bottom clearfix">
               <span class="product-name" @click="goDetail(product)" style="cursor: pointer;">{{ product.productName }}</span>
               <div class="product-info">
                   <span class="price">¥{{ product.price }}</span>
                   <span class="stock" v-if="product.stock > 0">库存: {{ product.stock }}</span>
                   <span class="stock" v-else style="color: red; font-weight: bold;">缺货</span>
               </div>
               <div class="trace-info">
                   <div style="margin-bottom: 10px;">
                       <el-tag size="mini" type="success" effect="light"><i class="el-icon-location-outline"></i> {{ product.origin || '未知产地' }}</el-tag>
                       <el-tag size="mini" type="warning" effect="light" style="margin-left: 5px;"><i class="el-icon-s-shop"></i> {{ product.farmer }}</el-tag>
                   </div>
                   <div class="date-info">
                       <i class="el-icon-date"></i> 采摘: {{ product.productionDate || '新鲜直达' }}
                   </div>
               </div>
               <div class="action-btn" style="display: flex; justify-content: space-between; margin-top: auto;">
                  <el-button v-if="isBuyer" type="primary" size="small" icon="el-icon-present" @click="handleBuy(product)" :disabled="product.stock <= 0" style="flex: 1; border-radius: 20px 0 0 20px;">
                      {{ product.stock > 0 ? '立即购买' : '已售罄' }}
                  </el-button>
                  <el-button v-if="isBuyer" type="warning" size="small" icon="el-icon-shopping-cart-2" @click="handleAddToCart(product)" :disabled="product.stock <= 0" style="flex: 1; border-radius: 0 20px 20px 0;">
                      加入购物车
                  </el-button>
               </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Buy/Cart Dialog -->
    <el-dialog :title="form.isCart ? '加入购物车' : '确认购买'" :visible.sync="open" width="500px" append-to-body>
        <el-form ref="form" :model="form" label-width="100px">
           <el-form-item label="商品名称">
               <el-input v-model="form.productName" disabled />
           </el-form-item>
           <el-form-item label="单价">
               <el-input v-model="form.price" disabled />
           </el-form-item>
           <el-form-item label="购买数量">
               <el-input-number v-model="form.quantity" :min="1" :max="form.maxStock" label="Quantity"></el-input-number>
           </el-form-item>
           <el-form-item label="总金额">
               <span style="color: red; font-size: 18px; font-weight: bold;">¥{{ (form.price * form.quantity).toFixed(2) }}</span>
           </el-form-item>
           <el-divider></el-divider>
           <el-form-item label="收货人姓名">
               <el-input v-model="form.receiverName" placeholder="请输入您的姓名" />
           </el-form-item>
           <el-form-item label="联系电话">
               <el-input v-model="form.receiverPhone" placeholder="请输入您的电话" />
           </el-form-item>
           <el-form-item label="收货地址">
               <el-input v-model="form.receiverAddress" placeholder="请输入详细地址" />
           </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="submitOrder" :loading="submitLoading">{{ form.isCart ? '确认加入购物车' : '提交订单' }}</el-button>
            <el-button @click="open = false">取 消</el-button>
        </div>
    </el-dialog>

    <!-- Payment QR Code Dialog -->
    <el-dialog title="扫码支付" :visible.sync="openPayment" width="400px" append-to-body center :close-on-click-modal="false" :show-close="false">
        <div style="text-align: center;">
            <p style="font-size: 16px; margin-bottom: 20px;">订单已生成! 订单号: <span style="color: #409EFF; font-weight: bold;">{{ currentOrderNo }}</span></p>
            <p>应付金额: <span style="color: #F56C6C; font-size: 20px; font-weight: bold;">¥{{ currentTotal }}</span></p>
            <!-- Placeholder QR Code -->
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MockPayment" style="margin: 20px 0; border: 1px solid #ddd; padding: 10px; border-radius: 4px;">
            <p style="color: #909399; font-size: 13px;">请使用微信或支付宝扫码 (模拟)</p>
        </div>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="paymentLoading" @click="handlePayCancel">取消支付</el-button>
            <el-button type="success" :loading="paymentLoading" @click="handlePaySuccess">我已完成支付</el-button>
        </div>
    </el-dialog>
  </div>
</template>

<script>
import { listProduct } from "@/api/agri/product";

import { addOrder, updateOrder } from "@/api/agri/order";
import { addCart } from "@/api/agri/cart";

export default {
  name: "Store",
  data() {
    return {
      // Search parameters
      showSearch: true,
      queryParams: {
        pageNum: 1,
        pageSize: 50,
        productName: undefined,
        status: '0'
      },
      // Product list
      productList: [],
      // Dialog
      open: false,
      submitLoading: false,
      // Payment Dialog
      openPayment: false,
      paymentLoading: false,
      currentOrderId: null,
      currentOrderNo: '',
      currentTotal: 0,
      form: {
          productId: null,
          productName: null,
          price: 0,
          quantity: 1,
          maxStock: 0,
          receiverName: '',
          receiverPhone: '',
          receiverAddress: ''
      }
    };
  },
  computed: {
    isBuyer() {
      return this.$store.getters.roles.includes("agri_buyer");
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** Query Product List */
    getList() {
      listProduct(this.queryParams).then(response => {
        this.productList = response.rows;
      });
    },
    /** Search button operation */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button operation */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.indexOf("http") === 0) return url;
      return process.env.VUE_APP_BASE_API + url;
    },
    handleBuy(product) {
        this.openDialog(product, false);
    },
    handleAddToCart(product) {
        this.openDialog(product, true);
    },
    goDetail(product) {
        this.$router.push(`/agri/store/detail/${product.productId}`);
    },
    openDialog(product, isCart) {
        this.form = {
            productId: product.productId,
            productName: product.productName,
            price: product.price,
            quantity: 1,
            maxStock: product.stock,
            receiverName: '',
            receiverPhone: '',
            receiverAddress: '',
            isCart: isCart
        };
        this.open = true;
    },
    submitOrder() {
        if(!this.form.receiverName || !this.form.receiverPhone || !this.form.receiverAddress) {
            this.$modal.msgError("请填写完整收货信息(姓名、电话、地址)");
            return;
        }

        if (this.form.isCart) {
            const cartData = {
                productId: this.form.productId,
                quantity: this.form.quantity,
                receiverName: this.form.receiverName,
                receiverPhone: this.form.receiverPhone,
                receiverAddress: this.form.receiverAddress
            };
            this.submitLoading = true;
            addCart(cartData).then(res => {
                this.$modal.msgSuccess("已成功加入购物车");
                this.open = false;
                this.submitLoading = false;
            }).catch(() => {
                this.submitLoading = false;
            });
            return;
        }

        if(!this.form.receiverName || !this.form.receiverPhone) {
            this.$modal.msgError("请填写姓名和电话");
            return;
        }

        this.submitLoading = true;
        
        // Construct Order Data
        const orderData = {
            receiverName: this.form.receiverName,
            receiverPhone: this.form.receiverPhone,
            receiverAddress: this.form.receiverAddress,
            totalAmount: this.form.price * this.form.quantity,
            status: '0', // Pending
            orderItemList: [
                {
                    productId: this.form.productId,
                    productName: this.form.productName,
                    productPrice: this.form.price,
                    quantity: this.form.quantity,
                    subtotal: this.form.price * this.form.quantity
                }
            ]
        };

        addOrder(orderData).then(response => {
            // this.$modal.msgSuccess("Order Placed Successfully!");
            this.open = false;
            this.submitLoading = false;
            
            // Open Payment Dialog
            this.currentOrderId = response.data; // Assuming backend returns ID
            // Ideally backend should return Order object or we query it. 
            // For MVP, simplified:
            this.currentTotal = orderData.totalAmount;
            this.currentOrderNo = response.orderNo || ("ORD" + new Date().getTime());

            this.openPayment = true;
            this.getList();
        }).catch(() => {
            this.submitLoading = false;
        });
    },
    handlePaySuccess() {
        this.paymentLoading = true;
        // Mock: Update Status to Paid (1)
        const updateData = {
            orderId: this.currentOrderId,
            status: '1' // Paid
        };
        updateOrder(updateData).then(response => {
             this.$modal.msgSuccess("支付成功！");
             this.openPayment = false;
             this.paymentLoading = false;
        }).catch(() => {
             this.paymentLoading = false;
        });
    },
    handlePayCancel() {
        if (!this.currentOrderId) {
            this.openPayment = false;
            return;
        }

        this.paymentLoading = true;
        updateOrder({
            orderId: this.currentOrderId,
            status: '4'
        }).then(() => {
            this.$modal.msgSuccess("已取消支付");
            this.openPayment = false;
            this.getList();
        }).catch(() => {
            this.$modal.msgError("取消支付失败，请到订单列表处理");
        }).finally(() => {
            this.paymentLoading = false;
        });
    }

  }
};
</script>

<style scoped lang="scss">
.app-container {
  background-color: #f5f7fb; /* Modern Light grey */
  min-height: 100vh;
  padding: 20px;
}

.search-form {
  background: #fff;
  padding: 18px 15px 0 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.sold-out-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(2px);
    display: flex;
    justify-content: center;
    align-items: center;
    pointer-events: none;
    z-index: 10;
    span {
        color: #F56C6C;
        font-size: 26px;
        font-weight: 900;
        border: 4px solid #F56C6C;
        padding: 5px 20px;
        border-radius: 8px;
        transform: rotate(-20deg);
        letter-spacing: 4px;
        box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
    }
}

.product-card {
  border: none;
  border-radius: 12px;
  background: #ffffff;
  transition: all 0.3s ease;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1) !important;
  }
}

.image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  display: block;
  transition: transform 0.5s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.bottom {
  padding: 0 5px;
}

.product-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    display: block;
    margin-bottom: 8px;
    height: 24px; 
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    
    &:hover {
      color: #409EFF;
    }
}

.product-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    background: #f8fdfa; /* Subtle green tint */
    padding: 8px;
    border-radius: 6px;
}

.price {
    color: #F56C6C;
    font-size: 20px;
    font-weight: 700;
    font-family: 'Segoe UI', sans-serif;
    
    &::before {
      content: '¥';
      font-size: 14px;
      margin-right: 2px;
    }
}

.stock {
    color: #909399;
    font-size: 12px;
    background: #fff;
    padding: 2px 6px;
    border-radius: 4px;
    border: 1px solid #ebeef5;
}

.trace-info {
    font-size: 12px;
    color: #606266;
    margin-bottom: 15px;
    line-height: 1.6;
    background: #fdfdfd;
    border: 1px dashed #e4e7ed;
    padding: 8px;
    border-radius: 8px;
    transition: all 0.3s;
    &:hover {
        border-color: #C0C4CC;
        background: #f4f4f5;
    }
    .date-info {
        color: #909399; 
        font-size: 13px;
    }
}

.action-btn {
    text-align: center;
    border-top: 1px solid #ebeef5;
    padding-top: 15px;
    display: flex;
    justify-content: space-between;
    gap: 10px;
    
    .el-button {
      flex: 1;
      border-radius: 6px;
      font-weight: 500;
    }
}
</style>
