package com.leot.baguservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.leot.baguservice.domain.dto.BatchImportDTO;
import com.leot.baguservice.domain.dto.ParsedQuestionDTO;
import com.leot.baguservice.domain.pojo.Question;
import com.leot.baguservice.domain.pojo.QuestionBankQuestion;
import com.leot.baguservice.domain.vo.BatchImportResultVO;
import com.leot.baguservice.mapper.QuestionBankQuestionMapper;
import com.leot.baguservice.mapper.QuestionMapper;
import com.leot.baguservice.service.QuestionBankService;
import com.leot.baguservice.service.QuestionImportService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 题目导入服务实现
 */
@Slf4j
@Service
public class QuestionImportServiceImpl implements QuestionImportService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Resource
    private QuestionBankService questionBankService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchImportResultVO batchImport(BatchImportDTO dto) {
        // 1. 参数校验
        if (dto == null || dto.getQuestionBankId() == null || dto.getQuestions() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "导入参数不能为空");
        }

        Long questionBankId = dto.getQuestionBankId();
        List<ParsedQuestionDTO> questions = dto.getQuestions();

        // 2. 校验题库是否存在
        if (questionBankService.getById(questionBankId) == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 3. 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 4. 批量导入题目
        int successCount = 0;
        int failCount = 0;
        int totalCount = questions.size();

        for (ParsedQuestionDTO parsedQuestion : questions) {
            try {
                // 创建题目
                Question question = createQuestion(parsedQuestion, userId);
                questionMapper.insert(question);

                // 关联到题库
                QuestionBankQuestion relation = new QuestionBankQuestion();
                relation.setQuestionBankId(questionBankId);
                relation.setQuestionId(question.getId());
                relation.setUserId(userId);
                relation.setCreateTime(new Date());
                relation.setUpdateTime(new Date());
                questionBankQuestionMapper.insert(relation);

                successCount++;
                log.debug("题目导入成功: {}", parsedQuestion.getTitle());
            } catch (Exception e) {
                failCount++;
                log.warn("题目导入失败: {}, 原因: {}", parsedQuestion.getTitle(), e.getMessage());
            }
        }

        // 5. 构建返回结果
        BatchImportResultVO result = new BatchImportResultVO();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setTotalCount(totalCount);

        log.info("批量导入完成，成功: {}, 失败: {}, 总数: {}", successCount, failCount, totalCount);
        return result;
    }

    /**
     * 根据解析的题目数据创建 Question 实体
     */
    private Question createQuestion(ParsedQuestionDTO parsedQuestion, Long userId) {
        Question question = new Question();
        question.setTitle(parsedQuestion.getTitle());
        question.setContent(parsedQuestion.getContent() != null ? parsedQuestion.getContent() : "");
        question.setAnswer(parsedQuestion.getAnswer() != null ? parsedQuestion.getAnswer() : "");
        question.setTags("[]"); // 默认空标签
        question.setUserId(userId);
        question.setViewNum(0);
        question.setThumbNum(0);
        question.setFavourNum(0);
        question.setEditTime(new Date());
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());
        question.setIsDelete(0);
        return question;
    }
}
