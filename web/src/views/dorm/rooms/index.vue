<template>
  <el-container>
    <el-main>
      <el-card shadow="never">
        <div class="toolbar">
          <el-form :inline="true" :model="query" class="search-form">
            <el-form-item label="楼栋">
              <el-select
                v-model="query.buildingId"
                placeholder="全部楼栋"
                style="width: 180px;"
                clearable
              >
                <el-option
                  v-for="b in buildings"
                  :key="b.id"
                  :label="b.name"
                  :value="b.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="房间号">
              <el-input
                v-model="query.roomNumber"
                placeholder="如 301"
                style="width: 180px;"
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="onSearch">搜索</el-button>
              <el-button @click="onReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div style="margin: 12px 0;">
          <el-button type="primary" @click="openAdd">新增房间</el-button>
        </div>

        <el-table :data="tableData" border stripe style="width: 100%;">
          <el-table-column prop="roomNumber" label="房间号" width="140" />
          <el-table-column label="楼栋" width="180">
            <template #default="{ row }">
              {{ buildingName(row.buildingId) }}
            </template>
          </el-table-column>
          <el-table-column prop="capacity" label="容量" width="120" />
          <el-table-column prop="currentCount" label="当前入住数" width="150" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确定删除该房间？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="onDelete(row.id)"
              >
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :total="total"
            :page-size="query.size"
            :current-page="query.current"
            @current-change="onPageChange"
          />
        </div>
      </el-card>

      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="520px"
        destroy-on-close
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="90px"
          size="large"
        >
          <el-form-item label="楼栋" prop="buildingId">
            <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%;">
              <el-option
                v-for="b in buildings"
                :key="b.id"
                :label="b.name"
                :value="b.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="房间号" prop="roomNumber">
            <el-input v-model="form.roomNumber" placeholder="如 301" />
          </el-form-item>

          <el-form-item label="容量" prop="capacity">
            <el-input-number v-model="form.capacity" :min="1" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="当前入住数" prop="currentCount">
            <el-input-number v-model="form.currentCount" :min="0" style="width: 100%;" />
          </el-form-item>
        </el-form>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="onSave">
              保存
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { DormBuilding, DormRoom, PageResult } from '@/api/dorm'
import { createRoom, deleteRoom, getBuildingsList, getRoomsPage, updateRoom } from '@/api/dorm'

type RoomForm = {
  id?: number
  buildingId: number | null
  roomNumber: string
  capacity: number
  currentCount: number
}

const buildings = ref<DormBuilding[]>([])
const tableData = ref<DormRoom[]>([])

const dialogVisible = ref(false)
const dialogTitle = computed(() => (form.value.id ? '编辑房间' : '新增房间'))
const saving = ref(false)

const total = ref(0)

const query = reactive({
  current: 1,
  size: 10,
  buildingId: undefined as number | undefined,
  roomNumber: '' as string
})

const formRef = ref<FormInstance | null>(null)
const form = ref<RoomForm>({
  id: undefined,
  buildingId: null,
  roomNumber: '',
  capacity: 1,
  currentCount: 0
})

const rules = reactive<FormRules>({
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'change' }],
  currentCount: [{ required: true, message: '请输入当前入住数', trigger: 'change' }]
})

const buildingName = (buildingId: number) => {
  return buildings.value.find((b) => b.id === buildingId)?.name || '-'
}

const loadBuildings = async () => {
  const res = await getBuildingsList()
  const body = res.data
  if (body.code !== 0) throw new Error(body.msg || '加载楼栋失败')
  buildings.value = body.data || []
}

const loadRooms = async () => {
  const res = await getRoomsPage({
    current: query.current,
    size: query.size,
    buildingId: query.buildingId,
    roomNumber: query.roomNumber || undefined
  })
  const body = res.data
  if (body.code !== 0) {
    throw new Error(body.msg || '加载房间失败')
  }
  const page = body.data as PageResult<DormRoom>
  tableData.value = page.records || []
  total.value = page.total || 0
}

const onSearch = async () => {
  query.current = 1
  try {
    await loadRooms()
  } catch (e: any) {
    ElMessage.error(e?.message || '搜索失败')
  }
}

const onReset = () => {
  query.buildingId = undefined
  query.roomNumber = ''
  query.current = 1
  void onSearch()
}

const onPageChange = (page: number) => {
  query.current = page
  void loadRooms().catch((e: any) => ElMessage.error(e?.message || '分页失败'))
}

const openAdd = () => {
  form.value = {
    id: undefined,
    buildingId: null,
    roomNumber: '',
    capacity: 1,
    currentCount: 0
  }
  dialogVisible.value = true
}

const openEdit = (row: DormRoom) => {
  form.value = {
    id: row.id,
    buildingId: row.buildingId,
    roomNumber: row.roomNumber,
    capacity: row.capacity,
    currentCount: row.currentCount
  }
  dialogVisible.value = true
}

const onDelete = (id: number) => {
  return deleteRoom(id)
    .then((res) => {
      const body = res.data
      if (body.code !== 0) throw new Error(body.msg || '删除失败')
      ElMessage.success('删除成功')
      void loadRooms()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '删除失败')
    })
}

const onSave = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saving.value = true
      const payload = {
        buildingId: form.value.buildingId as number,
        roomNumber: form.value.roomNumber.trim(),
        capacity: form.value.capacity,
        currentCount: form.value.currentCount
      }
      if (form.value.id) {
        const res = await updateRoom(form.value.id, payload)
        const body = res.data
        if (body.code !== 0) throw new Error(body.msg || '更新失败')
        ElMessage.success('更新成功')
      } else {
        const res = await createRoom(payload)
        const body = res.data
        if (body.code !== 0) throw new Error(body.msg || '新增失败')
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadRooms()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  try {
    await loadBuildings()
    await loadRooms()
  } catch (e: any) {
    ElMessage.error(e?.message || '初始化失败')
  }
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 8px;
}

.search-form {
  margin-bottom: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

