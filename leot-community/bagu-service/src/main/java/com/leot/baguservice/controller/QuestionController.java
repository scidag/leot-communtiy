package com.leot.baguservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leot.baguservice.domain.dto.AddQuestionDTO;
import com.leot.baguservice.domain.dto.QueryQuestionDTO;
import com.leot.baguservice.domain.dto.SearchQuestionDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionDTO;
import com.leot.baguservice.domain.vo.QuestionVO;
import com.leot.baguservice.service.QuestionService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.request.DeleteRequest;
import com.leot.leotcommon.request.PageRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 创建题目（管理员）
     */
    @PostMapping("/add")
    @SaCheckRole("admin")
    public BaseResponse<Long> addQuestion(@RequestBody AddQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long questionId = questionService.addQuestion(dto, userId);
        return ResultUtil.success(questionId);
    }

    /**
     * 更新题目（管理员）
     */
    @PutMapping("/update")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> updateQuestion(@RequestBody UpdateQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Boolean result = questionService.updateQuestion(dto);
        return ResultUtil.success(result);
    }


    /**
     * 删除题目（管理员）
     */
    @DeleteMapping("/delete")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest request) {
        if (ObjUtil.isEmpty(request) || request.getId() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Boolean result = questionService.deleteQuestion(request.getId());
        return ResultUtil.success(result);
    }

    /**
     * 获取题目详情（公开）
     */
    @GetMapping("/get")
    public BaseResponse<QuestionVO> getQuestionById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID无效");
        }
        QuestionVO questionVO = questionService.getQuestionById(id);
        return ResultUtil.success(questionVO);
    }

    /**
     * 分页查询题目列表（公开）
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionVO>> listQuestionByPage(@RequestBody QueryQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Page<QuestionVO> page = questionService.listQuestionByPage(dto);
        return ResultUtil.success(page);
    }

    /**
     * 搜索题目（公开）
     */
    @PostMapping("/search")
    public BaseResponse<Page<QuestionVO>> searchQuestion(@RequestBody SearchQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Page<QuestionVO> page = questionService.searchQuestion(dto);
        return ResultUtil.success(page);
    }

    /**
     * 点赞/取消点赞题目（需登录）
     */
    @PostMapping("/thumb")
    public BaseResponse<Boolean> thumbQuestion(@RequestParam Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID无效");
        }
        // 从 Sa-Token 获取当前登录用户ID（Redis 共享 Token）
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = questionService.thumbQuestion(questionId, userId);
        return ResultUtil.success(result);
    }

    /**
     * 收藏/取消收藏题目（需登录）
     */
    @PostMapping("/favour")
    public BaseResponse<Boolean> favourQuestion(@RequestParam Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID无效");
        }
        // 从 Sa-Token 获取当前登录用户ID（Redis 共享 Token）
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = questionService.favourQuestion(questionId, userId);
        return ResultUtil.success(result);
    }

    /**
     * 获取用户收藏的题目列表（需登录）
     */
    @PostMapping("/favour/list")
    public BaseResponse<Page<QuestionVO>> listFavourQuestion(@RequestBody PageRequest pageRequest) {
        if (ObjUtil.isEmpty(pageRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        // 从 Sa-Token 获取当前登录用户ID（Redis 共享 Token）
        Long userId = StpUtil.getLoginIdAsLong();
        Page<QuestionVO> page = questionService.listFavourQuestion(userId, pageRequest);
        return ResultUtil.success(page);
    }
}
