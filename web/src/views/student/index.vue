<template>
  <div class="page-wrap">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="22"><UserFilled /></el-icon>
        <span>学生管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学生</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchQuery" class="search-form">
        <el-form-item label="姓名">
          <el-input
            v-model="searchQuery.name"
            placeholder="搜索姓名"
            style="width: 180px;"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="专业">
          <el-input
            v-model="searchQuery.major"
            placeholder="搜索专业"
            style="width: 180px;"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="filteredList">搜索</el-button>
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
        empty-text="暂无学生数据"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="120">
          <template #default="{ row }">
            <div class="cell-student">
              <el-avatar :size="28" :icon="UserFilled" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'M' ? 'primary' : row.gender === 'F' ? 'danger' : 'info'" effect="light" size="small">
              {{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="major" label="专业" min-width="180">
          <template #default="{ row }">
            <span class="cell-major">{{ row.major }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" :icon="Connection" @click="handleAssignDorm(row)">分配宿舍</el-button>
            <el-popconfirm
              title="确定删除该学生？"
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

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户ID" prop="userId" v-if="!isEdit">
          <el-input v-model.number="form.userId" placeholder="请输入用户ID" :prefix-icon="User" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" :prefix-icon="Edit" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%;">
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" :prefix-icon="Reading" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" :icon="Check" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配宿舍对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配宿舍"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="assignFormRef"
        :model="assignForm"
        :rules="assignRules"
        label-width="80px"
      >
        <el-form-item label="学生ID">
          <el-input v-model.number="assignForm.studentId" disabled />
        </el-form-item>
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model.number="assignForm.roomId" placeholder="请选择房间" style="width: 100%;" :prefix-icon="House">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.buildingName} - ${room.roomNumber} (${room.currentCount}/${room.capacity})`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="assignForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="assignLoading" :icon="Check" @click="handleAssignSubmit">确定</el-button>
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
  UserFilled, Plus, Search, RefreshRight, Edit, Delete, Check,
  Connection, User, Phone, Reading, House
} from '@element-plus/icons-vue'
import { studentApi } from '@/api/student'
import { getAvailableRooms, type DormRoomVO } from '@/api/dorm'

interface Student {
  id: number
  userId: number
  name: string
  gender: string
  phone: string
  major: string
}

interface Form {
  userId?: number
  name: string
  gender: string
  phone: string
  major: string
}

interface AssignForm {
  studentId: number
  roomId: number
  checkInDate: string
}

const studentList = ref<Student[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance | null>(null)
const assignFormRef = ref<FormInstance | null>(null)
const assignLoading = ref(false)
const availableRooms = ref<DormRoomVO[]>([])

const currentPage = ref(1)
const pageSize = ref(10)

const searchQuery = reactive({
  name: '',
  major: ''
})

const form = ref<Form>({
  name: '',
  gender: '',
  phone: '',
  major: ''
})

const assignForm = ref<AssignForm>({
  studentId: 0,
  roomId: 0,
  checkInDate: ''
})

const rules: FormRules = {
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { type: 'number', message: '用户ID必须是数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' },
    { min: 2, max: 50, message: '专业长度在2-50个字符之间', trigger: 'blur' }
  ]
}

const assignRules: FormRules = {
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ]
}

const displayList = computed(() => {
  let list = studentList.value
  if (searchQuery.name) {
    list = list.filter(s => s.name.includes(searchQuery.name))
  }
  if (searchQuery.major) {
    list = list.filter(s => s.major.includes(searchQuery.major))
  }
  return list
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return displayList.value.slice(start, start + pageSize.value)
})

const getStudentList = async () => {
  loading.value = true
  try {
    const response = await studentApi.getStudentList()
    studentList.value = response.data.data
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

const filteredList = () => {
  currentPage.value = 1
}

const onReset = () => {
  searchQuery.name = ''
  searchQuery.major = ''
  currentPage.value = 1
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = 0
  form.value = {
    name: '',
    gender: '',
    phone: '',
    major: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: Student) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    name: row.name,
    gender: row.gender,
    phone: row.phone,
    major: row.major
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      if (isEdit.value) {
        const response = await studentApi.updateStudent(currentId.value, form.value)
        if (response.data.code !== 0) throw new Error(response.data.msg || '编辑失败')
        ElMessage.success('编辑学生成功')
      } else {
        const response = await studentApi.addStudent(form.value as any)
        if (response.data.code !== 0) throw new Error(response.data.msg || '添加失败')
        ElMessage.success('添加学生成功')
      }
      dialogVisible.value = false
      getStudentList()
    } catch (error: any) {
      ElMessage.error(error?.message || (isEdit.value ? '编辑学生失败' : '添加学生失败'))
    } finally {
      loading.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    const response = await studentApi.deleteStudent(id)
    if (response.data.code !== 0) throw new Error(response.data.msg || '删除失败')
    ElMessage.success('删除学生成功')
    getStudentList()
  } catch (error: any) {
    ElMessage.error(error?.message || '删除学生失败')
  }
}

const loadAvailableRooms = async () => {
  try {
    const res = await getAvailableRooms()
    if (res.data.code === 0) {
      availableRooms.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载可分配房间失败', e)
  }
}

const handleAssignDorm = async (row: Student) => {
  await loadAvailableRooms()
  assignForm.value = {
    studentId: row.id,
    roomId: 0,
    checkInDate: new Date().toISOString().split('T')[0]
  }
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  if (!assignFormRef.value) return
  assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      assignLoading.value = true
      const response = await studentApi.assignDorm(assignForm.value)
      if (response.data.code !== 0) throw new Error(response.data.msg || '分配失败')
      ElMessage.success('分配宿舍成功')
      assignDialogVisible.value = false
    } catch (error: any) {
      ElMessage.error(error?.message || '分配宿舍失败')
    } finally {
      assignLoading.value = false
    }
  })
}

onMounted(() => {
  getStudentList()
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
  margin-bottom: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 16px;
}

.cell-student {
  display: flex;
  align-items: center;
  gap: 10px;
}

.cell-major {
  color: #606266;
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
