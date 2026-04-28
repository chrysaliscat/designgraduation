<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable size="small">
          <el-option label="待支付" value="0" />
          <el-option label="已支付" value="1" />
          <el-option label="已发货" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['agri:order:add']"
          v-if="!isAdmin"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['agri:order:edit']"
          v-if="!isAdmin"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['agri:order:remove']"
          v-if="!isAdmin"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['agri:order:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="orderId" />
      <el-table-column label="订单号" align="center" prop="orderNo" />
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
           <el-tag v-if="scope.row.status == '0'" type="warning">待支付</el-tag>
           <el-tag v-if="scope.row.status == '1'" type="primary">已支付</el-tag>
           <el-tag v-if="scope.row.status == '2'" type="info">已发货</el-tag>
           <el-tag v-if="scope.row.status == '3'" type="success">已完成</el-tag>
           <el-tag v-if="scope.row.status == '4'" type="danger">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收货人" align="center" prop="receiverName" />
      <el-table-column label="联系电话" align="center" prop="receiverPhone" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleUpdate(scope.row)"
            v-if="isAdmin"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-if="isBuyer && scope.row.status < 2"
          >修改信息</el-button>
          <el-button
             size="mini"
             type="text"
             icon="el-icon-bank-card"
             style="color: #67C23A;"
             @click="handlePay(scope.row)"
             v-if="isBuyer && scope.row.status == '0'"
          >立即支付</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleUpdate(scope.row)"
            v-if="isSeller || (isBuyer && scope.row.status >= 2)"
          >详情</el-button>
          
          <!-- Workflow Buttons -->
          <el-button
            v-if="isSeller && scope.row.status == '1'"
            size="mini"
            type="text"
            icon="el-icon-truck"
            @click="handleShip(scope.row)"
          >发货</el-button>
          <el-button
            v-if="isBuyer && scope.row.status == '2'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleConfirm(scope.row)"
          >确认收货</el-button>
          <el-button
            v-if="isBuyer && (scope.row.status == '0' || scope.row.status == '1')"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleCancel(scope.row)"
          >取消订单</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['agri:order:remove']"
            v-if="isBuyer && (scope.row.status == '3' || scope.row.status == '4')"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Add or Update Order Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" :disabled="isAdmin">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="系统自动生成" :disabled="form.orderId != null" />
        </el-form-item>
        <el-form-item label="买家账号" v-if="form.buyerName">
           <el-input v-model="form.buyerName" disabled />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-radio-group v-model="form.status" disabled>
            <el-radio label="0">待支付</el-radio>
            <el-radio label="1">已支付</el-radio>
            <el-radio label="2">已发货</el-radio>
            <el-radio label="3">已完成</el-radio>
            <el-radio label="4">已取消</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="收货人姓名" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="收货人电话" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入收货人电话" />
        </el-form-item>
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input v-model="form.receiverAddress" type="textarea" placeholder="请输入收货地址" />
        </el-form-item>
        
        <el-divider content-position="center">订单明细</el-divider>
        <el-row :gutter="10" class="mb8" v-if="false">
          <el-col :span="1.5">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAddAgOrderItem">添加商品</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleDeleteAgOrderItem">删除选中</el-button>
          </el-col>
        </el-row>
        <el-table :data="agOrderItemList" :row-class-name="rowAgOrderItemIndex" @selection-change="handleAgOrderItemSelectionChange" ref="agOrderItem">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="店铺/卖家" prop="sellerName" width="120">
            <template slot-scope="scope">
                <el-tag size="mini" type="info" v-if="scope.row.sellerName">{{ scope.row.sellerName }}</el-tag>
                <span v-else>自营/未知</span>
            </template>
          </el-table-column>
          <el-table-column label="商品名称" prop="productName" width="200">
            <template slot-scope="scope">
              <el-input v-model="scope.row.productName" placeholder="商品名称" disabled />
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="productPrice" width="150">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.productPrice" :min="0" :precision="2" controls-position="right" size="small" disabled />
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="150">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" :max="9999" size="small" disabled />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" v-if="!isAdmin && isBuyer && form.status < 2">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- Payment QR Code Dialog -->
    <el-dialog title="扫码支付" :visible.sync="openPayment" width="400px" append-to-body center :close-on-click-modal="false">
        <div style="text-align: center;">
            <p style="font-size: 16px; margin-bottom: 20px;">订单号: <span style="color: #409EFF; font-weight: bold;">{{ currentOrderNo }}</span></p>
            <p>应付金额: <span style="color: #F56C6C; font-size: 20px; font-weight: bold;">¥{{ currentTotal }}</span></p>
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MockPayment" style="margin: 20px 0; border: 1px solid #ddd; padding: 10px; border-radius: 4px;">
            <p style="color: #909399; font-size: 13px;">请使用微信或支付宝扫码 (模拟)</p>
        </div>
        <div slot="footer" class="dialog-footer">
            <el-button type="success" :loading="paymentLoading" @click="handlePaySuccess" style="width: 100%">我已完成支付</el-button>
        </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, delOrder, addOrder, updateOrder, exportOrder } from "@/api/agri/order";

