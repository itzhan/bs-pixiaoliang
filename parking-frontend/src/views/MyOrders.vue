<template>
  <div class="orders-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#2D6A4F"><ReceiptOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的停车记录</h1>
        <p class="hero-subtitle">查看您的所有停车订单和支付状态</p>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== Summary Stats ===== -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(45,106,79,0.08);">
            <n-icon :size="22" color="#2D6A4F"><ListOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num">{{ total }}</span>
            <span class="stat-desc">总记录</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(224,122,95,0.08);">
            <n-icon :size="22" color="#E07A5F"><TimeOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num" style="color: var(--accent);">{{ stats.parking }}</span>
            <span class="stat-desc">进行中</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(45,106,79,0.08);">
            <n-icon :size="22" color="#2D6A4F"><CheckmarkCircleOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num" style="color: var(--primary);">{{ stats.completed }}</span>
            <span class="stat-desc">已完成</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(245,158,11,0.08);">
            <n-icon :size="22" color="#F59E0B"><WalletOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num" style="color: #F59E0B;">{{ stats.unpaid }}</span>
            <span class="stat-desc">待支付</span>
          </div>
        </div>
      </div>

      <!-- ===== Status Filter ===== -->
      <n-tabs v-model:value="currentStatus" type="line" @update:value="handleStatusChange">
        <n-tab v-for="tab in statusTabs" :key="tab.value" :name="tab.value">
          {{ tab.label }}
        </n-tab>
      </n-tabs>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="orders.length === 0" description="暂无停车记录" style="margin-top: 60px" />

      <!-- ===== Order Cards ===== -->
      <div v-else class="card-list">
        <div
          v-for="item in orders"
          :key="item.id"
          class="order-card"
        >
          <div class="card-color-bar" :style="{ background: getStatusColor(item.status) }" />
          <div class="card-inner">
            <div class="card-header">
              <span class="order-no">
                <n-icon :size="14" style="margin-right: 4px; vertical-align: -2px;"><DocumentTextOutline /></n-icon>
                {{ item.orderNo || item.id }}
              </span>
              <n-space :size="8">
                <n-tag :type="orderStatusTagType(item.status)" size="small" round>
                  {{ orderStatusLabel(item.status) }}
                </n-tag>
                <n-tag :type="payStatusTagType(item.paymentStatus)" size="small" round>
                  {{ payStatusLabel(item.paymentStatus) }}
                </n-tag>
              </n-space>
            </div>

            <div class="card-body">
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><CarSportOutline /></n-icon>
                <span class="label">车牌号</span>
                <span class="value plate-number">{{ item.plateNumber || '—' }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><LogInOutline /></n-icon>
                <span class="label">入场时间</span>
                <span class="value">{{ formatTime(item.entryTime) }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><LogOutOutline /></n-icon>
                <span class="label">出场时间</span>
                <span class="value">{{ formatTime(item.exitTime) }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><TimeOutline /></n-icon>
                <span class="label">停车时长</span>
                <span class="value">{{ item.duration || '—' }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><CashOutline /></n-icon>
                <span class="label">停车费用</span>
                <span class="value price-text">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
              </div>
            </div>

            <div class="card-footer" v-if="canPay(item)">
              <n-button type="primary" size="small" round @click="openPayModal(item)">
                <template #icon><n-icon><WalletOutline /></n-icon></template>
                去支付
              </n-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <n-pagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="total"
          @update:page="fetchList"
        />
      </div>

      <!-- Payment Modal -->
      <n-modal v-model:show="payModalVisible" preset="card" title="确认支付" style="max-width: 420px">
        <div class="pay-modal-content">
          <div class="pay-amount">
            <span>应付金额</span>
            <span class="price-text-large">¥{{ (payTarget?.amount ?? 0).toFixed(2) }}</span>
          </div>

          <n-form-item label="支付方式" :show-feedback="false" style="margin-top: 16px">
            <n-select
              v-model:value="payMethod"
              :options="payMethodOptions"
              placeholder="请选择支付方式"
            />
          </n-form-item>

          <div class="pay-actions">
            <n-button @click="payModalVisible = false">取消</n-button>
            <n-button type="primary" :loading="payLoading" :disabled="!payMethod" @click="handlePay">
              确认支付
            </n-button>
          </div>
        </div>
      </n-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyOrders } from '@/api/order'
import { createPayment } from '@/api/payment'
import {
  ReceiptOutline,
  ListOutline,
  TimeOutline,
  CheckmarkCircleOutline,
  WalletOutline,
  CarSportOutline,
  LogInOutline,
  LogOutOutline,
  CashOutline,
  DocumentTextOutline,
} from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

const message = useMessage()

// --- Status Config ---
const statusTabs = [
  { label: '全部', value: '' },
  { label: '进行中', value: '0' },
  { label: '已完成', value: '1' },
  { label: '已取消', value: '2' },
]

const orderStatusMap: Record<number, string> = {
  0: '进行中',
  1: '已完成',
  2: '已取消',
}
const orderStatusTagMap: Record<number, 'warning' | 'success' | 'error' | 'default'> = {
  0: 'warning',
  1: 'success',
  2: 'error',
}

const payStatusMap: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '已退款',
}
const payStatusTagMap: Record<number, 'warning' | 'success' | 'info' | 'default'> = {
  0: 'warning',
  1: 'success',
  2: 'info',
}

function orderStatusLabel(s: number) {
  return orderStatusMap[s] ?? '未知'
}
function orderStatusTagType(s: number) {
  return orderStatusTagMap[s] ?? 'default'
}
function payStatusLabel(s: number) {
  return payStatusMap[s] ?? '未知'
}
function payStatusTagType(s: number) {
  return payStatusTagMap[s] ?? 'default'
}

function canPay(item: any) {
  return item.status === 1 && item.paymentStatus === 0
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
}

function getStatusColor(status: number): string {
  const map: Record<number, string> = {
    0: '#F59E0B',
    1: '#2D6A4F',
    2: '#EF4444',
  }
  return map[status] ?? '#94A3B8'
}

// --- Data ---
const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const currentStatus = ref('')

const stats = computed(() => {
  return {
    parking: orders.value.filter(o => o.status === 0).length,
    completed: orders.value.filter(o => o.status === 1).length,
    unpaid: orders.value.filter(o => o.paymentStatus === 0).length,
  }
})

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize }
    if (currentStatus.value !== '') {
      params.status = currentStatus.value
    }
    const res: any = await getMyOrders(params)
    orders.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取停车记录失败')
  } finally {
    loading.value = false
  }
}

