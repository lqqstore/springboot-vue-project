import request, { type Result } from '@/utils/request'

interface Notice {
  id: number
  title: string
  content: string
  publishTime: string
}

interface NoticeAddRequest {
  title: string
  content: string
}

interface NoticeUpdateRequest {
  title: string
  content: string
}

export const noticeApi = {
  getNoticeList: () => {
    return request.get<Result<Notice[]>>('/notice/list')
  },
  getNoticeById: (id: number) => {
    return request.get<Result<Notice>>(`/notice/${id}`)
  },
  addNotice: (data: NoticeAddRequest) => {
    return request.post<Result<Notice>>('/notice', data)
  },
  updateNotice: (id: number, data: NoticeUpdateRequest) => {
    return request.put<Result<Notice>>(`/notice/${id}`, data)
  },
  deleteNotice: (id: number) => {
    return request.delete<Result<null>>(`/notice/${id}`)
  }
}
