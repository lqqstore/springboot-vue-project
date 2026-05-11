<template>
  <div class="repair-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修管理</span>
          <el-button type="primary" @click="handleAdd">添加报修</el-button>
        </div>
      </template>
      
      <el-table :data="repairList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="roomId" label="房间ID" width="100" />
        <el-table-column prop="description" label="描述" :show-overflow-tooltip="true" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerId" label="处理人ID" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑报修对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑报修' : '添加报修'"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="学生ID" v-if="!isEdit">
          <el-input v-model.number="form.studentId" placeholder="请输入学生ID" />
        </el-form-item>
        <el-form-item label="房间ID" v-if="!isEdit">
          <el-input v-model.number="form.roomId" placeholder="请输入房间ID" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入报修描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人ID" v-if="isEdit">
          <el-input v-model.number="form.handlerId" placeholder="请输入处理人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const form = ref<Form>({
  description: ''
})

// 获取报修列表
const getRepairList = async () => {
  try {
    const response = await repairApi.getRepairOrderList()
    repairList.value = response.data
  } catch (error) {
    ElMessage.error('获取报修列表失败')
  }
}

// 添加报修
const handleAdd = () => {
  isEdit.value = false
  currentId.value = 0
  form.value = {
    description: ''
  }
  dialogVisible.value = true
}

// 编辑报修
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

// 提交表单
const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await repairApi.updateRepairOrder(currentId.value, form.value)
      ElMessage.success('编辑报修成功')
    } else {
      await repairApi.addRepairOrder(form.value as any)
      ElMessage.success('添加报修成功')
    }
    dialogVisible.value = false
    getRepairList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑报修失败' : '添加报修失败')
  }
}

// 删除报修
const handleDelete = async (id: number) => {
  try {
    await repairApi.deleteRepairOrder(id)
    ElMessage.success('删除报修成功')
    getRepairList()
  } catch (error) {
    ElMessage.error('删除报修失败')
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

// 初始化
onMounted(() => {
  getRepairList()
})
</script>

<style scoped>
.repair-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
