package com.leot.baguservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leot.baguservice.domain.pojo.Question;
import com.leot.baguservice.domain.pojo.QuestionBank;
import com.leot.baguservice.domain.pojo.QuestionBankQuestion;
import com.leot.baguservice.domain.vo.QuestionVO;
import com.leot.baguservice.mapper.QuestionBankMapper;
import com.leot.baguservice.mapper.QuestionBankQuestionMapper;
import com.leot.baguservice.mapper.QuestionMapper;
import com.leot.baguservice.service.QuestionBankQuestionService;
import com.leot.baguservice.service.QuestionService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 针对表【question_bank_question(题库题目关联)】的数据库操作Service实现
 */
@Slf4j
@Service
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion>
        implements QuestionBankQuestionService {

    @Resource
    private QuestionBankMapper questionBankMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addQuestionToBank(Long questionBankId, Long questionId, Long userId) {
        log.info("添加题目到题库, questionBankId={}, questionId={}, userId={}", questionBankId, questionId, userId);
        // 参数校验
        if (ObjUtil.isEmpty(questionBankId) || ObjUtil.isEmpty(questionId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID和题目ID不能为空");
        }


        // 检查题库是否存在
        QuestionBank questionBank = questionBankMapper.selectById(questionBankId);
        if (questionBank == null) {
            log.warn("添加题目到题库失败, 题库不存在, questionBankId={}", questionBankId);
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 检查题目是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            log.warn("添加题目到题库失败, 题目不存在, questionId={}", questionId);
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 检查关联是否已存在
        if (existsRelation(questionBankId, questionId)) {
            log.warn("添加题目到题库失败, 关联已存在, questionBankId={}, questionId={}", questionBankId, questionId);
            throw new BusinessException(ErrorCode.EXIST_ERROR, "该题目已存在于此题库中");
        }

        // 创建关联记录
        QuestionBankQuestion relation = new QuestionBankQuestion();
        relation.setQuestionBankId(questionBankId);
        relation.setQuestionId(questionId);
        relation.setUserId(userId);
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());

        boolean result = this.save(relation);
        log.info("添加题目到题库完成, questionBankId={}, questionId={}, result={}", questionBankId, questionId, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAddQuestionToBank(Long questionBankId, List<Long> questionIds, Long userId) {
        log.info("批量添加题目到题库, questionBankId={}, questionCount={}, userId={}", questionBankId, questionIds != null ? questionIds.size() : 0, userId);
        // 参数校验
        if (ObjUtil.isEmpty(questionBankId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID不能为空");
        }
        if (CollUtil.isEmpty(questionIds)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题目ID列表不能为空");
        }

        // 检查题库是否存在
        QuestionBank questionBank = questionBankMapper.selectById(questionBankId);
        if (questionBank == null) {
            log.warn("批量添加题目到题库失败, 题库不存在, questionBankId={}", questionBankId);
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 查询已存在的关联
        QueryWrapper<QuestionBankQuestion> existWrapper = new QueryWrapper<>();
        existWrapper.eq("questionBankId", questionBankId);
        existWrapper.in("questionId", questionIds);
        List<QuestionBankQuestion> existingRelations = this.list(existWrapper);
        Set<Long> existingQuestionIds = existingRelations.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toSet());

        // 过滤出需要新增的题目ID
        List<Long> newQuestionIds = questionIds.stream()
                .filter(id -> !existingQuestionIds.contains(id))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(newQuestionIds)) {
            log.info("批量添加题目到题库, 所有题目已存在, questionBankId={}", questionBankId);
            return 0;
        }

        // 验证题目是否存在
        QueryWrapper<Question> questionWrapper = new QueryWrapper<>();
        questionWrapper.in("id", newQuestionIds);
        List<Question> existingQuestions = questionMapper.selectList(questionWrapper);
        Set<Long> validQuestionIds = existingQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        // 批量创建关联记录
        List<QuestionBankQuestion> relations = new ArrayList<>();
        Date now = new Date();
        for (Long questionId : newQuestionIds) {
            if (validQuestionIds.contains(questionId)) {
                QuestionBankQuestion relation = new QuestionBankQuestion();
                relation.setQuestionBankId(questionBankId);
                relation.setQuestionId(questionId);
                relation.setUserId(userId);
                relation.setCreateTime(now);
                relation.setUpdateTime(now);
                relations.add(relation);
            }
        }

        if (CollUtil.isNotEmpty(relations)) {
            this.saveBatch(relations);
        }

        log.info("批量添加题目到题库完成, questionBankId={}, addedCount={}", questionBankId, relations.size());
        return relations.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeQuestionFromBank(Long questionBankId, Long questionId) {
        log.info("从题库移除题目, questionBankId={}, questionId={}", questionBankId, questionId);
        // 参数校验
        if (ObjUtil.isEmpty(questionBankId) || ObjUtil.isEmpty(questionId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID和题目ID不能为空");
        }

        // 硬删除关联记录
        QueryWrapper<QuestionBankQuestion> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("questionBankId", questionBankId);
        deleteWrapper.eq("questionId", questionId);

        int deleted = this.baseMapper.delete(deleteWrapper);
        log.info("从题库移除题目完成, questionBankId={}, questionId={}, deleted={}", questionBankId, questionId, deleted);
        return deleted > 0;
    }

    @Override
    public List<QuestionVO> listQuestionByBankId(Long questionBankId) {
        // 参数校验
        if (ObjUtil.isEmpty(questionBankId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID不能为空");
        }

        // 检查题库是否存在
        QuestionBank questionBank = questionBankMapper.selectById(questionBankId);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 查询关联的题目ID
        QueryWrapper<QuestionBankQuestion> relationWrapper = new QueryWrapper<>();
        relationWrapper.eq("questionBankId", questionBankId);
        relationWrapper.select("questionId");
        List<QuestionBankQuestion> relations = this.list(relationWrapper);

        if (CollUtil.isEmpty(relations)) {
            return new ArrayList<>();
        }

        List<Long> questionIds = relations.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toList());

        // 查询题目信息
        QueryWrapper<Question> questionWrapper = new QueryWrapper<>();
        questionWrapper.in("id", questionIds);
        questionWrapper.orderByDesc("createTime");
        List<Question> questions = questionMapper.selectList(questionWrapper);

        // 转换为VO
        return questions.stream()
                .map(questionService::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean existsRelation(Long questionBankId, Long questionId) {
        if (ObjUtil.isEmpty(questionBankId) || ObjUtil.isEmpty(questionId)) {
            return false;
        }

        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", questionBankId);
        queryWrapper.eq("questionId", questionId);

        return this.count(queryWrapper) > 0;
    }
}
