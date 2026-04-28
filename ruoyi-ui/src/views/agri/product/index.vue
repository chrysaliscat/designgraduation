<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="下架" value="1" />
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
          v-if="!isAdmin"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['agri:product:add']"
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
          v-hasPermi="['agri:product:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-if="!isAdmin"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['agri:product:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="商品ID" align="center" prop="productId" />
      <el-table-column label="商品名称" align="center" prop="productName" />
      <el-table-column label="图片" align="center" prop="productImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="getImageUrl(scope.row.productImage)" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="价格" align="center" prop="price" />
      <el-table-column label="库存" align="center" prop="stock" />
      <el-table-column label="产地" align="center" prop="origin" />
      <el-table-column label="农户" align="center" prop="farmer" />
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="生产日期" align="center" prop="productionDate" width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.productionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
          <el-tag v-else type="info">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <!-- 管理员视角 -->
          <template v-if="isAdmin">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleUpdate(scope.row)"
            >查看</el-button>
            <el-button
              v-if="scope.row.status === '0'"
              size="mini"
              type="text"
              icon="el-icon-bottom"
              style="color: #E6A23C"
              @click="handleToggleStatus(scope.row, '1')"
            >下架</el-button>
            <el-button
              v-else
              size="mini"
              type="text"
              icon="el-icon-top"
              style="color: #67C23A"
              @click="handleToggleStatus(scope.row, '0')"
            >恢复</el-button>
          </template>
          <!-- 卖家视角 -->
          <template v-else>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['agri:product:edit']"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['agri:product:remove']"
            >删除</el-button>
          </template>
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

    <!-- Add or Modify Dialog -->
    <el-dialog :title="isAdmin ? '监管处理 - 关停/恢复' : title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" :disabled="isAdmin" />
        </el-form-item>
        <el-form-item label="商品图片" prop="productImage">
          <image-upload v-model="form.productImage" :disabled="isAdmin" v-if="!isAdmin"/>
          <image-preview v-else :src="getImageUrl(form.productImage)" :width="100" :height="100"/>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" :disabled="isAdmin" />
          <el-tooltip v-if="!isAdmin" content="涉及财务金额，请务必核实后操作" placement="top">
            <i class="el-icon-warning-outline" style="margin-left: 10px; color: #E6A23C; cursor: pointer;"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :disabled="isAdmin" />
          <el-tooltip v-if="!isAdmin" content="涉及库存准确性，影响订单履行，请谨慎修改" placement="top">
            <i class="el-icon-warning-outline" style="margin-left: 10px; color: #E6A23C; cursor: pointer;"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" placeholder="请输入产地" :disabled="isAdmin" />
        </el-form-item>
        <el-form-item label="农户/合作社" prop="farmer">
          <el-input v-model="form.farmer" placeholder="请输入农户或合作社名称" :disabled="isAdmin" />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="form.batchNo" placeholder="请输入批次号" :disabled="isAdmin"/>
          <div v-if="!isAdmin" style="font-size: 12px; color: #F56C6C; margin-top: 5px;">
            <i class="el-icon-info"></i> 合规预警：溯源批次变更将被审计日志永久记录
          </div>
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
            v-model="form.productionDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            style="width: 100%"
            :disabled="isAdmin"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status" :disabled="isAdmin">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">下架</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入商品描述" :disabled="isAdmin" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <template v-if="isAdmin">
          <el-button type="primary" @click="cancel">关 闭</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listProduct, getProduct, delProduct, addProduct, updateProduct } from "@/api/agri/product";

export default {
  name: "Product",
  data() {
    return {
      // Mask layer
      loading: true,
      // Selection
      ids: [],
      // Non-single disabled
      single: true,
      // Multiple disabled
      multiple: true,
      // Show search
      showSearch: true,
      // Total
      total: 0,
      // Table data
      productList: [],
      // Title
      title: "",
      // Dialog open
      open: false,
      // Query params
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        productName: null,
        status: null,
      },
      // Form params
      form: {},
      // Validation rules
      rules: {
        productName: [
          { required: true, message: "商品名称不能为空", trigger: "blur" }
        ],
        price: [
          { required: true, message: "价格不能为空", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    isAdmin() {
      return this.$store.getters.roles.includes('admin') || this.$store.getters.userId === 1;
    }
  },
  created() {
    this.getList();
  },
  methods: {
    getImageUrl(url) {
      if (!url) return "";
      if (url.indexOf("http") !== -1) {
        return url.replace("http://localhost:8080", "");
      }
      if (url.startsWith("/products")) {
          return window.location.origin + url;
      }
      return url;
    },
    /** Query list */
    getList() {
      this.loading = true;
      listProduct(this.queryParams).then(response => {
        this.productList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** Cancel button */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** Reset form */
    reset() {
      this.form = {
        productId: null,
        productName: null,
        productImage: null,
        price: 0.00,
        stock: 0,
        stock: 0,
        origin: null,
        farmer: null,
        batchNo: null,
        productionDate: null,
        description: null,
        status: "0"
      };
      this.resetForm("form");
    },
    /** Search button */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** Selection change */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.productId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加农产品";
    },
    /** Update button */
    handleUpdate(row) {
      this.reset();
      const productId = row.productId || this.ids
      getProduct(productId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改农产品";
      });
    },
    /** 直接切换状态（快捷下架/恢复） */
    handleToggleStatus(row, status) {
      const text = status === "0" ? "恢复上架" : "监管下架";
      this.$modal.confirm('确认对商品 "' + row.productName + '" 执行 [' + text + '] 操作吗？').then(() => {
        const data = { productId: row.productId, status: status };
        return updateProduct(data);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {});
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.productId != null) {
            updateProduct(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addProduct(this.form).then(response => {
              this.$modal.msgSuccess("添加成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Delete button */
    handleDelete(row) {
      const productIds = row.productId || this.ids;
      this.$modal.confirm('是否确认删除商品ID为"' + productIds + '"的数据项？').then(function() {
        return delProduct(productIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
