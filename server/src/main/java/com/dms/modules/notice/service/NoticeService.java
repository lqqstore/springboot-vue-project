package com.dms.modules.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dms.modules.notice.entity.Notice;
import com.dms.modules.notice.dto.NoticeAddRequestDTO;
import com.dms.modules.notice.dto.NoticeUpdateRequestDTO;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    
    List<Notice> getNoticeList();
    
    Notice getNoticeById(Long id);
    
    Notice addNotice(NoticeAddRequestDTO dto);
    
    Notice updateNotice(Long id, NoticeUpdateRequestDTO dto);
    
    void deleteNotice(Long id);
}
