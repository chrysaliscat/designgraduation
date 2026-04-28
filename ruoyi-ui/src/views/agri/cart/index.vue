<template>
  <div class="app-container">
    <h2>我的购物车</h2>
    
    <el-table v-loading="loading" :data="cartList" style="width: 100%">
      <el-table-column label="商品图片" width="120">
        <template slot-scope="scope">
           <!-- Ideally should use a base url component or helper -->
           <img :src="getImageUrl(scope.row.productImg)" width="80" height="80" style="object-fit:cover; border-radius: 4px;"/>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column label="单价" width="120">
        <template slot-scope="scope">
           <span style="color: red; font-weight: bold;">￥{{ scope.row.productPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="200">
        <template slot-scope="scope">
          <el-input-number v-model="scope.row.quantity" @change="handleChange(scope.row)" :min="1" :max="99" size="small"></el-input-number>
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template slot-scope="scope">
           <span style="color: red;">￥{{ (scope.row.productPrice * scope.row.quantity).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收货人" width="120" prop="receiverName">
        <template slot-scope="scope">
            <div>{{ scope.row.receiverName }}</div>
            <div style="font-size: 12px; color: #999;">{{ scope.row.receiverPhone }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-view" @click="goDetail(scope.row)" style="color: #409EFF;">详情</el-button>
          <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" style="color: #E6A23C;">修改</el-button>
          <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" style="color: #f56c6c;">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-row type="flex" justify="end" style="margin-top: 20px; align-items: center;">
        <div style="margin-right: 20px; font-size: 18px;">
            总计: <span style="color: red; font-weight: bold; font-size: 24px;">￥{{ totalPrice }}</span>
        </div>
        <el-button type="primary" size="medium" @click="handleCheckout" :disabled="cartList.length === 0">立即结算</el-button>
    </el-row>

    <!-- Payment QR Code Dialog -->
    <el-dialog title="扫码支付" :visible.sync="openPayment" width="400px" append-to-body center :close-on-click-modal="false" :show-close="false">
        <div style="text-align: center;">
            <p style="font-size: 16px; margin-bottom: 20px;">订单已生成! <span v-if="createdOrderIds.length > 1">(共 {{createdOrderIds.length}} 单合并)</span></p>
            <p>实付金额: <span style="color: #F56C6C; font-size: 20px; font-weight: bold;">¥{{ finalPaymentAmount }}</span></p>
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MockPayment" style="margin: 20px 0; border: 1px solid #ddd; padding: 10px; border-radius: 4px;">
            <p style="color: #909399; font-size: 13px;">请使用微信或支付宝扫码 (模拟)</p>
        </div>
        <div slot="footer" class="dialog-footer">
            <el-button :loading="paymentLoading" @click="cancelPayment">取消支付</el-button>
            <el-button type="success" :loading="paymentLoading" @click="confirmPayment">我已完成支付</el-button>
        </div>
    </el-dialog>

    <!-- Edit Cart Dialog -->
    <el-dialog title="修改购物车商品" :visible.sync="openEdit" width="500px" append-to-body>
      <el-form ref="editForm" :model="editForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <img :src="getImageUrl(editForm.productImg)" style="width: 100%; max-width: 120px; height: 120px; object-fit: cover; border-radius: 8px; border: 1px solid #eee;">
          </el-col>
          <el-col :span="16">
            <el-form-item label="商品名称">
              <el-input v-model="editForm.productName" disabled />
            </el-form-item>
            <el-form-item label="单价">
              <el-input :value="editForm.productPrice" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider></el-divider>
        <el-form-item label="购买数量">
          <el-input-number v-model="editForm.quantity" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="总金额">
          <span style="color: red; font-size: 22px; font-weight: bold;">￥{{ (editForm.productPrice * editForm.quantity).toFixed(2) }}</span>
        </el-form-item>
        <el-divider></el-divider>
        <el-form-item label="收货人姓名">
          <el-input v-model="editForm.receiverName" placeholder="请输入您的姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.receiverPhone" placeholder="请输入您的电话" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="editForm.receiverAddress" placeholder="请输入详细地址" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEdit">确认修改</el-button>
        <el-button @click="openEdit = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCart, updateCart, delCart } from "@/api/agri/cart";
import { addOrder, updateOrder } from "@/api/agri/order";

export default {
  name: "Cart",
  data() {
    return {
      loading: true,
      cartList: [],
      openPayment: false,
      openEdit: false,
      paymentLoading: false,
      paymentAmount: 0,
      finalPaymentAmount: 0,
      createdOrderIds: [],
      editForm: {
        cartId: null,
        productName: '',
        productPrice: 0,
        quantity: 1
      }
    };
  },
  computed: {
      totalPrice() {
          let total = 0;
          this.cartList.forEach(item => {
              total += item.productPrice * item.quantity;
          });
          return total.toFixed(2);
      }
  },
  created() {
    this.getList();
  },
  activated() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listCart().then(response => {
        this.cartList = response.rows || [];
        this.loading = false;
      }).catch(err => {
        console.error("Cart API Error:", err);
        this.loading = false;
      });
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.indexOf("http") !== -1) return url;
      return process.env.VUE_APP_BASE_API + url;
    },
    handleChange(row) {
        updateCart(row).then(res => {
            // Optional: Success message
        });
    },
    goDetail(row) {
        this.$router.push('/agri/store/detail/' + row.productId);
    },
    handleEdit(row) {
        this.editForm = {
            cartId: row.cartId,
            productName: row.productName,
            productPrice: row.productPrice,
            productImg: row.productImg,
            quantity: row.quantity,
            receiverName: row.receiverName || '',
            receiverPhone: row.receiverPhone || '',
            receiverAddress: row.receiverAddress || ''
        };
        this.openEdit = true;
    },
    submitEdit() {
        updateCart({
            cartId: this.editForm.cartId,
            quantity: this.editForm.quantity,
            receiverName: this.editForm.receiverName,
            receiverPhone: this.editForm.receiverPhone,
            receiverAddress: this.editForm.receiverAddress
        }).then(res => {
            this.$modal.msgSuccess("修改成功");
            this.openEdit = false;
            this.getList();
        });
    },
    handleDelete(row) {
        this.$confirm('是否确认删除该商品?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return delCart(row.cartId);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    handleCheckout() {
        const loading = this.$loading({
          lock: true,
          text: '正在生成订单...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        this.createdOrderIds = []; // Reset tracked IDs
        let promises = [];
        
        this.cartList.forEach(item => {
            let orderData = {
                receiverName: item.receiverName,
                receiverPhone: item.receiverPhone,
                receiverAddress: item.receiverAddress,
                totalAmount: item.productPrice * item.quantity,
                status: '0', // Pending Payment
                orderItemList: [
                    {
                        productId: item.productId,
                        productName: item.productName,
                        productImg: item.productImg,
                        productPrice: item.productPrice,
                        quantity: item.quantity,
                        subtotal: item.productPrice * item.quantity,
                    }
                ]
            };
            promises.push(addOrder(orderData));
        });

        // Calculate and store total amount immediately (Snapshot)
        let total = 0;
        this.cartList.forEach(item => {
            total += item.productPrice * item.quantity;
        });
        this.finalPaymentAmount = total.toFixed(2);
        
        Promise.all(promises.map(promise => {
            return promise
                .then(res => ({ success: true, res }))
                .catch(err => ({ success: false, err }));
        })).then(results => {
             const successResults = results.filter(item => item.success);
             const failedResults = results.filter(item => !item.success);
             this.createdOrderIds = successResults.map(item => item.res.data).filter(Boolean);

             if (failedResults.length > 0) {
                 const cancelPromises = this.createdOrderIds.map(orderId => {
                     return updateOrder({ orderId: orderId, status: '4' }).catch(() => {});
                 });
                 return Promise.all(cancelPromises).then(() => {
                     this.createdOrderIds = [];
                     loading.close();
                     this.getList();
                     this.$modal.msgError("部分商品库存不足或已下架，已取消已生成订单，请重新结算");
                 });
             }

             loading.close();
             this.$modal.msgSuccess("订单已生成，请支付");
             this.openPayment = true; // Show Payment Dialog
        });
    },
    confirmPayment() {
        this.openPayment = false;
        const loading = this.$loading({
          lock: true,
          text: '正在支付...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        if (!this.createdOrderIds || this.createdOrderIds.length === 0) {
            loading.close();
            this.$modal.msgSuccess("支付成功");
            this.$router.push('/order');
            return;
        }

        // Update all created orders to Paid (Status 1)
        let updatePromises = this.createdOrderIds.map(orderId => {
            return updateOrder({ 
                orderId: orderId,
                status: '1'
            });
        });
        
        Promise.all(updatePromises).then(() => {
             let deletePromises = this.cartList.map(item => delCart(item.cartId));
             return Promise.all(deletePromises);
        }).then(() => {
             loading.close();
             this.$modal.msgSuccess("支付成功！");
             this.getList();
             this.$router.push('/order');
        }).catch(err => {
             loading.close();
             this.$modal.msgError("支付失败，请到订单列表重试");
             this.$router.push('/order');
        });
    },
    cancelPayment() {
        if (!this.createdOrderIds || this.createdOrderIds.length === 0) {
            this.openPayment = false;
            return;
        }

        this.paymentLoading = true;
        const cancelPromises = this.createdOrderIds.map(orderId => {
            return updateOrder({
                orderId: orderId,
                status: '4'
            });
        });

        Promise.all(cancelPromises).then(() => {
            this.$modal.msgSuccess("已取消支付，购物车商品已保留");
            this.openPayment = false;
            this.createdOrderIds = [];
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
