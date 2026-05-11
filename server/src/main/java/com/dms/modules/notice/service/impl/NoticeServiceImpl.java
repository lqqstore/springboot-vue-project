package com.dms.modules.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dms.modules.notice.dto.NoticeAddRequestDTO;
import com.dms.modules.notice.dto.NoticeUpdateRequestDTO;
import com.dms.modules.notice.entity.Notice;
import com.dms.modules.notice.mapper.NoticeMapper;
import com.dms.modules.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
    
    private final NoticeMapper noticeMapper;
    
    @Override
    public List<Notice> getNoticeList() {
        return noticeMapper.selectList(null);
    }
    
    @Override
    public Notice getNoticeById(Long id) {
        return noticeMapper.selectById(id);
    }
    
    @Override
    public Notice addNotice(NoticeAddRequestDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublishTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return notice;
    }
    
    @Override
    public Notice updateNotice(Long id, NoticeUpdateRequestDTO dto) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new IllegalArgumentException("Notice not found");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        noticeMapper.updateById(notice);
        return notice;
    }
    
    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }
}
