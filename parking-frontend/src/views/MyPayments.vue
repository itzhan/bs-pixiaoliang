<template>
  <div class="page-container">
    <h2 class="section-title">我的支付记录</h2>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrapper">
      <n-spin size="large" />
    </div>

    <!-- Empty -->
    <n-empty v-else-if="payments.length === 0" description="暂无支付记录" style="margin-top: 60px" />

    <!-- Payment Cards -->
    <div v-else class="card-list">
      <n-card
        v-for="item in payments"
        :key="item.id"
        class="payment-card card-hover"
        :bordered="false"
      >
        <div class="card-header">
          <span class="payment-no">支付单号：{{ item.paymentNo || item.id }}</span>
          <n-tag :type="payStatusTagType(item.status)" size="small" round>
            {{ payStatusLabel(item.status) }}
          </n-tag>
        </div>

        <div class="card-body">
          <div class="info-row amount-row">
            <span class="label">支付金额</span>
            <span class="price-text-large">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付方式</span>
            <n-tag :type="payMethodTagType(item.paymentMethod)" size="small">
              {{ payMethodLabel(item.paymentMethod) }}
            </n-tag>
          </div>
          <div class="info-row">
            <span class="label">支付时间</span>
            <span class="value">{{ formatTime(item.payTime || item.createTime) }}</span>
          </div>
        </div>
      </n-card>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyPayments } from '@/api/payment'

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
.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.card-list {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.payment-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.payment-no {
  font-size: 13px;
  color: var(--text-secondary);
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-row .label {
  color: var(--text-tertiary);
  font-size: 13px;
  min-width: 70px;
  flex-shrink: 0;
}

.info-row .value {
  color: var(--text-primary);
  font-size: 14px;
}

.amount-row {
  padding: 8px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
