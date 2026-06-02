package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerificationVO {

    private Long id;
    private String realName;
    private String idCardNo;
    private Long frontFileId;
    private String frontFileUrl;
    private Long backFileId;
    private String backFileUrl;
    private String status;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
}
