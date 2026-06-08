package com.campus.platform.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminErrandConversationReviewVO {

    private Long orderId;
    private String orderNo;
    private String orderStatus;
    private Long conversationId;
    private String conversationType;
    private String conversationTitle;
    private ErrandCounterpartyVO publisher;
    private ErrandCounterpartyVO runner;
    private List<AdminConversationMemberVO> members;
    private List<AdminMessageReviewVO> messages;
}
