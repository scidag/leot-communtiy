package com.leot.baguservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leot.baguservice.domain.dto.AddQuestionBankDTO;
import com.leot.baguservice.domain.dto.QueryQuestionBankDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionBankDTO;
import com.leot.baguservice.domain.vo.QuestionBankVO;
import com.leot.baguservice.service.QuestionBankService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.request.DeleteRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 题库管理控制器
 */
@RestController
@RequestMapping("/questionBank")
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    /**
     * 创建题库（管理员）
     */
    @PostMapping("/add")
    @SaCheckRole("admin")
    public BaseResponse<Long> addQuestionBank(@RequestBody AddQuestionBankDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        Long questionBankId = questionBankService.addQuestionBank(dto, userId);
        return ResultUtil.success(questionBankId);
    }

    /**
     * 更新题库（管理员）
     */
    @PutMapping("/update")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody UpdateQuestionBankDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Boolean result = questionBankService.updateQuestionBank(dto);
        return ResultUtil.success(result);
    }

    /**
     * 删除题库（管理员）
     */
    @DeleteMapping("/delete")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest request) {
        if (ObjUtil.isEmpty(request) || request.getId() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Boolean result = questionBankService.deleteQuestionBank(request.getId());
        return ResultUtil.success(result);
    }

    /**
     * 获取题库详情（公开）
     */
    @GetMapping("/get")
    public BaseResponse<QuestionBankVO> getQuestionBankById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题库ID无效");
        }
        QuestionBankVO questionBankVO = questionBankService.getQuestionBankById(id);
        return ResultUtil.success(questionBankVO);
    }

    /**
     * 分页查询题库列表（公开）
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankByPage(@RequestBody QueryQuestionBankDTO dto) {
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        Page<QuestionBankVO> page = questionBankService.listQuestionBankByPage(dto);
        return ResultUtil.success(page);
    }
}