export default {
  name: "Order",
  data() {
    return {
      // Mask layer
      loading: true,
      // Selection array
      ids: [],
      // Sub-table selection
      checkedAgOrderItem: [],
      // Non-single disabled
      single: true,
      // Non-multiple disabled
      multiple: true,
      // Show search
      showSearch: true,
      // Total records
      total: 0,
      // Order table data
      orderList: [],
      // Order Item table data
      agOrderItemList: [],
      // Popup layer title
      title: "",
      // Check if popup is open
      open: false,
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        status: null,
      },
      // Form parameters
      form: {},
      // Form validation
      rules: {
        receiverName: [
          { required: true, message: "Receiver Name cannot be empty", trigger: "blur" }
        ],
        receiverPhone: [
          { required: true, message: "Receiver Phone cannot be empty", trigger: "blur" }
        ],
      },
      // Payment Dialog Data
      openPayment: false,
      paymentLoading: false,
      currentOrderId: null,
      currentOrderNo: '',
      currentTotal: 0
    };
  },
  created() {
    this.getList();
  },
  computed: {
    isAdmin() {
        return this.$store.getters.roles.includes('admin');
    },
    isBuyer() {
        return this.$store.getters.roles.includes('agri_buyer');
    },
    isSeller() {
        return this.$store.getters.roles.includes('agri_seller');
    }
  },
  activated() {
    this.getList();
  },
  methods: {
    /** Query Order List */
    getList() {
      this.loading = true;
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // Reset form
    reset() {
      this.form = {
        orderId: null,
        orderNo: null,
        userId: null,
        totalAmount: null,
        status: "0",
        receiverName: null,
        receiverPhone: null,
        receiverAddress: null,
        createTime: null,
        payTime: null
      };
      this.agOrderItemList = [];
      this.resetForm("form");
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
    // Multi-select box selected data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.orderId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加订单";
    },
    /** Update button operation */
    handleUpdate(row) {
      this.reset();
      const orderId = row.orderId || this.ids
      getOrder(orderId).then(response => {
        this.form = response.data;
        this.agOrderItemList = response.data.orderItemList;
        this.open = true;
        this.title = this.isAdmin ? "订单详情" : "修改订单";
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.orderItemList = this.agOrderItemList;
          if (this.form.orderId != null) {
            updateOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addOrder(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const orderIds = row.orderId || this.ids;
      this.$modal.confirm('是否确认删除订单编号为"' + orderIds + '"的数据项？').then(() => {
        return delOrder(orderIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** Order Item Index */
    rowAgOrderItemIndex({ row, rowIndex }) {
      row.index = rowIndex + 1;
    },
    /** Order Item Add Button */
    handleAddAgOrderItem() {
      let obj = {};
      obj.productName = "";
      obj.productPrice = "";
      obj.quantity = "";
      obj.subtotal = "";
      this.agOrderItemList.push(obj);
    },
    /** Order Item Delete Button */
    handleDeleteAgOrderItem() {
      if (this.checkedAgOrderItem.length == 0) {
        this.$modal.msgError("请先选择要删除的订单明细");
      } else {
        const agOrderItemList = this.agOrderItemList;
        const checkedAgOrderItem = this.checkedAgOrderItem;
        this.agOrderItemList = agOrderItemList.filter(function(item) {
          return checkedAgOrderItem.indexOf(item) === -1
        });
      }
    },
    /** Order Item Selection */
    handleAgOrderItemSelectionChange(selection) {
      this.checkedAgOrderItem = selection;
    },
    /** Export button operation */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有订单数据项？').then(() => {
        this.exportLoading = true;
        return exportOrder(queryParams);
      }).then(response => {
        this.$download.name(response.msg);
        this.exportLoading = false;
      }).catch(() => {});
    },
    /** Ship Order */
    handleShip(row) {
      this.$modal.confirm('确认发货订单 "' + row.orderNo + '" 吗？').then(() => {
        const oldStatus = row.status;
        row.status = '2'; // Update status first, backend should check
        return updateOrder(row).catch(err => {
          row.status = oldStatus;
          throw err;
        });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("发货成功");
      }).catch(() => {
        this.getList();
      });
    },
    /** Confirm Receipt */
    handleConfirm(row) {
      this.$modal.confirm('确认已收到订单 "' + row.orderNo + '" 的货物吗？').then(() => {
        const oldStatus = row.status;
        row.status = '3';
        return updateOrder(row).catch(err => {
          row.status = oldStatus;
          throw err;
        });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("收货成功");
      }).catch(() => {
        this.getList();
      });
    },
    /** Cancel Order */
    handleCancel(row) {
      this.$modal.confirm('确认取消订单 "' + row.orderNo + '" 吗？').then(() => {
        // Validation handled by backend or status check
        if (parseInt(row.status) >= 2) {
             this.$modal.msgError("货物已发送，无法取消");
             return Promise.reject();
        }
        row.status = '4';
        return updateOrder(row);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("取消成功");
      }).catch((err) => {
          // If backend rejects (e.g. already shipped), check error message
          // Because request.js may have already shown a msgError, we might want a specific Alert here
          // But catching it here usually suppresses the default handler if we don't re-throw.
          // Let's assume request.js handles the toast. The USER wants a specific Dialog.
          // Since we can't easily access the error message object structure uniformly without inspection,
          // we'll just check if the list status has changed.
          
          this.getList(); // Refresh first
          
          // Show explicit Alert as requested
          if (err && (err.msg || '').includes('shipped') || (err.msg || '').includes('发货')) {
                this.$alert('该商品商家已发货，无法取消！', '提示', {
                    confirmButtonText: '确定',
                    type: 'warning'
                });
          }
      });
    },
    /** Pay Order */
    handlePay(row) {
       this.currentOrderId = row.orderId;
       this.currentOrderNo = row.orderNo;
       this.currentTotal = row.totalAmount;
       this.openPayment = true;
    },
    handlePaySuccess() {
        this.paymentLoading = true;
        updateOrder({ 
            orderId: this.currentOrderId,
            status: '1'
        }).then(() => {
            this.$modal.msgSuccess("支付成功");
            this.openPayment = false;
            this.paymentLoading = false;
            this.getList();
        }).catch(() => {
            this.paymentLoading = false;
        });
    }
  }
};
</script>
