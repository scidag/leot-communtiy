package com.leot.baguservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leot.baguservice.domain.dto.AddQuestionBankDTO;
import com.leot.baguservice.domain.dto.QueryQuestionBankDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionBankDTO;
import com.leot.baguservice.domain.pojo.QuestionBank;
import com.leot.baguservice.domain.pojo.QuestionBankQuestion;
import com.leot.baguservice.domain.vo.QuestionBankVO;
import com.leot.baguservice.mapper.QuestionBankMapper;
import com.leot.baguservice.mapper.QuestionBankQuestionMapper;
import com.leot.baguservice.service.QuestionBankService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对表【question_bank(题库)】的数据库操作Service实现
 */
@Slf4j
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankService {

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addQuestionBank(AddQuestionBankDTO dto, Long userId) {
        log.info("创建题库, userId={}, title={}", userId, dto != null ? dto.getTitle() : null);
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        // 标题校验
        validateTitle(dto.getTitle(), true);

        // 创建题库实体
        QuestionBank questionBank = new QuestionBank();
        BeanUtil.copyProperties(dto, questionBank);
        questionBank.setUserId(userId);
        questionBank.setViewNum(0);
        questionBank.setCreateTime(new Date());
        questionBank.setUpdateTime(new Date());
        questionBank.setEditTime(new Date());
        questionBank.setIsDelete(0);

        // 保存到数据库
        boolean saved = this.save(questionBank);
        if (!saved) {
            log.error("创建题库失败, userId={}, title={}", userId, dto.getTitle());
            throw new BusinessException(ErrorCode.DATABASE_OPERATION_ERROR, "创建题库失败");
        }
        log.info("创建题库成功, questionBankId={}", questionBank.getId());
        return questionBank.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateQuestionBank(UpdateQuestionBankDTO dto) {
        log.info("更新题库, questionBankId={}", dto != null ? dto.getId() : null);
        // 参数校验
        if (ObjUtil.isEmpty(dto) || ObjUtil.isEmpty(dto.getId())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }

        // 检查题库是否存在
        QuestionBank existingBank = this.getById(dto.getId());
        if (existingBank == null) {
            log.warn("更新题库失败, 题库不存在, questionBankId={}", dto.getId());
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 标题校验（如果提供了标题）
        if (StrUtil.isNotBlank(dto.getTitle())) {
            validateTitle(dto.getTitle(), false);
        }

        // 更新题库
        QuestionBank questionBank = new QuestionBank();
        BeanUtil.copyProperties(dto, questionBank);
        questionBank.setUpdateTime(new Date());
        questionBank.setEditTime(new Date());

        boolean result = this.updateById(questionBank);
        log.info("更新题库完成, questionBankId={}, result={}", dto.getId(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteQuestionBank(Long id) {
        log.info("删除题库, questionBankId={}", id);
        // 参数校验
        if (ObjUtil.isEmpty(id)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID不能为空");
        }

        // 检查题库是否存在
        QuestionBank existingBank = this.getById(id);
        if (existingBank == null) {
            log.warn("删除题库失败, 题库不存在, questionBankId={}", id);
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 解除该题库与所有题目的关联（硬删除关联记录）
        QueryWrapper<QuestionBankQuestion> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("questionBankId", id);
        int deletedRelations = questionBankQuestionMapper.delete(deleteWrapper);
        log.info("解除题库关联, questionBankId={}, deletedRelations={}", id, deletedRelations);

        // 逻辑删除题库
        boolean result = this.removeById(id);
        log.info("删除题库完成, questionBankId={}, result={}", id, result);
        return result;
    }

    @Override
    public QuestionBankVO getQuestionBankById(Long id) {
        // 参数校验
        if (ObjUtil.isEmpty(id)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题库ID不能为空");
        }

        // 查询题库
        QuestionBank questionBank = this.getById(id);
        if (questionBank == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "题库不存在");
        }

        // 增加浏览量
        incrementViewNum(id);

        // 转换为VO
        return convertToVO(questionBank);
    }

    @Override
    public Page<QuestionBankVO> listQuestionBankByPage(QueryQuestionBankDTO dto) {
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }

        int current = dto.getCurrent();
        int pageSize = dto.getPageSize();

        // 构建查询条件
        QueryWrapper<QuestionBank> queryWrapper = getQueryWrapper(dto);

        // 分页查询
        Page<QuestionBank> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<QuestionBankVO> voPage = new Page<>(current, pageSize, page.getTotal());
        List<QuestionBankVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QueryQuestionBankDTO dto) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();

        if (dto == null) {
            return queryWrapper;
        }

        Long id = dto.getId();
        String title = dto.getTitle();
        Long userId = dto.getUserId();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();

        // 构建查询条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);

        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    public QuestionBankVO convertToVO(QuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }

        QuestionBankVO vo = new QuestionBankVO();
        BeanUtil.copyProperties(questionBank, vo);

        // 查询题目数量
        QueryWrapper<QuestionBankQuestion> countWrapper = new QueryWrapper<>();
        countWrapper.eq("questionBankId", questionBank.getId());
        Long questionCount = questionBankQuestionMapper.selectCount(countWrapper);
        vo.setQuestionCount(questionCount.intValue());

        // TODO: 可以通过远程调用获取用户名称
        // vo.setUserName(userService.getUserById(questionBank.getUserId()).getUserName());

        return vo;
    }

    @Override
    public void validateTitle(String title, boolean isAdd) {
        if (isAdd) {
            // 新增时标题不能为空
            if (StrUtil.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
            }
        }

        // 标题长度校验
        if (StrUtil.isNotBlank(title) && title.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度不能超过256字符");
        }
    }

    /**
     * 增加浏览量
     * @param id 题库ID
     */
    private void incrementViewNum(Long id) {
        UpdateWrapper<QuestionBank> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.setSql("viewNum = viewNum + 1");
        this.update(updateWrapper);
    }
}
