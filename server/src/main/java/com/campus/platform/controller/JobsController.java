package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.JobsApplicationCreateRequest;
import com.campus.platform.dto.JobsPostCreateRequest;
import com.campus.platform.dto.JobsPostVisibilityRequest;
import com.campus.platform.dto.JobsResumeSaveRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.JobsService;
import com.campus.platform.vo.JobsMobileApplicationVO;
import com.campus.platform.vo.JobsConversationVO;
import com.campus.platform.vo.JobsMobilePostVO;
import com.campus.platform.vo.JobsResumeVO;
import com.campus.platform.vo.JobsReviewProgressVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobsController {

    private final JobsService jobsService;

    @GetMapping("/posts")
    public ApiResponse<List<JobsMobilePostVO>> posts(@RequestParam(defaultValue = "") String keyword,
                                                     @RequestParam(defaultValue = "") String category) {
        return ApiResponse.success(jobsService.listVisiblePosts(keyword, category, SecurityUtils.getCurrentUserIdOrNull()));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<JobsMobilePostVO> postDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsService.detail(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/posts")
    public ApiResponse<JobsMobilePostVO> createPost(@Valid @RequestBody JobsPostCreateRequest request) {
        return ApiResponse.success(jobsService.createPost(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/posts/{id}")
    public ApiResponse<JobsMobilePostVO> updatePost(@PathVariable("id") Long id,
                                                    @Valid @RequestBody JobsPostCreateRequest request) {
        return ApiResponse.success(jobsService.updatePost(SecurityUtils.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable("id") Long id) {
        jobsService.deletePost(SecurityUtils.getCurrentUserId(), id);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/posts/{id}/visibility")
    public ApiResponse<JobsMobilePostVO> updateVisibility(@PathVariable("id") Long id,
                                                          @Valid @RequestBody JobsPostVisibilityRequest request) {
        return ApiResponse.success(jobsService.updateVisibility(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/posts/mine")
    public ApiResponse<List<JobsMobilePostVO>> myPosts() {
        return ApiResponse.success(jobsService.myPosts(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/favorites")
    public ApiResponse<List<JobsMobilePostVO>> favorites() {
        return ApiResponse.success(jobsService.favorites(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/posts/{id}/favorite")
    public ApiResponse<JobsMobilePostVO> favorite(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsService.favorite(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/posts/{id}/unfavorite")
    public ApiResponse<JobsMobilePostVO> unfavorite(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsService.unfavorite(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/applications")
    public ApiResponse<JobsMobileApplicationVO> apply(@Valid @RequestBody JobsApplicationCreateRequest request) {
        return ApiResponse.success(jobsService.apply(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/applications/mine")
    public ApiResponse<List<JobsMobileApplicationVO>> myApplications() {
        return ApiResponse.success(jobsService.myApplications(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/conversations")
    public ApiResponse<List<JobsConversationVO>> conversations() {
        return ApiResponse.success(jobsService.conversations(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/conversations/{id}")
    public ApiResponse<JobsConversationVO> conversationDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsService.conversationDetail(id, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/posts/{id}/conversation")
    public ApiResponse<JobsConversationVO> ensureConversation(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsService.ensureConversation(id, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/resume")
    public ApiResponse<JobsResumeVO> resume() {
        return ApiResponse.success(jobsService.getResume(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/resume")
    public ApiResponse<JobsResumeVO> saveResume(@Valid @RequestBody JobsResumeSaveRequest request) {
        return ApiResponse.success(jobsService.saveResume(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/review-progress")
    public ApiResponse<JobsReviewProgressVO> reviewProgress() {
        return ApiResponse.success(jobsService.reviewProgress(SecurityUtils.getCurrentUserId()));
    }
}
