<template>
  <div class="student-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="handleAdd">添加学生</el-button>
        </div>
      </template>
      
      <el-table :data="studentList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="major" label="专业" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleAssignDorm(row)">分配宿舍</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户ID" prop="userId" v-if="!isEdit">
          <el-input v-model.number="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分配宿舍对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配宿舍"
      width="500px"
    >
      <el-form
        ref="assignFormRef"
        :model="assignForm"
        :rules="assignRules"
        label-width="80px"
      >
        <el-form-item label="学生ID" disabled>
          <el-input v-model.number="assignForm.studentId" />
        </el-form-item>
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model.number="assignForm.roomId" placeholder="请选择房间" style="width: 100%;">
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
        <span class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="assignLoading"
            @click="handleAssignSubmit"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
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
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance | null>(null)
const assignFormRef = ref<FormInstance | null>(null)
const loading = ref(false)
const assignLoading = ref(false)
const availableRooms = ref<DormRoomVO[]>([])

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

// 表单验证规则
const rules = ref<FormRules>({
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
})

// 宿舍分配表单验证规则
const assignRules = ref<FormRules>({
  roomId: [
    { required: true, message: '请输入房间ID', trigger: 'blur' },
    { type: 'number', message: '房间ID必须是数字', trigger: 'blur' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ]
})

// 获取学生列表
const getStudentList = async () => {
  try {
    const response = await studentApi.getStudentList()
    studentList.value = response.data.data
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  }
}

// 添加学生
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

// 编辑学生
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

// 提交表单
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
      ElMessage.error(isEdit.value ? '编辑学生失败' : '添加学生失败')
    } finally {
      loading.value = false
    }
  })
}

// 删除学生
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await studentApi.deleteStudent(id)
    if (response.data.code !== 0) throw new Error(response.data.msg || '删除失败')
    ElMessage.success('删除学生成功')
    getStudentList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除学生失败')
    }
  }
}

// 加载可分配房间
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

// 分配宿舍
const handleAssignDorm = async (row: Student) => {
  await loadAvailableRooms()
  assignForm.value = {
    studentId: row.id,
    roomId: 0,
    checkInDate: new Date().toISOString().split('T')[0]
  }
  assignDialogVisible.value = true
}

// 提交宿舍分配
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
      ElMessage.error('分配宿舍失败')
    } finally {
      assignLoading.value = false
    }
  })
}

// 初始化
onMounted(() => {
  getStudentList()
})
</script>

<style scoped>
.student-container {
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
