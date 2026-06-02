package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.NotificationWebSocketHandler;
import com.campus.platform.dto.SendMessageRequest;
import com.campus.platform.entity.Conversation;
import com.campus.platform.entity.ConversationMember;
import com.campus.platform.entity.Message;
import com.campus.platform.enums.ConversationType;
import com.campus.platform.enums.RoleType;
import com.campus.platform.mapper.ConversationMapper;
import com.campus.platform.mapper.ConversationMemberMapper;
import com.campus.platform.mapper.MessageMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.vo.ConversationVO;
import com.campus.platform.vo.ConversationUnreadEventVO;
import com.campus.platform.vo.MessageNewEventVO;
import com.campus.platform.vo.MessageVO;
import com.campus.platform.vo.UnreadVO;
import com.campus.platform.vo.WebSocketEventVO;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper conversationMemberMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public List<ConversationVO> listConversations(Long userId) {
        List<ConversationMember> members =
            conversationMemberMapper.selectList(new LambdaQueryWrapper<ConversationMember>().eq(ConversationMember::getUserId, userId));
        return members.stream()
            .map(member -> {
                Conversation conversation = conversationMapper.selectById(member.getConversationId());
                Message lastMessage =
                    conversation.getLastMessageId() == null ? null : messageMapper.selectById(conversation.getLastMessageId());
                return ConversationVO.builder()
                    .id(conversation.getId())
                    .type(conversation.getType())
                    .title(conversation.getTitle())
                    .unreadCount(member.getUnreadCount())
                    .lastMessage(lastMessage == null ? null : toVo(lastMessage))
                    .updatedAt(conversation.getUpdatedAt())
                    .build();
            })
            .sorted(Comparator.comparing((ConversationVO item) ->
                item.getUpdatedAt() == null ? (item.getLastMessage() == null ? null : item.getLastMessage().getCreatedAt()) : item.getUpdatedAt(),
                Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());
    }

    public List<MessageVO> listMessages(Long userId, Long conversationId) {
        ensureMember(userId, conversationId);
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getId))
            .stream()
            .map(this::toVo)
            .collect(Collectors.toList());
    }

    @Transactional
    public MessageVO send(Long senderId, SendMessageRequest request) {
        ensureSenderAccess(senderId, request.getConversationId());
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setSenderId(senderId);
        message.setType(request.getType());
        message.setContent(request.getContent());
        messageMapper.insert(message);

        Conversation conversation = conversationMapper.selectById(request.getConversationId());
        conversation.setLastMessageId(message.getId());
        conversationMapper.updateById(conversation);

        List<ConversationMember> members = conversationMemberMapper.selectList(
            new LambdaQueryWrapper<ConversationMember>().eq(ConversationMember::getConversationId, request.getConversationId()));
        for (ConversationMember member : members) {
            if (!Objects.equals(member.getUserId(), senderId)) {
                member.setUnreadCount(member.getUnreadCount() + 1);
                conversationMemberMapper.updateById(member);
                pushMessageEvents(member.getUserId(), request.getConversationId(), toVo(message), member.getUnreadCount());
            }
        }
        return toVo(message);
    }

    @Transactional
    public MessageVO sendSystemMessage(Long senderId, Long recipientUserId, String content) {
        Long conversationId = ensureSystemConversation(recipientUserId);
        SendMessageRequest request = new SendMessageRequest();
        request.setConversationId(conversationId);
        request.setType("TEXT");
        request.setContent(content);
        return send(senderId, request);
    }

    @Transactional
    public MessageVO sendConversationNotice(Long conversationId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(0L);
        message.setType("TEXT");
        message.setContent(content);
        messageMapper.insert(message);

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        conversation.setLastMessageId(message.getId());
        conversationMapper.updateById(conversation);

        List<ConversationMember> members = conversationMemberMapper.selectList(
            new LambdaQueryWrapper<ConversationMember>().eq(ConversationMember::getConversationId, conversationId));
        for (ConversationMember member : members) {
            member.setUnreadCount(member.getUnreadCount() + 1);
            conversationMemberMapper.updateById(member);
            pushMessageEvents(member.getUserId(), conversationId, toVo(message), member.getUnreadCount());
        }
        return toVo(message);
    }

    @Transactional
    public void markConversationRead(Long userId, Long conversationId) {
        ConversationMember member = ensureMember(userId, conversationId);
        Conversation conversation = conversationMapper.selectById(conversationId);
        member.setUnreadCount(0);
        member.setLastReadMessageId(conversation == null ? null : conversation.getLastMessageId());
        conversationMemberMapper.updateById(member);
        pushUnreadEvent(userId, conversationId, 0);
    }

    public UnreadVO getUnread(Long userId) {
        List<ConversationMember> members =
            conversationMemberMapper.selectList(new LambdaQueryWrapper<ConversationMember>().eq(ConversationMember::getUserId, userId));
        int total = members.stream().mapToInt(item -> item.getUnreadCount() == null ? 0 : item.getUnreadCount()).sum();
        return new UnreadVO(total);
    }

    @Transactional
    public Long ensureSystemConversation(Long userId) {
        List<ConversationMember> members =
            conversationMemberMapper.selectList(new LambdaQueryWrapper<ConversationMember>().eq(ConversationMember::getUserId, userId));
        for (ConversationMember member : members) {
            Conversation conversation = conversationMapper.selectById(member.getConversationId());
            if (ConversationType.SYSTEM.name().equals(conversation.getType())) {
                return conversation.getId();
            }
        }
        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.SYSTEM.name());
        conversation.setTitle("系统通知");
        conversationMapper.insert(conversation);

        ConversationMember member = new ConversationMember();
        member.setConversationId(conversation.getId());
        member.setUserId(userId);
        member.setUnreadCount(0);
        conversationMemberMapper.insert(member);
        return conversation.getId();
    }

    private ConversationMember ensureMember(Long userId, Long conversationId) {
        ConversationMember member = conversationMemberMapper.selectOne(
            new LambdaQueryWrapper<ConversationMember>()
                .eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException("无权访问该会话");
        }
        return member;
    }

    private void ensureSenderAccess(Long userId, Long conversationId) {
        ConversationMember member = conversationMemberMapper.selectOne(
            new LambdaQueryWrapper<ConversationMember>()
                .eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId));
        if (member != null) {
            return;
        }
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        var sender = userMapper.selectById(userId);
        if (sender != null && RoleType.ADMIN.name().equals(sender.getRole()) && ConversationType.SYSTEM.name().equals(conversation.getType())) {
            return;
        }
        throw new BusinessException("无权访问该会话");
    }

    private MessageVO toVo(Message message) {
        return MessageVO.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .type(message.getType())
            .content(message.getContent())
            .createdAt(message.getCreatedAt())
            .build();
    }

    private void pushMessageEvents(Long userId, Long conversationId, MessageVO message, Integer unreadCount) {
        notificationWebSocketHandler.sendToUser(
            userId,
            new WebSocketEventVO<>("message.new", MessageNewEventVO.builder()
                .conversationId(conversationId)
                .message(message)
                .build())
        );
        pushUnreadEvent(userId, conversationId, unreadCount);
    }

    private void pushUnreadEvent(Long userId, Long conversationId, Integer unreadCount) {
        int totalUnread = getUnread(userId).getTotalUnread();
        notificationWebSocketHandler.sendToUser(
            userId,
            new WebSocketEventVO<>("conversation.unread", ConversationUnreadEventVO.builder()
                .conversationId(conversationId)
                .unreadCount(unreadCount)
                .totalUnread(totalUnread)
                .build())
        );
    }
}
