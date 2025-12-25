package com.leot.baguservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leot.api.client.UserClient;
import com.leot.api.dto.UserDTO;
import com.leot.baguservice.domain.dto.AddQuestionDTO;
import com.leot.baguservice.domain.dto.QueryQuestionDTO;
import com.leot.baguservice.domain.dto.SearchQuestionDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionDTO;
import com.leot.baguservice.domain.pojo.Question;
import com.leot.baguservice.domain.pojo.QuestionBankQuestion;
import com.leot.baguservice.domain.pojo.QuestionFavour;
import com.leot.baguservice.domain.pojo.QuestionThumb;
import com.leot.baguservice.domain.vo.QuestionVO;
import com.leot.baguservice.mapper.QuestionBankQuestionMapper;
import com.leot.baguservice.mapper.QuestionFavourMapper;
import com.leot.baguservice.mapper.QuestionMapper;
import com.leot.baguservice.mapper.QuestionThumbMapper;
import com.leot.baguservice.service.QuestionService;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.request.PageRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 针对表【question(题目)】的数据库操作Service实现
 */
@Slf4j
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Resource
    private QuestionThumbMapper questionThumbMapper;

    @Resource
    private QuestionFavourMapper questionFavourMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserClient userClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addQuestion(AddQuestionDTO dto, Long userId) {
        log.info("创建题目, userId={}, title={}", userId, dto != null ? dto.getTitle() : null);
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        // 标题校验
        validateTitle(dto.getTitle(), true);

        // 创建题目实体
        Question question = new Question();
        BeanUtil.copyProperties(dto, question);
        question.setUserId(userId);
        question.setViewNum(0);
        question.setThumbNum(0);
        question.setFavourNum(0);
        question.setCreateTime(new Date());
        question.setUpdateTime(new Date());
        question.setEditTime(new Date());
        question.setIsDelete(0);

        // 处理标签
        if (CollUtil.isNotEmpty(dto.getTags())) {
            question.setTags(convertTagsToJson(dto.getTags()));
        }

        // 保存到数据库
        boolean saved = this.save(question);
        if (!saved) {
            log.error("创建题目失败, userId={}, title={}", userId, dto.getTitle());
            throw new BusinessException(ErrorCode.DATABASE_OPERATION_ERROR, "创建题目失败");
        }
        log.info("创建题目成功, questionId={}", question.getId());
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateQuestion(UpdateQuestionDTO dto) {
        log.info("更新题目, questionId={}", dto != null ? dto.getId() : null);
        // 参数校验
        if (ObjUtil.isEmpty(dto) || ObjUtil.isEmpty(dto.getId())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }

        // 检查题目是否存在
        Question existingQuestion = this.getById(dto.getId());
        if (existingQuestion == null) {
            log.warn("更新题目失败, 题目不存在, questionId={}", dto.getId());
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 标题校验（如果提供了标题）
        if (StrUtil.isNotBlank(dto.getTitle())) {
            validateTitle(dto.getTitle(), false);
        }

        // 更新题目
        Question question = new Question();
        BeanUtil.copyProperties(dto, question);
        question.setUpdateTime(new Date());
        question.setEditTime(new Date());

        // 处理标签
        if (dto.getTags() != null) {
            question.setTags(convertTagsToJson(dto.getTags()));
        }

        boolean result = this.updateById(question);
        log.info("更新题目完成, questionId={}, result={}", dto.getId(), result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteQuestion(Long id) {
        log.info("删除题目, questionId={}", id);
        // 参数校验
        if (ObjUtil.isEmpty(id)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题目ID不能为空");
        }

        // 检查题目是否存在
        Question existingQuestion = this.getById(id);
        if (existingQuestion == null) {
            log.warn("删除题目失败, 题目不存在, questionId={}", id);
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 解除该题目与所有题库的关联（硬删除关联记录）
        QueryWrapper<QuestionBankQuestion> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("questionId", id);
        int deletedRelations = questionBankQuestionMapper.delete(deleteWrapper);
        log.info("解除题目关联, questionId={}, deletedRelations={}", id, deletedRelations);

        // 逻辑删除题目
        boolean result = this.removeById(id);
        log.info("删除题目完成, questionId={}, result={}", id, result);
        return result;
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        // 参数校验
        if (ObjUtil.isEmpty(id)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题目ID不能为空");
        }

        // 查询题目
        Question question = this.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 增加浏览量
        questionMapper.incrementViewNum(id);

        // 转换为VO
        return convertToVO(question);
    }


    @Override
    public Page<QuestionVO> listQuestionByPage(QueryQuestionDTO dto) {
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }

        int current = dto.getCurrent();
        int pageSize = dto.getPageSize();

        // 构建查询条件
        QueryWrapper<Question> queryWrapper = getQueryWrapper(dto);

        // 分页查询
        Page<Question> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 使用批量转换优化
        Page<QuestionVO> voPage = new Page<>(current, pageSize, page.getTotal());
        List<QuestionVO> voList = convertToVOList(page.getRecords(), null);
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Page<QuestionVO> searchQuestion(SearchQuestionDTO dto) {
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }

        int current = dto.getCurrent();
        int pageSize = dto.getPageSize();

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        // 关键词搜索（标题模糊匹配）
        String keyword = dto.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like("title", keyword);
        }

        // 标签筛选
        List<String> tags = dto.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 题库筛选
        Long questionBankId = dto.getQuestionBankId();
        if (ObjUtil.isNotNull(questionBankId)) {
            // 查询题库下的题目ID列表
            QueryWrapper<QuestionBankQuestion> bankQuestionWrapper = new QueryWrapper<>();
            bankQuestionWrapper.eq("questionBankId", questionBankId);
            bankQuestionWrapper.select("questionId");
            List<QuestionBankQuestion> bankQuestions = questionBankQuestionMapper.selectList(bankQuestionWrapper);
            List<Long> questionIds = bankQuestions.stream()
                    .map(QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(questionIds)) {
                // 如果题库下没有题目，返回空结果
                Page<QuestionVO> emptyPage = new Page<>(current, pageSize, 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            queryWrapper.in("id", questionIds);
        }

        // 排序
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            // 验证排序字段是否合法
            if (isValidSortField(sortField)) {
                boolean isAsc = "ascend".equals(sortOrder) || "asc".equals(sortOrder);
                queryWrapper.orderBy(true, isAsc, sortField);
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        // 分页查询
        Page<Question> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 使用批量转换优化
        Page<QuestionVO> voPage = new Page<>(current, pageSize, page.getTotal());
        List<QuestionVO> voList = convertToVOList(page.getRecords(), null);
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortField(String sortField) {
        List<String> validFields = List.of("viewNum", "thumbNum", "favourNum", "createTime", "updateTime");
        return validFields.contains(sortField);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean thumbQuestion(Long questionId, Long userId) {
        log.info("点赞/取消点赞题目, questionId={}, userId={}", questionId, userId);
        // 参数校验
        if (ObjUtil.isEmpty(questionId) || ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数不能为空");
        }

        // 检查题目是否存在
        Question question = this.getById(questionId);
        if (question == null) {
            log.warn("点赞失败, 题目不存在, questionId={}", questionId);
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 查询是否已点赞
        QueryWrapper<QuestionThumb> thumbWrapper = new QueryWrapper<>();
        thumbWrapper.eq("questionId", questionId);
        thumbWrapper.eq("userId", userId);
        QuestionThumb existingThumb = questionThumbMapper.selectOne(thumbWrapper);

        if (existingThumb != null) {
            // 已点赞，取消点赞
            questionThumbMapper.deleteById(existingThumb.getId());
            questionMapper.updateThumbNum(questionId, -1);
            log.info("取消点赞成功, questionId={}, userId={}", questionId, userId);
            return false;
        } else {
            // 未点赞，添加点赞
            QuestionThumb thumb = new QuestionThumb();
            thumb.setQuestionId(questionId);
            thumb.setUserId(userId);
            thumb.setCreateTime(new Date());
            questionThumbMapper.insert(thumb);
            questionMapper.updateThumbNum(questionId, 1);
            log.info("点赞成功, questionId={}, userId={}", questionId, userId);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favourQuestion(Long questionId, Long userId) {
        log.info("收藏/取消收藏题目, questionId={}, userId={}", questionId, userId);
        // 参数校验
        if (ObjUtil.isEmpty(questionId) || ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数不能为空");
        }

        // 检查题目是否存在
        Question question = this.getById(questionId);
        if (question == null) {
            log.warn("收藏失败, 题目不存在, questionId={}", questionId);
            throw new BusinessException(ErrorCode.NO_FOUND, "题目不存在");
        }

        // 查询是否已收藏
        QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
        favourWrapper.eq("questionId", questionId);
        favourWrapper.eq("userId", userId);
        QuestionFavour existingFavour = questionFavourMapper.selectOne(favourWrapper);

        if (existingFavour != null) {
            // 已收藏，取消收藏
            questionFavourMapper.deleteById(existingFavour.getId());
            questionMapper.updateFavourNum(questionId, -1);
            log.info("取消收藏成功, questionId={}, userId={}", questionId, userId);
            return false;
        } else {
            // 未收藏，添加收藏
            QuestionFavour favour = new QuestionFavour();
            favour.setQuestionId(questionId);
            favour.setUserId(userId);
            favour.setCreateTime(new Date());
            questionFavourMapper.insert(favour);
            questionMapper.updateFavourNum(questionId, 1);
            log.info("收藏成功, questionId={}, userId={}", questionId, userId);
            return true;
        }
    }

    @Override
    public Page<QuestionVO> listFavourQuestion(Long userId, PageRequest pageRequest) {
        // 参数校验
        if (ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户ID不能为空");
        }

        int current = pageRequest.getCurrent();
        int pageSize = pageRequest.getPageSize();

        // 查询用户收藏的题目ID列表
        QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
        favourWrapper.eq("userId", userId);
        favourWrapper.select("questionId");
        List<QuestionFavour> favours = questionFavourMapper.selectList(favourWrapper);
        List<Long> questionIds = favours.stream()
                .map(QuestionFavour::getQuestionId)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(questionIds)) {
            // 如果没有收藏，返回空结果
            Page<QuestionVO> emptyPage = new Page<>(current, pageSize, 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 查询题目
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", questionIds);
        queryWrapper.orderByDesc("createTime");

        Page<Question> page = this.page(new Page<>(current, pageSize), queryWrapper);

        // 转换为VO
        Page<QuestionVO> voPage = new Page<>(current, pageSize, page.getTotal());
        List<QuestionVO> voList = page.getRecords().stream()
                .map(q -> convertToVO(q, userId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }


    @Override
    public QueryWrapper<Question> getQueryWrapper(QueryQuestionDTO dto) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();

        if (dto == null) {
            return queryWrapper;
        }

        Long id = dto.getId();
        String title = dto.getTitle();
        String content = dto.getContent();
        List<String> tags = dto.getTags();
        Long userId = dto.getUserId();
        Long questionBankId = dto.getQuestionBankId();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();

        // 构建查询条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);

        // 标签筛选
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 题库筛选
        if (ObjUtil.isNotNull(questionBankId)) {
            QueryWrapper<QuestionBankQuestion> bankQuestionWrapper = new QueryWrapper<>();
            bankQuestionWrapper.eq("questionBankId", questionBankId);
            bankQuestionWrapper.select("questionId");
            List<QuestionBankQuestion> bankQuestions = questionBankQuestionMapper.selectList(bankQuestionWrapper);
            List<Long> questionIds = bankQuestions.stream()
                    .map(QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(questionIds)) {
                queryWrapper.in("id", questionIds);
            } else {
                // 如果题库下没有题目，返回空结果
                queryWrapper.eq("id", -1);
            }
        }

        // 排序
        if (StrUtil.isNotBlank(sortField) && isValidSortField(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder) || "asc".equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    public String convertTagsToJson(List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return "[]";
        }
        return JSONUtil.toJsonStr(tags);
    }

    @Override
    public List<String> parseTagsFromJson(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(tagsJson, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public QuestionVO convertToVO(Question question) {
        return convertToVO(question, null);
    }

    @Override
    public QuestionVO convertToVO(Question question, Long userId) {
        if (question == null) {
            return null;
        }

        QuestionVO vo = new QuestionVO();
        BeanUtil.copyProperties(question, vo);

        // 解析标签
        vo.setTags(parseTagsFromJson(question.getTags()));

        // 设置用户点赞/收藏状态
        if (userId != null) {
            // 查询是否点赞
            QueryWrapper<QuestionThumb> thumbWrapper = new QueryWrapper<>();
            thumbWrapper.eq("questionId", question.getId());
            thumbWrapper.eq("userId", userId);
            vo.setHasThumb(questionThumbMapper.selectCount(thumbWrapper) > 0);

            // 查询是否收藏
            QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
            favourWrapper.eq("questionId", question.getId());
            favourWrapper.eq("userId", userId);
            vo.setHasFavour(questionFavourMapper.selectCount(favourWrapper) > 0);
        } else {
            vo.setHasThumb(false);
            vo.setHasFavour(false);
        }

        // 通过远程调用获取用户名称
        try {
            if (question.getUserId() != null) {
                BaseResponse<UserDTO> userResponse = userClient.getUserById(question.getUserId());
                if (userResponse != null && userResponse.getData() != null) {
                    vo.setUserName(userResponse.getData().getUserName());
                }
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}, error={}", question.getUserId(), e.getMessage());
            // 降级处理，不影响主流程
        }

        // TODO: 可以查询评论数
        // vo.setCommentNum(commentService.countByQuestionId(question.getId()));

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
     * 批量转换 Question 列表为 QuestionVO 列表
     * 优化：批量获取用户信息，减少远程调用次数
     *
     * @param questions 题目列表
     * @param currentUserId 当前用户ID（用于判断点赞/收藏状态）
     * @return QuestionVO 列表
     */
    @Override
    public List<QuestionVO> convertToVOList(List<Question> questions, Long currentUserId) {
        if (CollUtil.isEmpty(questions)) {
            return new ArrayList<>();
        }

        // 收集所有不重复的 userId
        List<Long> userIds = questions.stream()
                .map(Question::getUserId)
                .filter(ObjUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<Long, UserDTO> userMap = new java.util.HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            try {
                BaseResponse<List<UserDTO>> userResponse = userClient.getUserListByIds(userIds);
                if (userResponse != null && CollUtil.isNotEmpty(userResponse.getData())) {
                    userMap = userResponse.getData().stream()
                            .filter(u -> u != null && u.getId() != null)
                            .collect(Collectors.toMap(UserDTO::getId, u -> u, (a, b) -> a));
                }
            } catch (Exception e) {
                log.warn("批量获取用户信息失败, userIds={}, error={}", userIds, e.getMessage());
                // 降级处理，不影响主流程
            }
        }

        // 转换为 VO 列表
        Map<Long, UserDTO> finalUserMap = userMap;
        return questions.stream()
                .map(question -> {
                    QuestionVO vo = convertToVOWithoutUser(question, currentUserId);
                    // 从缓存的用户信息中获取用户名
                    if (question.getUserId() != null && finalUserMap.containsKey(question.getUserId())) {
                        vo.setUserName(finalUserMap.get(question.getUserId()).getUserName());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为 VO（不包含用户信息查询，用于批量转换）
     */
    private QuestionVO convertToVOWithoutUser(Question question, Long userId) {
        if (question == null) {
            return null;
        }

        QuestionVO vo = new QuestionVO();
        BeanUtil.copyProperties(question, vo);

        // 解析标签
        vo.setTags(parseTagsFromJson(question.getTags()));

        // 设置用户点赞/收藏状态
        if (userId != null) {
            QueryWrapper<QuestionThumb> thumbWrapper = new QueryWrapper<>();
            thumbWrapper.eq("questionId", question.getId());
            thumbWrapper.eq("userId", userId);
            vo.setHasThumb(questionThumbMapper.selectCount(thumbWrapper) > 0);

            QueryWrapper<QuestionFavour> favourWrapper = new QueryWrapper<>();
            favourWrapper.eq("questionId", question.getId());
            favourWrapper.eq("userId", userId);
            vo.setHasFavour(questionFavourMapper.selectCount(favourWrapper) > 0);
        } else {
            vo.setHasThumb(false);
            vo.setHasFavour(false);
        }

        return vo;
    }
}
