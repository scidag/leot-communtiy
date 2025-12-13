package com.leot.baguservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leot.baguservice.domain.pojo.QuestionBankQuestion;
import com.leot.baguservice.domain.vo.QuestionVO;

import java.util.List;

/**
 * 针对表【question_bank_question(题库题目关联)】的数据库操作Service
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 添加题目到题库
     * @param questionBankId 题库ID
     * @param questionId 题目ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    Boolean addQuestionToBank(Long questionBankId, Long questionId, Long userId);

    /**
     * 批量添加题目到题库（跳过已存在的关联）
     * @param questionBankId 题库ID
     * @param questionIds 题目ID列表
     * @param userId 操作用户ID
     * @return 成功添加的数量
     */
    Integer batchAddQuestionToBank(Long questionBankId, List<Long> questionIds, Long userId);

    /**
     * 从题库移除题目（硬删除）
     * @param questionBankId 题库ID
     * @param questionId 题目ID
     * @return 是否成功
     */
    Boolean removeQuestionFromBank(Long questionBankId, Long questionId);

    /**
     * 查询题库下的题目列表
     * @param questionBankId 题库ID
     * @return 题目视图对象列表
     */
    List<QuestionVO> listQuestionByBankId(Long questionBankId);

    /**
     * 检查关联是否存在
     * @param questionBankId 题库ID
     * @param questionId 题目ID
     * @return 是否存在
     */
    Boolean existsRelation(Long questionBankId, Long questionId);
}
