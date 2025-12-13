package com.leot.baguservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leot.baguservice.domain.dto.AddQuestionDTO;
import com.leot.baguservice.domain.dto.QueryQuestionDTO;
import com.leot.baguservice.domain.dto.SearchQuestionDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionDTO;
import com.leot.baguservice.domain.pojo.Question;
import com.leot.baguservice.domain.vo.QuestionVO;
import com.leot.leotcommon.request.PageRequest;

import java.util.List;

/**
 * 针对表【question(题目)】的数据库操作Service
 */
public interface QuestionService extends IService<Question> {

    /**
     * 创建题目
     * @param dto 创建题目请求参数
     * @param userId 创建用户ID
     * @return 题目ID
     */
    Long addQuestion(AddQuestionDTO dto, Long userId);

    /**
     * 更新题目
     * @param dto 更新题目请求参数
     * @return 是否成功
     */
    Boolean updateQuestion(UpdateQuestionDTO dto);

    /**
     * 删除题目（逻辑删除，同时解除关联）
     * @param id 题目ID
     * @return 是否成功
     */
    Boolean deleteQuestion(Long id);

    /**
     * 根据ID获取题目详情（增加浏览量）
     * @param id 题目ID
     * @return 题目视图对象
     */
    QuestionVO getQuestionById(Long id);


    /**
     * 分页查询题目列表
     * @param dto 查询参数
     * @return 分页结果
     */
    Page<QuestionVO> listQuestionByPage(QueryQuestionDTO dto);

    /**
     * 搜索题目
     * @param dto 搜索参数
     * @return 分页结果
     */
    Page<QuestionVO> searchQuestion(SearchQuestionDTO dto);

    /**
     * 点赞/取消点赞题目
     * @param questionId 题目ID
     * @param userId 用户ID
     * @return 是否点赞（true=点赞，false=取消点赞）
     */
    Boolean thumbQuestion(Long questionId, Long userId);

    /**
     * 收藏/取消收藏题目
     * @param questionId 题目ID
     * @param userId 用户ID
     * @return 是否收藏（true=收藏，false=取消收藏）
     */
    Boolean favourQuestion(Long questionId, Long userId);

    /**
     * 获取用户收藏的题目列表
     * @param userId 用户ID
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    Page<QuestionVO> listFavourQuestion(Long userId, PageRequest pageRequest);

    /**
     * 构建查询条件
     * @param dto 查询参数
     * @return QueryWrapper
     */
    QueryWrapper<Question> getQueryWrapper(QueryQuestionDTO dto);

    /**
     * 将标签列表转换为JSON字符串
     * @param tags 标签列表
     * @return JSON字符串
     */
    String convertTagsToJson(List<String> tags);

    /**
     * 将JSON字符串解析为标签列表
     * @param tagsJson JSON字符串
     * @return 标签列表
     */
    List<String> parseTagsFromJson(String tagsJson);

    /**
     * 将Question转换为QuestionVO
     * @param question 题目实体
     * @return 题目视图对象
     */
    QuestionVO convertToVO(Question question);

    /**
     * 将Question转换为QuestionVO（带用户状态）
     * @param question 题目实体
     * @param userId 当前用户ID（可为null）
     * @return 题目视图对象
     */
    QuestionVO convertToVO(Question question, Long userId);

    /**
     * 校验题目标题
     * @param title 标题
     * @param isAdd 是否为新增操作
     */
    void validateTitle(String title, boolean isAdd);
}
