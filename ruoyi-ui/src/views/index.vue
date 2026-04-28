<template>
  <div class="dashboard-editor-container">
    <div v-if="!isBuyer">
    <el-row :gutter="40" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
        <div class="card-panel" @click="$router.push('/order')">
          <div class="card-panel-icon-wrapper icon-money">
            <i class="el-icon-money card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              销售总额
            </div>
            <count-to :start-val="0" :end-val="totalSales" :duration="800" :decimals="2" class="card-panel-num" />
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
        <div class="card-panel" @click="$router.push('/order')">
          <div class="card-panel-icon-wrapper icon-shopping">
            <i class="el-icon-s-order card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              订单数量
            </div>
            <count-to :start-val="0" :end-val="orderCount" :duration="800" class="card-panel-num" />
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
        <div class="card-panel" @click="handleUserCardClick">
          <div class="card-panel-icon-wrapper icon-people">
            <i class="el-icon-user card-panel-icon" />
          </div>
          <div class="card-panel-description">
            <div class="card-panel-text">
              {{ userLabel }}
            </div>
            <count-to :start-val="0" :end-val="userCount" :duration="800" class="card-panel-num" />
          </div>
        </div>
      </el-col>
    </el-row>


    </div>

    <!-- BUYER VIEW: Rich Dashboard -->
    <div v-if="isBuyer" class="buyer-dashboard">
      <!-- 1. Banner Carousel -->
      <el-carousel :interval="5000" type="card" height="300px">
        <el-carousel-item v-for="item in carouselItems" :key="item.id">
           <div class="banner-content" :style="{ background: item.color }">
              <h1 style="color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.3);">{{ item.title }}</h1>
              <p style="color: #f0f0f0; font-size: 18px;">{{ item.desc }}</p>
           </div>
        </el-carousel-item>
      </el-carousel>

      <!-- 2. Quick Categories -->
      <el-row :gutter="20" class="category-section">
         <el-col :span="6" v-for="cat in categories" :key="cat.name">
            <el-card shadow="hover" class="category-card" @click.native="$router.push('/store')">
               <div style="text-align: center;">
                  <i :class="cat.icon" style="font-size: 32px; color: #409EFF; margin-bottom: 10px;"></i>
                  <div style="font-weight: bold;">{{ cat.name }}</div>
               </div>
            </el-card>
         </el-col>
      </el-row>

      <!-- 3. Recommended Products -->
      <div class="section-title">
          <span><i class="el-icon-star-on" style="color: #F56C6C;"></i> 今日优选</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="$router.push('/store')">查看更多 >></el-button>
      </div>

      <el-row :gutter="20">
         <el-col :span="6" v-for="product in recommendedProducts" :key="product.productId" style="margin-bottom: 20px;">
            <el-card :body-style="{ padding: '0px' }" shadow="hover">
               <div class="image-placeholder" style="height: 150px; background: #f5f7fa; display: flex; align-items: center; justify-content: center;">
                  <img v-if="product.productImage" :src="getImageUrl(product.productImage)" style="max-width: 100%; max-height: 100%;">
                  <span v-else style="color: #909399;"><i class="el-icon-picture-outline"></i> 暂无图片</span>
               </div>
               <div style="padding: 14px;">
                  <span class="product-name" style="font-weight: bold; display: block; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">{{ product.productName }}</span>
                  <div class="bottom clearfix" style="margin-top: 10px; display: flex; justify-content: space-between; align-items: center;">
                     <span class="price" style="color: #F56C6C; font-size: 16px;">¥ {{ product.price }}</span>
                     <el-tag size="mini" type="success">{{ product.batchCode || '热销' }}</el-tag>
                  </div>
               </div>
            </el-card>
         </el-col>
      </el-row>

      <!-- Notices (Admin Only) -->
        <el-card v-if="noticeList.length > 0 && roles.includes('admin')" style="margin-top: 40px;">
            <div slot="header">
                <span><i class="el-icon-bell"></i> 平台公告</span>
            </div>
            <div v-for="item in noticeList" :key="item.noticeId" style="margin-bottom: 15px; border-bottom: 1px dashed #eee; padding-bottom: 10px;">
                <el-tag size="mini" :type="item.noticeType == '1' ? 'warning' : 'success'">{{ item.noticeType == '1' ? '通知' : '公告' }}</el-tag>
                <span style="font-weight: bold; margin-left: 10px;">{{ item.noticeTitle }}</span>
                <span style="float: right; color: #999; font-size: 12px;">{{ item.createTime }}</span>
                <div style="margin-top: 5px; color: #666; font-size: 13px; padding-left: 45px;" v-html="item.noticeContent"></div>
            </div>
        </el-card>
    </div>

    <!-- SELLER/ADMIN VIEW: Charts -->
    <div v-if="!isBuyer">
      <!-- Admin/Seller Notice Section (Admin Only) -->
      <el-card v-if="noticeList.length > 0 && roles.includes('admin')" shadow="hover" style="margin-bottom: 20px;">
         <div slot="header" class="clearfix">
            <span><i class="el-icon-bell"></i> 平台公告</span>
         </div>
         <div v-for="item in noticeList" :key="item.noticeId" style="margin-bottom: 10px; font-size: 14px; border-bottom: 1px dashed #eee; padding-bottom: 5px; cursor: pointer;">
            <el-tag size="mini" :type="item.noticeType == '1' ? 'warning' : 'success'" style="margin-right: 10px;">{{ item.noticeType == '1' ? '通知' : '公告' }}</el-tag>
            {{ item.noticeTitle }}
            <span style="float: right; color: #999; font-size: 12px;">{{ item.createTime }}</span>
         </div>
      </el-card>
    <!-- 2. Charts Row -->
    <el-row :gutter="32">
      <!-- Sales Trend Line Chart -->
      <el-col :xs="24" :sm="24" :lg="16">
        <div class="chart-wrapper">
          <div class="chart-title">七日销售趋势</div>
          <div id="lineChart" style="width: 100%; height: 350px;"></div>
        </div>
      </el-col>
      
      <!-- Category Pie Chart -->
      <el-col :xs="24" :sm="24" :lg="8">
        <div class="chart-wrapper">
          <div class="chart-title">分类占比</div>
          <div id="pieChart" style="width: 100%; height: 350px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 3. Top Products Table -->
    <el-row :gutter="8">
      <el-col :span="24">
        <el-card shadow="hover" class="box-card" style="margin-top: 20px;">
            <div slot="header" class="clearfix">
                <span>热销商品榜</span>
            </div>
            <el-table :data="topProducts" style="width: 100%">
                <el-table-column prop="name" label="商品名称" />
                <el-table-column label="Sales" width="180">
                    <template slot-scope="scope">
                        <el-progress :percentage="scope.row.percentage" :format="() => scope.row.count"></el-progress>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script>
