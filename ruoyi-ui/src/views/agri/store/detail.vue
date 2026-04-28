<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <el-page-header @back="goBack" content="商品详情"></el-page-header>
      </div>
      <div v-loading="loading">
        <el-row :gutter="40" v-if="product" class="product-detail">
          <el-col :span="10">
            <div class="image-container">
               <img :src="getImageUrl(product.productImage) || 'https://via.placeholder.com/400x400?text=No+Image'" class="product-image">
            </div>
          </el-col>
          <el-col :span="14">
            <h1 class="product-title">{{ product.productName }}</h1>
            <div class="product-meta">
               <div class="price-tag">¥ <span class="price-val">{{ product.price }}</span></div>
               <div class="stock-tag" :class="{'no-stock': product.stock <= 0}">
                   {{ product.stock > 0 ? `库存: ${product.stock}` : '暂时缺货' }}
               </div>
            </div>
            
            <el-divider></el-divider>

            <div class="info-item">
                <span class="label">产地:</span>
                <span class="value"><i class="el-icon-location-outline"></i> {{ product.origin || '未知' }}</span>
            </div>
            <div class="info-item">
                <span class="label">农户/合作社:</span>
                <span class="value">{{ product.farmer }}</span>
            </div>
            <div class="info-item">
                <span class="label">生产日期:</span>
                <span class="value">{{ product.productionDate }}</span>
            </div>
            <div class="info-item">
                <span class="label">批次号:</span>
                <span class="value">{{ product.batchNo }}</span>
            </div>

            <el-divider></el-divider>

             <div class="description-box">
                <h3 class="desc-title">商品描述</h3>
                <p class="desc-content">{{ product.description || '暂无描述' }}</p>
            </div>

            <div class="action-area" style="margin-top: 30px;">
                <el-button type="warning" icon="el-icon-shopping-cart-2" size="medium" @click="handleAddToCart(product)" :disabled="product.stock <= 0">加入购物车</el-button>
                <el-button type="primary" size="medium" @click="handleBuy(product)" :disabled="product.stock <= 0" style="margin-left: 20px; width: 150px;">立即购买</el-button>
            </div>
          </el-col>
        </el-row>
        <div v-else class="no-data">
            <el-empty description="未找到商品信息"></el-empty>
        </div>
      </div>
    </el-card>

    <!-- Buy/Cart Dialog (Reuse Logic) -->
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
import { getProduct } from "@/api/agri/product";
import { addOrder, updateOrder } from "@/api/agri/order";
import { addCart } from "@/api/agri/cart";

export default {
  name: "ProductDetail",
  data() {
    return {
      product: null,
      loading: true,
      open: false,
      submitLoading: false,
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
  created() {
    const productId = this.$route.params.productId;
    this.getProductDetail(productId);
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.indexOf("http") !== -1) return url;
      return process.env.VUE_APP_BASE_API + url;
    },
    getProductDetail(productId) {
      this.loading = true;
      getProduct(productId).then(response => {
        this.product = response.data;
        this.loading = false;
      }).catch(err => {
         this.loading = false;
      });
    },
    handleBuy(product) {
        this.openDialog(product, false);
    },
    handleAddToCart(product) {
        this.openDialog(product, true);
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

        this.submitLoading = true;
        
        const orderData = {
            receiverName: this.form.receiverName,
            receiverPhone: this.form.receiverPhone,
            receiverAddress: this.form.receiverAddress,
            totalAmount: this.form.price * this.form.quantity,
            status: '0', 
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
            this.open = false;
            this.submitLoading = false;
            
            this.currentOrderId = response.data;
            this.currentTotal = orderData.totalAmount;
            this.currentOrderNo = response.orderNo || ("ORD" + new Date().getTime());

            this.openPayment = true;
        }).catch(() => {
            this.submitLoading = false;
        });
    },
    handlePaySuccess() {
        this.paymentLoading = true;
        const updateData = {
            orderId: this.currentOrderId,
            status: '1' // Paid
        };
        updateOrder(updateData).then(response => {
             this.$modal.msgSuccess("支付成功！");
             this.openPayment = false;
             this.paymentLoading = false;
             this.getProductDetail(this.product.productId); // Refresh
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
             this.getProductDetail(this.product.productId);
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
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20px;
}

.box-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.product-detail {
    padding: 20px;
}

.image-container {
    width: 100%;
    height: 400px;
    background: #fff;
    border: 1px solid #ebeef5;
    padding: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 12px;
    box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.02);
    
    .product-image {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
        transition: transform 0.3s ease;
        
        &:hover {
            transform: scale(1.02);
        }
    }
}

.product-title {
    font-size: 32px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 25px 0;
    line-height: 1.4;
}

.product-meta {
    background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
    padding: 25px;
    border-radius: 12px;
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border: 1px solid #e4e7ed;
    
    .price-tag {
        color: #F56C6C;
        font-size: 16px;
        
        .price-val {
            font-size: 40px;
            font-weight: 800;
            font-family: 'DIN Alternate', 'Segoe UI', sans-serif;
            margin-left: 5px;
        }
    }
    
    .stock-tag {
        color: #67C23A;
        font-weight: 600;
        background: rgba(103, 194, 58, 0.1);
        padding: 5px 12px;
        border-radius: 20px;
        
        &.no-stock {
            color: #F56C6C;
            background: rgba(245, 108, 108, 0.1);
        }
    }
}

.info-item {
    margin-bottom: 15px;
    font-size: 15px;
    display: flex;
    align-items: center;
    
    .label {
        display: inline-block;
        width: 120px;
        color: #909399;
        font-weight: 500;
    }
    .value {
        color: #606266;
        font-weight: 500;
        background: #f4f4f5;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 14px;
    }
}

.description-box {
    margin-top: 30px;
    background: #fafdff;
    padding: 20px;
    border-radius: 8px;
    border: 1px dashed #d9ecff;
    
    .desc-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 15px;
        color: #303133;
        display: flex;
        align-items: center;
        
        &::before {
            content: '';
            display: inline-block;
            width: 4px;
            height: 18px;
            background: #409EFF;
            margin-right: 10px;
            border-radius: 2px;
        }
    }
    .desc-content {
        line-height: 1.8;
        color: #606266;
        white-space: pre-wrap;
        font-size: 15px;
    }
}

.action-area {
    margin-top: 40px;
    display: flex;
    gap: 20px;
    
    .el-button {
      height: 46px; 
      font-size: 16px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      padding: 0 40px;
    }
}
</style>
