package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.SendMessageRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.MessageService;
import com.campus.platform.vo.ConversationVO;
import com.campus.platform.vo.MessageVO;
import com.campus.platform.vo.UnreadVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationVO>> conversations() {
        return ApiResponse.success(messageService.listConversations(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<MessageVO>> messages(@PathVariable("id") Long conversationId) {
        return ApiResponse.success(messageService.listMessages(SecurityUtils.getCurrentUserId(), conversationId));
    }

    @PostMapping("/messages/send")
    public ApiResponse<MessageVO> send(@Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.success(messageService.send(SecurityUtils.getCurrentUserId(), request));
    }

    @PostMapping("/conversations/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") Long conversationId) {
        messageService.markConversationRead(SecurityUtils.getCurrentUserId(), conversationId);
        return ApiResponse.success("success", null);
    }

    @GetMapping("/messages/unread")
    public ApiResponse<UnreadVO> unread() {
        return ApiResponse.success(messageService.getUnread(SecurityUtils.getCurrentUserId()));
    }
}
