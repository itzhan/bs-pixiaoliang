<template>
  <div class="reservations-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#3B82F6"><CalendarOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的预约</h1>
        <p class="hero-subtitle">管理您的车位预约记录</p>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== Status Filter ===== -->
      <n-tabs v-model:value="currentTab" type="line" @update:value="handleTabChange">
        <n-tab v-for="tab in statusTabs" :key="tab.value" :name="tab.value">
          {{ tab.label }}
        </n-tab>
      </n-tabs>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="reservations.length === 0" description="暂无预约记录" style="margin-top: 60px" />

      <!-- ===== Reservation Cards ===== -->
      <div v-else class="card-list">
        <div
          v-for="item in reservations"
          :key="item.id"
          class="reservation-card"
        >
          <div class="card-color-bar" :style="{ background: getCardColor(item) }" />
          <div class="card-inner">
            <div class="card-header">
              <span class="order-no">
                <n-icon :size="14" style="margin-right: 4px; vertical-align: -2px;"><DocumentTextOutline /></n-icon>
                {{ item.reservationNo || item.id }}
              </span>
              <n-tag :type="getCardTagType(item)" size="small" round>
                {{ getCardTagLabel(item) }}
              </n-tag>
            </div>

            <div class="card-body">
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><LocationOutline /></n-icon>
                <span class="label">车位信息</span>
                <span class="value">{{ item.areaName || '—' }} · {{ item.spaceName || item.spaceCode || '—' }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><CalendarOutline /></n-icon>
                <span class="label">预约时间</span>
                <span class="value">{{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}</span>
              </div>
              <div class="info-row">
                <n-icon :size="15" color="#94A3B8" style="flex-shrink:0;"><CashOutline /></n-icon>
                <span class="label">预约金额</span>
                <span class="value price-text">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
              </div>
            </div>

            <div class="card-footer" v-if="canCancel(item.status) || canPay(item.paymentStatus)">
              <n-button
                v-if="canPay(item.paymentStatus)"
                type="primary"
                size="small"
                round
                :loading="payingId === item.id"
                @click="openPayModal(item)"
              >
                立即支付
              </n-button>
              <n-button
                v-if="canCancel(item.status)"
                type="error"
                size="small"
                ghost
                round
                @click="openCancelModal(item)"
              >
                取消预约
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

      <!-- Cancel Modal -->
      <n-modal v-model:show="cancelModalVisible" preset="dialog" title="取消预约" positive-text="确认取消" negative-text="再想想" @positive-click="handleCancel" :loading="cancelLoading">
        <n-input
          v-model:value="cancelReason"
          type="textarea"
          placeholder="请输入取消原因（选填）"
          :rows="3"
        />
      </n-modal>

      <!-- Pay Confirm Modal -->
      <n-modal v-model:show="payModalVisible" preset="card" title="确认支付" style="max-width: 420px">
        <div v-if="payTarget" class="pay-confirm-card">
          <div class="pay-confirm-row">
            <span class="pay-confirm-label">预约编号</span>
            <span class="pay-confirm-value">{{ payTarget.reservationNo || payTarget.id }}</span>
          </div>
          <div class="pay-confirm-row">
            <span class="pay-confirm-label">车位信息</span>
            <span class="pay-confirm-value">{{ payTarget.areaName || '—' }} · {{ payTarget.spaceName || payTarget.spaceCode || '—' }}</span>
          </div>
          <div class="pay-confirm-row">
            <span class="pay-confirm-label">预约时间</span>
            <span class="pay-confirm-value">{{ formatTime(payTarget.startTime) }} ~ {{ formatTime(payTarget.endTime) }}</span>
          </div>
          <div class="pay-confirm-row pay-amount-row">
            <span class="pay-confirm-label">支付金额</span>
            <span class="pay-confirm-value pay-confirm-price">¥{{ (payTarget.amount ?? 0).toFixed(2) }}</span>
          </div>
        </div>
        <template #action>
          <div style="display: flex; justify-content: flex-end; gap: 10px;">
            <n-button @click="payModalVisible = false">取消</n-button>
            <n-button type="primary" :loading="payingId === payTarget?.id" @click="confirmPay">确认支付</n-button>
          </div>
        </template>
      </n-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage, NIcon } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyReservations, cancelReservation, payReservation } from '@/api/reservation'
