import request, { type Result } from '@/utils/request'

interface RepairOrder {
  id: number
  studentId: number | null
  studentName: string
  reporterName: string
  roomId: number
  buildingName: string
  roomNumber: string
  description: string
  status: number
  handlerName: string
}

interface RepairOrderAddRequest {
  reporterName: string
  roomId: number
  description: string
}

interface RepairOrderUpdateRequest {
  description?: string
  status?: number
  handlerName?: string
}

export const repairApi = {
  getRepairOrderList: () => {
    return request.get<Result<RepairOrder[]>>('/repair/list')
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
