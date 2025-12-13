package com.leot.baguservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leot.baguservice.domain.dto.AddQuestionBankDTO;
import com.leot.baguservice.domain.dto.QueryQuestionBankDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionBankDTO;
import com.leot.baguservice.domain.pojo.QuestionBank;
import com.leot.baguservice.domain.vo.QuestionBankVO;

/**
 * 针对表【question_bank(题库)】的数据库操作Service
 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 创建题库
     * @param dto 创建题库请求参数
     * @param userId 创建用户ID
     * @return 题库ID
     */
    Long addQuestionBank(AddQuestionBankDTO dto, Long userId);

    /**
     * 更新题库
     * @param dto 更新题库请求参数
     * @return 是否成功
     */
    Boolean updateQuestionBank(UpdateQuestionBankDTO dto);

    /**
     * 删除题库（逻辑删除，同时解除关联）
     * @param id 题库ID
     * @return 是否成功
     */
    Boolean deleteQuestionBank(Long id);

    /**
     * 根据ID获取题库详情（增加浏览量）
     * @param id 题库ID
     * @return 题库视图对象
     */
    QuestionBankVO getQuestionBankById(Long id);

    /**
     * 分页查询题库列表
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<QuestionBankVO> listQuestionBankByPage(QueryQuestionBankDTO dto);

    /**
     * 构建查询条件
     * @param dto 查询参数
     * @return QueryWrapper
     */
    QueryWrapper<QuestionBank> getQueryWrapper(QueryQuestionBankDTO dto);

    /**
     * 将QuestionBank转换为QuestionBankVO
     * @param questionBank 题库实体
     * @return 题库视图对象
     */
    QuestionBankVO convertToVO(QuestionBank questionBank);

    /**
     * 校验题库标题
     * @param title 标题
     * @param isAdd 是否为新增操作
     */
    void validateTitle(String title, boolean isAdd);
}