import {
  CalendarOutline,
  LocationOutline,
  CashOutline,
  DocumentTextOutline,
} from '@vicons/ionicons5'

const message = useMessage()

// --- Tab Config (全部 / 待支付 / 已支付 / 已取消) ---
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'pay:0' },
  { label: '已支付', value: 'pay:1' },
  { label: '已取消', value: 'status:2' },
]

// --- Payment Status ---
const payStatusMap: Record<number, string> = {
  0: '待支付',
  1: '已支付',
}

const payStatusTagMap: Record<number, 'warning' | 'success' | 'default'> = {
  0: 'warning',
  1: 'success',
}

function payStatusLabel(paymentStatus: number) {
  return payStatusMap[paymentStatus] ?? '未知'
}

function payStatusTagType(paymentStatus: number) {
  return payStatusTagMap[paymentStatus] ?? 'default'
}

function canCancel(status: number) {
  return status === 0 // 待使用
}

function canPay(paymentStatus: number) {
  return paymentStatus === 0 // 待支付
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
}

function getPayStatusColor(paymentStatus: number): string {
  const map: Record<number, string> = {
    0: '#F59E0B',
    1: '#2D6A4F',
  }
  return map[paymentStatus] ?? '#94A3B8'
}

function getCardColor(item: any): string {
  if (item.status === 2) return '#94A3B8'
  return getPayStatusColor(item.paymentStatus)
}

function getCardTagType(item: any) {
  if (item.status === 2) return 'default'
  return payStatusTagType(item.paymentStatus)
}

function getCardTagLabel(item: any) {
  if (item.status === 2) return '已取消'
  return payStatusLabel(item.paymentStatus)
}

// --- Data ---
const loading = ref(false)
const reservations = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const currentTab = ref('')
const payingId = ref<number | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize }
    if (currentTab.value.startsWith('pay:')) {
      params.paymentStatus = currentTab.value.split(':')[1]
    } else if (currentTab.value.startsWith('status:')) {
      params.status = currentTab.value.split(':')[1]
    }
    const res: any = await getMyReservations(params)
    reservations.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取预约列表失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  fetchList()
}

// --- Pay ---
const payModalVisible = ref(false)
const payTarget = ref<any>(null)

function openPayModal(item: any) {
  payTarget.value = item
  payModalVisible.value = true
}

async function confirmPay() {
  if (!payTarget.value) return
  payingId.value = payTarget.value.id
  try {
    await payReservation(payTarget.value.id)
    message.success('支付成功')
    payModalVisible.value = false
    fetchList()
  } catch (e: any) {
    message.error(e.message || '支付失败')
  } finally {
    payingId.value = null
  }
}

// --- Cancel ---
const cancelModalVisible = ref(false)
const cancelLoading = ref(false)
const cancelReason = ref('')
const cancelTarget = ref<any>(null)

function openCancelModal(item: any) {
  cancelTarget.value = item
  cancelReason.value = ''
  cancelModalVisible.value = true
}

async function handleCancel() {
  if (!cancelTarget.value) return
  cancelLoading.value = true
  try {
    await cancelReservation(cancelTarget.value.id, cancelReason.value || undefined)
    message.success('预约已取消')
    cancelModalVisible.value = false
    fetchList()
  } catch (e: any) {
    message.error(e.message || '取消失败')
  } finally {
    cancelLoading.value = false
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
  background: linear-gradient(135deg, #eef2ff 0%, #e8f0f6 50%, #f0f7f4 100%);
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
  background: #3B82F6;
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
  background: rgba(59, 130, 246, 0.08);
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
.reservations-page .page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  position: relative;
  z-index: 1;
}

/* ===== Cards ===== */
.reservation-card {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.reservation-card:hover {
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
  gap: 10px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border-color);
}

/* ===== Pay Confirm Modal ===== */
.pay-confirm-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pay-confirm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pay-confirm-label {
  font-size: 14px;
  color: #94a3b8;
  min-width: 64px;
}

.pay-confirm-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  text-align: right;
}

.pay-amount-row {
  padding-top: 14px;
  border-top: 1px dashed #e2e8f0;
}

.pay-confirm-price {
  font-size: 22px;
  font-weight: 700;
  color: #e07a5f;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .reservations-page .page-container {
    padding: 24px 16px 40px;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}
</style>
