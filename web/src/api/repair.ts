import request from '@/utils/request'

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
    return request.get<RepairOrder[]>('/repair/list')
  },
  getRepairOrdersByStudentId: (studentId: number) => {
    return request.get<RepairOrder[]>(`/repair/student/${studentId}`)
  },
  getRepairOrderById: (id: number) => {
    return request.get<RepairOrder>(`/repair/${id}`)
  },
  addRepairOrder: (data: RepairOrderAddRequest) => {
    return request.post<RepairOrder>('/repair', data)
  },
  updateRepairOrder: (id: number, data: RepairOrderUpdateRequest) => {
    return request.put<RepairOrder>(`/repair/${id}`, data)
  },
  deleteRepairOrder: (id: number) => {
    return request.delete(`/repair/${id}`)
  }
}