function handleStatusChange() {
  currentPage.value = 1
  fetchList()
}

// --- Payment Modal ---
const payModalVisible = ref(false)
const payLoading = ref(false)
const payTarget = ref<any>(null)
const payMethod = ref<string | null>(null)

const payMethodOptions = [
  { label: '微信支付', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '现金', value: 'CASH' },
  { label: '刷卡', value: 'CARD' },
]

function openPayModal(item: any) {
  payTarget.value = item
  payMethod.value = null
  payModalVisible.value = true
}

async function handlePay() {
  if (!payTarget.value || !payMethod.value) return
  payLoading.value = true
  try {
    await createPayment({
      orderId: payTarget.value.id,
      paymentMethod: payMethod.value,
    })
    message.success('支付成功')
    payModalVisible.value = false
    fetchList()
  } catch (e: any) {
    message.error(e.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
/* ===== Hero Header ===== */
.page-hero {
  position: relative;
  z-index: 0;
  padding: 40px 24px 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f7f4 0%, #e8f0f6 50%, #f5f0e8 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.hero-shape-1 {
  width: 280px;
  height: 280px;
  background: #2d6a4f;
  top: -80px;
  right: -60px;
}

.hero-shape-2 {
  width: 180px;
  height: 180px;
  background: #e07a5f;
  bottom: -60px;
  left: -40px;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.hero-icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(45, 106, 79, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.hero-subtitle {
  font-size: 15px;
  color: #94a3b8;
  margin: 0;
}

/* ===== Container ===== */
.orders-page .page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  position: relative;
  z-index: 1;
}

/* ===== Stats ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-desc {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* ===== Order Cards ===== */
.order-card {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.order-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-color-bar {
  width: 4px;
  flex-shrink: 0;
}

.card-inner {
  flex: 1;
  padding: 18px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 8px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-row .label {
  color: var(--text-tertiary);
  font-size: 13px;
  min-width: 64px;
  flex-shrink: 0;
}

.info-row .value {
  color: var(--text-primary);
  font-size: 14px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
}

/* ===== Payment Modal ===== */
.pay-modal-content {
  padding: 4px 0;
}

.pay-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.pay-amount > span:first-child {
  color: var(--text-secondary);
  font-size: 14px;
}

.pay-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .orders-page .page-container {
    padding: 24px 16px 40px;
  }
}
</style>
