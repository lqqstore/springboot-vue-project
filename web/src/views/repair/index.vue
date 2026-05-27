<template>
  <div class="page-wrap">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="22"><Tools /></el-icon>
        <span>报修管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加报修</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchQuery" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchQuery.status" placeholder="全部状态" style="width: 150px;" clearable>
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="currentPage = 1">搜索</el-button>
          <el-button :icon="RefreshRight" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="paginatedList"
        border
        stripe
        style="width: 100%;"
        empty-text="暂无报修数据"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="roomId" label="房间ID" width="100" />
        <el-table-column prop="description" label="报修描述" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="cell-desc">
              <el-icon color="#909399"><Document /></el-icon>
              <span>{{ row.description }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light" round>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerId" label="处理人ID" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'cell-empty': !row.handlerId }">{{ row.handlerId || '未分配' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该报修单？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next"
          :total="displayList.length"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </el-card>

    <!-- 添加/编辑报修对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑报修' : '添加报修'"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="学生ID" prop="studentId" v-if="!isEdit">
          <el-input v-model.number="form.studentId" placeholder="请输入学生ID" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="房间ID" prop="roomId" v-if="!isEdit">
          <el-input v-model.number="form.roomId" placeholder="请输入房间ID" :prefix-icon="House" />
        </el-form-item>
        <el-form-item label="报修描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请详细描述报修内容"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人ID" v-if="isEdit">
          <el-input v-model.number="form.handlerId" placeholder="请输入处理人ID" :prefix-icon="Avatar" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" :icon="Check" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Tools, Plus, Search, RefreshRight, Edit, Delete, Check,
  User, House, Avatar, Document
} from '@element-plus/icons-vue'
import { repairApi } from '@/api/repair'

interface RepairOrder {
  id: number
  studentId: number
  roomId: number
  description: string
  status: number
  handlerId: number | null
}

interface Form {
  studentId?: number
  roomId?: number
  description: string
  status?: number
  handlerId?: number
}

const repairList = ref<RepairOrder[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance | null>(null)

const currentPage = ref(1)
const pageSize = ref(10)

const searchQuery = reactive({
  status: undefined as number | undefined
})

const form = ref<Form>({
  description: ''
})

const formRules: FormRules = {
  studentId: [
    { required: true, message: '请输入学生ID', trigger: 'blur' },
    { type: 'number', message: '学生ID必须是数字', trigger: 'blur' }
  ],
  roomId: [
    { required: true, message: '请输入房间ID', trigger: 'blur' },
    { type: 'number', message: '房间ID必须是数字', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入报修描述', trigger: 'blur' },
    { min: 2, max: 500, message: '描述长度在2-500个字符之间', trigger: 'blur' }
  ]
}

const displayList = computed(() => {
  if (searchQuery.status !== undefined && searchQuery.status !== null) {
    return repairList.value.filter(r => r.status === searchQuery.status)
  }
  return repairList.value
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return displayList.value.slice(start, start + pageSize.value)
})

const getRepairList = async () => {
  loading.value = true
  try {
    const res = await repairApi.getRepairOrderList()
    if (res.data.code === 0) {
      repairList.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '获取报修列表失败')
    }
  } catch (error) {
    ElMessage.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  searchQuery.status = undefined
  currentPage.value = 1
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = 0
  form.value = {
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: RepairOrder) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    description: row.description,
    status: row.status,
    handlerId: row.handlerId || undefined
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saving.value = true
      if (isEdit.value) {
        const res = await repairApi.updateRepairOrder(currentId.value, {
          description: form.value.description,
          status: form.value.status,
          handlerId: form.value.handlerId
        })
        if (res.data.code !== 0) throw new Error(res.data.msg || '编辑失败')
        ElMessage.success('编辑报修成功')
      } else {
        const res = await repairApi.addRepairOrder({
          studentId: form.value.studentId!,
          roomId: form.value.roomId!,
          description: form.value.description
        })
        if (res.data.code !== 0) throw new Error(res.data.msg || '添加失败')
        ElMessage.success('添加报修成功')
      }
      dialogVisible.value = false
      getRepairList()
    } catch (error: any) {
      const msg = error?.response?.data?.msg || error?.message
      ElMessage.error(msg || (isEdit.value ? '编辑报修失败' : '添加报修失败'))
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    const res = await repairApi.deleteRepairOrder(id)
    if (res.data.code !== 0) throw new Error(res.data.msg || '删除失败')
    ElMessage.success('删除报修成功')
    getRepairList()
  } catch (error: any) {
    const msg = error?.response?.data?.msg || error?.message
    ElMessage.error(msg || '删除报修失败')
  }
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status: number): 'info' | 'warning' | 'success' | 'danger' => {
  const typeMap: Record<number, 'info' | 'warning' | 'success' | 'danger'> = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  getRepairList()
})
</script>

<style scoped>
.page-wrap {
  max-width: 1300px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.search-card {
  margin-bottom: 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.cell-desc {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.cell-empty {
  color: var(--app-text-placeholder);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
