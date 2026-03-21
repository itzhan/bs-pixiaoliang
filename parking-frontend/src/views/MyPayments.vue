<template>
  <div class="payments-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#E07A5F"><CardOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的支付记录</h1>
        <p class="hero-subtitle">查看您的所有支付详情和交易流水</p>
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
            <span class="stat-desc">总笔数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(224,122,95,0.08);">
            <n-icon :size="22" color="#E07A5F"><CashOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num" style="color: var(--accent);">¥{{ totalAmount }}</span>
            <span class="stat-desc">总金额</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon-wrapper" style="background: rgba(45,106,79,0.08);">
            <n-icon :size="22" color="#2D6A4F"><CheckmarkCircleOutline /></n-icon>
          </div>
          <div class="stat-info">
            <span class="stat-num" style="color: var(--primary);">{{ paidCount }}</span>
            <span class="stat-desc">已支付</span>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="payments.length === 0" description="暂无支付记录" style="margin-top: 60px" />

      <!-- ===== Payment Cards ===== -->
      <div v-else class="card-list">
        <div
          v-for="item in payments"
          :key="item.id"
          class="payment-card"
        >
          <div class="card-color-bar" :style="{ background: getPayStatusColor(item.status) }" />
          <div class="card-inner">
            <div class="card-header">
              <span class="order-no">
                <n-icon :size="14" style="margin-right: 4px; vertical-align: -2px;"><DocumentTextOutline /></n-icon>
                {{ item.paymentNo || item.id }}
              </span>
              <n-tag :type="payStatusTagType(item.status)" size="small" round>
                {{ payStatusLabel(item.status) }}
              </n-tag>
            </div>

            <div class="card-body">
              <div class="amount-highlight">
                <span class="amount-label">支付金额</span>
                <span class="price-text-large">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><WalletOutline /></n-icon>
                <span class="label">支付方式</span>
                <n-tag :type="payMethodTagType(item.paymentMethod)" size="small">
                  {{ payMethodLabel(item.paymentMethod) }}
                </n-tag>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><TimeOutline /></n-icon>
                <span class="label">支付时间</span>
                <span class="value">{{ formatTime(item.payTime || item.createTime) }}</span>
              </div>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMessage, NIcon } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyPayments } from '@/api/payment'
import {
  CardOutline,
  ListOutline,
  CashOutline,
  CheckmarkCircleOutline,
  WalletOutline,
  TimeOutline,
  DocumentTextOutline,
} from '@vicons/ionicons5'

const message = useMessage()

// --- Status ---
const payStatusMap: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已退款',
  3: '支付失败',
}
const payStatusTagMap: Record<number, 'warning' | 'success' | 'info' | 'error' | 'default'> = {
  0: 'warning',
  1: 'success',
  2: 'info',
  3: 'error',
}
function payStatusLabel(s: number) {
  return payStatusMap[s] ?? '未知'
}
function payStatusTagType(s: number) {
  return payStatusTagMap[s] ?? 'default'
}

function getPayStatusColor(status: number): string {
  const map: Record<number, string> = {
    0: '#F59E0B',
    1: '#2D6A4F',
    2: '#3B82F6',
    3: '#EF4444',
  }
  return map[status] ?? '#94A3B8'
}

// --- Payment Method ---
const payMethodMap: Record<string, string> = {
  WECHAT: '微信支付',
  ALIPAY: '支付宝',
  CASH: '现金',
  CARD: '刷卡',
}
const payMethodTagTypeMap: Record<string, 'success' | 'info' | 'warning' | 'default'> = {
  WECHAT: 'success',
  ALIPAY: 'info',
  CASH: 'warning',
  CARD: 'default',
}
function payMethodLabel(m: string) {
  return payMethodMap[m] ?? m ?? '—'
}
function payMethodTagType(m: string) {
  return payMethodTagTypeMap[m] ?? 'default'
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '—'
}

// --- Data ---
const loading = ref(false)
const payments = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

const totalAmount = computed(() => {
  return payments.value.reduce((sum, p) => sum + (p.amount ?? 0), 0).toFixed(2)
})

const paidCount = computed(() => {
  return payments.value.filter(p => p.status === 1).length
})

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getMyPayments({ page: currentPage.value, size: pageSize })
    payments.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取支付记录失败')
  } finally {
    loading.value = false
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
  background: linear-gradient(135deg, #fdf2f0 0%, #fce8e4 50%, #f0f7f4 100%);
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
  background: #E07A5F;
  top: -80px;
  right: -60px;
}

.hero-shape-2 {
  width: 180px;
  height: 180px;
  background: #2d6a4f;
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
  background: rgba(224, 122, 95, 0.10);
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
.payments-page .page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  position: relative;
  z-index: 1;
}

/* ===== Stats ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

/* ===== Payment Cards ===== */
.payment-card {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.payment-card:hover {
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

/* Amount Highlight */
.amount-highlight {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(224, 122, 95, 0.04);
  border-radius: var(--radius-sm);
}

.amount-label {
  font-size: 14px;
  color: var(--text-secondary);
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

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .payments-page .page-container {
    padding: 24px 16px 40px;
  }
}
</style>
