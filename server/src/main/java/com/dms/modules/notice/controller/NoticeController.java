package com.dms.modules.notice.controller;

import com.dms.common.Result;
import com.dms.modules.notice.dto.NoticeAddRequestDTO;
import com.dms.modules.notice.dto.NoticeUpdateRequestDTO;
import com.dms.modules.notice.entity.Notice;
import com.dms.modules.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
    
    private final NoticeService noticeService;
    
    @GetMapping("/list")
    public Result<List<Notice>> getNoticeList() {
        List<Notice> notices = noticeService.getNoticeList();
        return Result.success(notices);
    }
    
    @GetMapping("/{id}")
    public Result<Notice> getNoticeById(@PathVariable Long id) {
        Notice notice = noticeService.getNoticeById(id);
        return Result.success(notice);
    }
    
    @PostMapping
    public Result<Notice> addNotice(@RequestBody @Valid NoticeAddRequestDTO dto) {
        Notice notice = noticeService.addNotice(dto);
        return Result.success(notice);
    }
    
    @PutMapping("/{id}")
    public Result<Notice> updateNotice(@PathVariable Long id, @RequestBody @Valid NoticeUpdateRequestDTO dto) {
        Notice notice = noticeService.updateNotice(id, dto);
        return Result.success(notice);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }
}
