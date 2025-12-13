package com.leot.baguservice.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.leot.baguservice.domain.dto.AddCommentDTO;
import com.leot.baguservice.domain.dto.ReplyCommentDTO;
import com.leot.baguservice.domain.vo.QuestionCommentVO;
import com.leot.baguservice.service.QuestionCommentService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.request.DeleteRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目评论控制器
 */
@RestController
@RequestMapping("/questionComment")
public class QuestionCommentController {

    @Resource
    private QuestionCommentService questionCommentService;

    /**
     * 发表评论（需登录）
     */
    @PostMapping("/add")
    @SaCheckLogin
    public BaseResponse<Long> addComment(@RequestBody AddCommentDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long commentId = questionCommentService.addComment(dto, userId);
        return ResultUtil.success(commentId);
    }

    /**
     * 回复评论（需登录）
     */
    @PostMapping("/reply")
    @SaCheckLogin
    public BaseResponse<Long> replyComment(@RequestBody ReplyCommentDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long commentId = questionCommentService.replyComment(dto, userId);
        return ResultUtil.success(commentId);
    }

    /**
     * 删除评论（本人或管理员）
     */
    @DeleteMapping("/delete")
    @SaCheckLogin
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest request) {
        if (ObjUtil.isEmpty(request) || request.getId() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isAdmin = StpUtil.hasRole("admin");
        Boolean result = questionCommentService.deleteComment(request.getId(), userId, isAdmin);
        return ResultUtil.success(result);
    }

    /**
     * 获取题目评论列表（公开，树形结构）
     */
    @GetMapping("/list")
    public BaseResponse<List<QuestionCommentVO>> listCommentByQuestionId(@RequestParam Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID无效");
        }
        List<QuestionCommentVO> list = questionCommentService.listCommentByQuestionId(questionId);
        return ResultUtil.success(list);
    }

    /**
     * 点赞/取消点赞评论（需登录）
     */
    @PostMapping("/thumb")
    @SaCheckLogin
    public BaseResponse<Boolean> thumbComment(@RequestParam Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论ID无效");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = questionCommentService.thumbComment(commentId, userId);
        return ResultUtil.success(result);
    }
}
