import request, { type Result } from '@/utils/request'

interface RepairOrder {
  id: number
  studentId: number
  roomId: number
  description: string
  status: number
  handlerId: number | null
}

interface RepairOrderAddRequest {
  studentId: number
  roomId: number
  description: string
}

interface RepairOrderUpdateRequest {
  description?: string
  status?: number
  handlerId?: number
}

export const repairApi = {
  getRepairOrderList: () => {
    return request.get<Result<RepairOrder[]>>('/repair/list')
  },
  getRepairOrdersByStudentId: (studentId: number) => {
    return request.get<Result<RepairOrder[]>>(`/repair/student/${studentId}`)
  },
  getRepairOrderById: (id: number) => {
    return request.get<Result<RepairOrder>>(`/repair/${id}`)
  },
  addRepairOrder: (data: RepairOrderAddRequest) => {
    return request.post<Result<RepairOrder>>('/repair', data)
  },
  updateRepairOrder: (id: number, data: RepairOrderUpdateRequest) => {
    return request.put<Result<RepairOrder>>(`/repair/${id}`, data)
  },
  deleteRepairOrder: (id: number) => {
    return request.delete<Result<null>>(`/repair/${id}`)
  }
}
