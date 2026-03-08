<template>
  <div class="page-container">
    <h2 class="section-title">我的停车记录</h2>

    <!-- Status Filter -->
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

    <!-- Order Cards -->
    <div v-else class="card-list">
      <n-card
        v-for="item in orders"
        :key="item.id"
        class="order-card card-hover"
        :bordered="false"
      >
        <div class="card-header">
          <span class="order-no">订单号：{{ item.orderNo || item.id }}</span>
          <n-space :size="8">
            <n-tag :type="orderStatusTagType(item.status)" size="small" round>
              {{ orderStatusLabel(item.status) }}
            </n-tag>
            <n-tag :type="payStatusTagType(item.payStatus)" size="small" round>
              {{ payStatusLabel(item.payStatus) }}
            </n-tag>
          </n-space>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">车牌号</span>
            <span class="value plate-number">{{ item.plateNumber || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="label">入场时间</span>
            <span class="value">{{ formatTime(item.entryTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">出场时间</span>
            <span class="value">{{ formatTime(item.exitTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">停车时长</span>
            <span class="value">{{ item.duration || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="label">停车费用</span>
            <span class="value price-text">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
          </div>
        </div>

        <div class="card-footer" v-if="canPay(item)">
          <n-button type="primary" size="small" @click="openPayModal(item)">
            去支付
          </n-button>
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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyOrders } from '@/api/order'
import { createPayment } from '@/api/payment'

const message = useMessage()

// --- Status Config ---
const statusTabs = [
  { label: '全部', value: '' },
  { label: '停车中', value: '0' },
  { label: '已完成', value: '1' },
  { label: '异常', value: '2' },
]

const orderStatusMap: Record<number, string> = {
  0: '停车中',
  1: '已完成',
  2: '异常',
}
const orderStatusTagMap: Record<number, 'warning' | 'success' | 'error' | 'default'> = {
  0: 'warning',
  1: 'success',
  2: 'error',
}

const payStatusMap: Record<number, string> = {
  0: '未支付',
  1: '已支付',
  2: '退款中',
  3: '已退款',
}
const payStatusTagMap: Record<number, 'warning' | 'success' | 'info' | 'default'> = {
  0: 'warning',
  1: 'success',
  2: 'info',
  3: 'default',
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
  return item.status === 1 && item.payStatus === 0
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
}

// --- Data ---
const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const currentStatus = ref('')

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

.order-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 8px;
}

.order-no {
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

.plate-number {
  font-weight: 600;
  letter-spacing: 1px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

/* Payment Modal */
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

@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}
</style>
