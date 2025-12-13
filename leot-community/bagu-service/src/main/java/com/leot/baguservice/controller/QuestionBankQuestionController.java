package com.leot.baguservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.leot.baguservice.domain.dto.AddQuestionBankQuestionDTO;
import com.leot.baguservice.domain.dto.BatchAddQuestionDTO;
import com.leot.baguservice.domain.vo.QuestionVO;
import com.leot.baguservice.service.QuestionBankQuestionService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库题目关联控制器
 */
@RestController
@RequestMapping("/questionBankQuestion")
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    /**
     * 添加题目到题库（管理员）
     */
    @PostMapping("/add")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> addQuestionToBank(@RequestBody AddQuestionBankQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = questionBankQuestionService.addQuestionToBank(
                dto.getQuestionBankId(), dto.getQuestionId(), userId);
        return ResultUtil.success(result);
    }

    /**
     * 批量添加题目到题库（管理员）
     */
    @PostMapping("/add/batch")
    @SaCheckRole("admin")
    public BaseResponse<Integer> batchAddQuestionToBank(@RequestBody BatchAddQuestionDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Integer count = questionBankQuestionService.batchAddQuestionToBank(
                dto.getQuestionBankId(), dto.getQuestionIds(), userId);
        return ResultUtil.success(count);
    }

    /**
     * 从题库移除题目（管理员）
     */
    @DeleteMapping("/remove")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> removeQuestionFromBank(
            @RequestParam Long questionBankId,
            @RequestParam Long questionId) {
        if (questionBankId == null || questionBankId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库ID无效");
        }
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID无效");
        }
        Boolean result = questionBankQuestionService.removeQuestionFromBank(questionBankId, questionId);
        return ResultUtil.success(result);
    }

    /**
     * 查询题库下的题目列表（公开）
     */
    @GetMapping("/list")
    public BaseResponse<List<QuestionVO>> listQuestionByBankId(@RequestParam Long questionBankId) {
        if (questionBankId == null || questionBankId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库ID无效");
        }
        List<QuestionVO> list = questionBankQuestionService.listQuestionByBankId(questionBankId);
        return ResultUtil.success(list);
    }
}
