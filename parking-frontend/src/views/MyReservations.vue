<template>
  <div class="page-container">
    <h2 class="section-title">我的预约</h2>

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
    <n-empty v-else-if="reservations.length === 0" description="暂无预约记录" style="margin-top: 60px" />

    <!-- Reservation Cards -->
    <div v-else class="card-list">
      <n-card
        v-for="item in reservations"
        :key="item.id"
        class="reservation-card card-hover"
        :bordered="false"
      >
        <div class="card-header">
          <span class="order-no">预约编号：{{ item.reservationNo || item.id }}</span>
          <n-tag :type="statusTagType(item.status)" size="small" round>
            {{ statusLabel(item.status) }}
          </n-tag>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="label">车位信息</span>
            <span class="value">{{ item.areaName || '—' }} · {{ item.spaceName || item.spaceCode || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="label">预约时间</span>
            <span class="value">{{ formatTime(item.startTime) }} ~ {{ formatTime(item.endTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">预约金额</span>
            <span class="value price-text">¥{{ (item.amount ?? 0).toFixed(2) }}</span>
          </div>
        </div>

        <div class="card-footer" v-if="canCancel(item.status)">
          <n-button
            type="error"
            size="small"
            ghost
            @click="openCancelModal(item)"
          >
            取消预约
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

    <!-- Cancel Modal -->
    <n-modal v-model:show="cancelModalVisible" preset="dialog" title="取消预约" positive-text="确认取消" negative-text="再想想" @positive-click="handleCancel" :loading="cancelLoading">
      <n-input
        v-model:value="cancelReason"
        type="textarea"
        placeholder="请输入取消原因（选填）"
        :rows="3"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyReservations, cancelReservation } from '@/api/reservation'

const message = useMessage()

// --- Status Config ---
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待确认', value: '0' },
  { label: '已确认', value: '1' },
  { label: '已取消', value: '2' },
  { label: '已过期', value: '3' },
  { label: '已完成', value: '4' },
]

const statusMap: Record<number, string> = {
  0: '待确认',
  1: '已确认',
  2: '已取消',
  3: '已过期',
  4: '已完成',
}

const statusTagMap: Record<number, 'warning' | 'success' | 'default' | 'info' | 'error'> = {
  0: 'warning',
  1: 'success',
  2: 'default',
  3: 'error',
  4: 'info',
}

function statusLabel(status: number) {
  return statusMap[status] ?? '未知'
}

function statusTagType(status: number) {
  return statusTagMap[status] ?? 'default'
}

function canCancel(status: number) {
  return status === 0 || status === 1
}

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
}

// --- Data ---
const loading = ref(false)
const reservations = ref<any[]>([])
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
    const res: any = await getMyReservations(params)
    reservations.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取预约列表失败')
  } finally {
    loading.value = false
  }
}

function handleStatusChange() {
  currentPage.value = 1
  fetchList()
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

.reservation-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
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

@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
}
</style>