import CountTo from 'vue-count-to'
import * as echarts from 'echarts'
import { getDashboardData } from "@/api/agri/data";
import { listNotice } from "@/api/system/notice";
import { listProduct } from "@/api/agri/product";

require('echarts/theme/macarons') // echarts theme

export default {
  name: 'Index',
  components: {
    CountTo
  },
  data() {
    return {
      totalSales: 0,
      orderCount: 0,
      userCount: 0,
      userLabel: '用户总数',
      topProducts: [],
      lineChart: null,
      topProducts: [],
      lineChart: null,
      pieChart: null,
      noticeList: [],
      // Buyer Dashboard Data
      carouselItems: [
        { id: 1, title: '新鲜直达', desc: '每日清晨采摘，直发您的餐桌', color: 'linear-gradient(to right, #4facfe 0%, #00f2fe 100%)' },
        { id: 2, title: '绿色无公害', desc: '严格质检，守护您的舌尖安全', color: 'linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%)' },
        { id: 3, title: '助农惠民', desc: '直接对接农户，没有中间商赚差价', color: 'linear-gradient(to right, #fa709a 0%, #fee140 100%)' }
      ],
      categories: [
        { name: '新鲜蔬菜', icon: 'el-icon-grape' },
        { name: '时令水果', icon: 'el-icon-apple' },
        { name: '肉禽蛋品', icon: 'el-icon-chicken' },
        { name: '粮油米面', icon: 'el-icon-dish' }
      ],
      recommendedProducts: []
    }
  },
  computed: {
    roles() {
        return this.$store.getters.roles;
    },
    isBuyer() {
        return this.roles.includes('agri_buyer');
    },
    isSeller() {
        return this.roles.includes('agri_seller');
    }
  },
  mounted() {
    this.initData();
    window.addEventListener('resize', this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts);
    if (!this.lineChart) {
      return
    }
    this.lineChart.dispose()
    this.lineChart = null
    if (!this.pieChart) {
        return
    }
    this.pieChart.dispose()
    this.pieChart = null
  },
  methods: {
    initData() {
        if (this.roles.includes('admin')) {
             this.getNotices();
        }

        if (this.isBuyer) {
             // Fetch Recommended Products
             listProduct({ pageNum: 1, pageSize: 8 }).then(res => {
                this.recommendedProducts = res.rows;
             });
             return; 
        }

        getDashboardData().then(response => {
            const data = response.data;
            this.totalSales = data.totalSales;
            this.orderCount = data.orderCount;
            this.userCount = data.userCount;
            this.userLabel = data.userLabel;
            
            // Process Top Products for Progress Bar
            let maxCount = 0;
            data.topProducts.forEach(item => {
                if(item.count > maxCount) maxCount = item.count;
            });
            this.topProducts = data.topProducts.map(item => {
                return {
                    name: item.name,
                    count: item.count,
                    percentage: maxCount === 0 ? 0 : Math.round((item.count / maxCount) * 100)
                };
            });

            this.initLineChart(data.trendDates, data.trendSales);
            this.initPieChart(data.categoryRatio);
        });
    },
    initLineChart(dates, values) {
        this.lineChart = echarts.init(document.getElementById('lineChart'), 'macarons')
        this.lineChart.setOption({
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: [
                {
                    type: 'category',
                    boundaryGap: false,
                    data: dates
                }
            ],
            yAxis: [
                {
                    type: 'value'
                }
            ],
            series: [
                {
                    name: 'Sales',
                    type: 'line',
                    stack: 'Total',
                    smooth: true,
                    areaStyle: {},
                    emphasis: {
                        focus: 'series'
                    },
                    data: values,
                    itemStyle: {
                        color: '#40c9c6'
                    },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#40c9c6' },
                        { offset: 1, color: '#fff' }
                        ])
                    }
                }
            ]
        })
    },
    getNotices() {
        listNotice({ pageNum: 1, pageSize: 5 }).then(response => {
             this.noticeList = response.rows;
        });
    },

    initPieChart(data) {
        this.pieChart = echarts.init(document.getElementById('pieChart'), 'macarons')
        this.pieChart.setOption({
            tooltip: {
                trigger: 'item',
                formatter: '{a} <br/>{b} : {c} ({d}%)'
            },
            legend: {
                left: 'center',
                bottom: '10',
                data: data.map(item => item.name)
            },
            series: [
                {
                    name: 'Category Sales',
                    type: 'pie',
                    roseType: 'radius',
                    radius: [15, 95],
                    center: ['50%', '38%'],
                    data: data,
                    animationEasing: 'cubicInOut',
                    animationDuration: 2600
                }
            ]
        })
    },
    resizeCharts() {
        if(this.lineChart) this.lineChart.resize();
        if(this.pieChart) this.pieChart.resize();
    },
    getImageUrl(url) {
        if (!url) return '';
        if (url.indexOf('http') === 0) return url;
        return process.env.VUE_APP_BASE_API + url;
    },
    handleUserCardClick() {
        if (this.isSeller) {
            this.$router.push('/agri/product');
        } else {
            this.$router.push('/system/user');
        }
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: #f0f2f5;
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
  
  .chart-title {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin-bottom: 15px;
      padding-left: 10px;
      border-left: 4px solid #409EFF;
  }
}

.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px;
      margin-left: 0px;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

@media (max-width:550px) {
  .card-panel-description {
    display: none;
  }

  .card-panel-icon-wrapper {
    float: none !important;
    width: 100%;
    height: 100%;
    margin: 0 !important;

    .svg-icon {
      display: block;
      margin: 14px auto !important;
      float: none !important;
    }
  }
}

// Buyer Dashboard Styles
.banner-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
}
.category-card {
    cursor: pointer;
    transition: all 0.3s;
    &:hover {
        transform: translateY(-5px);
    }
}
.section-title {
    margin: 30px 0 20px;
    font-size: 20px;
    font-weight: bold;
    border-bottom: 2px solid #F56C6C;
    padding-bottom: 10px;
}
</style>
