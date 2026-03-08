<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SaveOutlined } from '@ant-design/icons-vue'
import { getAllSettings, updateSettings } from '@/api/setting'

/* ---------- Form state ---------- */
const loading = ref(false)
const saving = ref(false)

const formState = reactive({
  // 停车设置
  'parking.max_reservation_hours': '',
  'parking.max_vehicles_per_user': '',
  'parking.default_free_minutes': '',
  // 系统信息
  'system.name': '',
  'system.version': '',
  'system.contact_phone': '',
  'system.contact_email': '',
  'system.address': '',
  // 支付设置
  'payment.enable_wechat': false,
  'payment.enable_alipay': false,
})

/* ---------- Load settings ---------- */
async function loadSettings() {
  loading.value = true
  try {
    const res = await getAllSettings()
    const settings: any[] = Array.isArray(res.data) ? res.data : []
    settings.forEach((item: any) => {
      const key = item.settingKey || item.key
      const val = item.settingValue || item.value
      if (key in formState) {
        if (key === 'payment.enable_wechat' || key === 'payment.enable_alipay') {
          ;(formState as any)[key] = val === 'true' || val === '1' || val === true
        } else {
          ;(formState as any)[key] = val ?? ''
        }
      }
    })
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}

/* ---------- Save settings ---------- */
async function handleSave() {
  saving.value = true
  try {
    const payload: Record<string, string> = {}
    for (const [key, val] of Object.entries(formState)) {
      if (typeof val === 'boolean') {
        payload[key] = val ? 'true' : 'false'
      } else {
        payload[key] = String(val)
      }
    }
    await updateSettings(payload)
    message.success('保存成功')
  } catch {
    /* handled by interceptor */
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <a-spin :spinning="loading">
      <!-- 停车设置 -->
      <a-card title="停车设置" :bordered="false" style="margin-bottom: 24px">
        <a-form layout="vertical" :model="formState">
          <a-row :gutter="24">
            <a-col :span="8">
              <a-form-item label="最大预约时长（小时）">
                <a-input-number
                  v-model:value="formState['parking.max_reservation_hours']"
                  :min="1"
                  :max="72"
                  style="width: 100%"
                  placeholder="例: 24"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="每用户最大车辆数">
                <a-input-number
                  v-model:value="formState['parking.max_vehicles_per_user']"
                  :min="1"
                  :max="20"
                  style="width: 100%"
                  placeholder="例: 5"
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="默认免费停车时间（分钟）">
                <a-input-number
                  v-model:value="formState['parking.default_free_minutes']"
                  :min="0"
                  :max="120"
                  style="width: 100%"
                  placeholder="例: 15"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <!-- 系统信息 -->
      <a-card title="系统信息" :bordered="false" style="margin-bottom: 24px">
        <a-form layout="vertical" :model="formState">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="系统名称">
                <a-input
                  v-model:value="formState['system.name']"
                  placeholder="例: 智能停车场管理系统"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="系统版本">
                <a-input
                  v-model:value="formState['system.version']"
                  placeholder="例: 1.0.0"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="联系电话">
                <a-input
                  v-model:value="formState['system.contact_phone']"
                  placeholder="例: 400-123-4567"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="联系邮箱">
                <a-input
                  v-model:value="formState['system.contact_email']"
                  placeholder="例: admin@parking.com"
                />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col :span="24">
              <a-form-item label="系统地址">
                <a-input
                  v-model:value="formState['system.address']"
                  placeholder="例: 北京市朝阳区xxx路xxx号"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <!-- 支付设置 -->
      <a-card title="支付设置" :bordered="false" style="margin-bottom: 24px">
        <a-form layout="vertical" :model="formState">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="微信支付">
                <a-switch
                  v-model:checked="formState['payment.enable_wechat']"
                  checked-children="开启"
                  un-checked-children="关闭"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="支付宝支付">
                <a-switch
                  v-model:checked="formState['payment.enable_alipay']"
                  checked-children="开启"
                  un-checked-children="关闭"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <!-- Save button -->
      <div style="text-align: right; padding-bottom: 16px">
        <a-button type="primary" size="large" :loading="saving" @click="handleSave">
          <template #icon><SaveOutlined /></template>
          保存设置
        </a-button>
      </div>
    </a-spin>
  </div>
</template>
