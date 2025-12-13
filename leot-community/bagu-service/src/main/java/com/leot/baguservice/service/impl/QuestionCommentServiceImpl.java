package com.leot.baguservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leot.baguservice.domain.dto.AddCommentDTO;
import com.leot.baguservice.domain.dto.ReplyCommentDTO;
import com.leot.baguservice.domain.pojo.QuestionComment;
import com.leot.baguservice.domain.pojo.QuestionCommentThumb;
import com.leot.baguservice.domain.vo.QuestionCommentVO;
import com.leot.baguservice.mapper.QuestionCommentMapper;
import com.leot.baguservice.mapper.QuestionCommentThumbMapper;
import com.leot.baguservice.service.QuestionCommentService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 针对表【question_comment(题目评论)】的数据库操作Service实现
 */
@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment>
        implements QuestionCommentService {

    @Resource
    private QuestionCommentMapper questionCommentMapper;

    @Resource
    private QuestionCommentThumbMapper questionCommentThumbMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(AddCommentDTO dto, Long userId) {
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        if (ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        if (ObjUtil.isEmpty(dto.getQuestionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID不能为空");
        }

        // 内容校验
        validateContent(dto.getContent());

        // 创建评论实体
        QuestionComment comment = new QuestionComment();
        comment.setQuestionId(dto.getQuestionId());
        comment.setUserId(userId);
        comment.setParentId(0L); // 顶级评论
        comment.setContent(dto.getContent());
        comment.setThumbNum(0);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setIsDelete(0);

        // 保存到数据库
        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.DATABASE_OPERATION_ERROR, "创建评论失败");
        }
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyComment(ReplyCommentDTO dto, Long userId) {
        // 参数校验
        if (ObjUtil.isEmpty(dto)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请求参数不能为空");
        }
        if (ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        if (ObjUtil.isEmpty(dto.getQuestionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID不能为空");
        }
        if (ObjUtil.isEmpty(dto.getParentId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父评论ID不能为空");
        }

        // 内容校验
        validateContent(dto.getContent());

        // 检查父评论是否存在
        QuestionComment parentComment = this.getById(dto.getParentId());
        if (parentComment == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "父评论不存在");
        }

        // 创建回复评论实体
        QuestionComment comment = new QuestionComment();
        comment.setQuestionId(dto.getQuestionId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setContent(dto.getContent());
        comment.setThumbNum(0);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setIsDelete(0);

        // 保存到数据库
        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.DATABASE_OPERATION_ERROR, "回复评论失败");
        }
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(Long commentId, Long userId, boolean isAdmin) {
        // 参数校验
        if (ObjUtil.isEmpty(commentId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "评论ID不能为空");
        }
        if (ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        // 检查评论是否存在
        QuestionComment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "评论不存在");
        }

        // 权限校验：只有评论作者或管理员可以删除
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限删除该评论");
        }

        // 逻辑删除评论
        return this.removeById(commentId);
    }


    @Override
    public List<QuestionCommentVO> listCommentByQuestionId(Long questionId) {
        // 参数校验
        if (ObjUtil.isEmpty(questionId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "题目ID不能为空");
        }

        // 查询所有评论
        List<QuestionComment> comments = questionCommentMapper.selectCommentsByQuestionId(questionId);
        if (CollUtil.isEmpty(comments)) {
            return new ArrayList<>();
        }

        // 转换为VO
        List<QuestionCommentVO> voList = comments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildCommentTree(voList);
    }

    /**
     * 构建评论树形结构
     * @param voList 评论VO列表
     * @return 树形结构的评论列表
     */
    private List<QuestionCommentVO> buildCommentTree(List<QuestionCommentVO> voList) {
        if (CollUtil.isEmpty(voList)) {
            return new ArrayList<>();
        }

        // 创建ID到VO的映射
        Map<Long, QuestionCommentVO> voMap = new HashMap<>();
        for (QuestionCommentVO vo : voList) {
            vo.setChildren(new ArrayList<>());
            voMap.put(vo.getId(), vo);
        }

        // 构建树形结构
        List<QuestionCommentVO> rootComments = new ArrayList<>();
        for (QuestionCommentVO vo : voList) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L) {
                // 顶级评论
                rootComments.add(vo);
            } else {
                // 子评论，添加到父评论的children中
                QuestionCommentVO parentVo = voMap.get(parentId);
                if (parentVo != null) {
                    parentVo.getChildren().add(vo);
                } else {
                    // 如果父评论不存在（可能被删除），作为顶级评论处理
                    rootComments.add(vo);
                }
            }
        }

        return rootComments;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean thumbComment(Long commentId, Long userId) {
        // 参数校验
        if (ObjUtil.isEmpty(commentId) || ObjUtil.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数不能为空");
        }

        // 检查评论是否存在
        QuestionComment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NO_FOUND, "评论不存在");
        }

        // 查询是否已点赞
        QueryWrapper<QuestionCommentThumb> thumbWrapper = new QueryWrapper<>();
        thumbWrapper.eq("commentId", commentId);
        thumbWrapper.eq("userId", userId);
        QuestionCommentThumb existingThumb = questionCommentThumbMapper.selectOne(thumbWrapper);

        if (existingThumb != null) {
            // 已点赞，取消点赞
            questionCommentThumbMapper.deleteById(existingThumb.getId());
            questionCommentMapper.updateThumbNum(commentId, -1);
            return false;
        } else {
            // 未点赞，添加点赞
            QuestionCommentThumb thumb = new QuestionCommentThumb();
            thumb.setCommentId(commentId);
            thumb.setUserId(userId);
            thumb.setCreateTime(new Date());
            questionCommentThumbMapper.insert(thumb);
            questionCommentMapper.updateThumbNum(commentId, 1);
            return true;
        }
    }

    @Override
    public void validateContent(String content) {
        // 内容不能为空
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }

        // 内容长度校验
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能超过1000字符");
        }
    }

    @Override
    public QuestionCommentVO convertToVO(QuestionComment comment) {
        return convertToVO(comment, null);
    }

    @Override
    public QuestionCommentVO convertToVO(QuestionComment comment, Long userId) {
        if (comment == null) {
            return null;
        }

        QuestionCommentVO vo = new QuestionCommentVO();
        BeanUtil.copyProperties(comment, vo);

        // 设置用户点赞状态
        if (userId != null) {
            QueryWrapper<QuestionCommentThumb> thumbWrapper = new QueryWrapper<>();
            thumbWrapper.eq("commentId", comment.getId());
            thumbWrapper.eq("userId", userId);
            vo.setHasThumb(questionCommentThumbMapper.selectCount(thumbWrapper) > 0);
        } else {
            vo.setHasThumb(false);
        }

        // TODO: 可以通过远程调用获取用户名称和头像
        // vo.setUserName(userService.getUserById(comment.getUserId()).getUserName());
        // vo.setUserAvatar(userService.getUserById(comment.getUserId()).getUserAvatar());
        // if (comment.getReplyUserId() != null) {
        //     vo.setReplyUserName(userService.getUserById(comment.getReplyUserId()).getUserName());
        // }

        return vo;
    }
}
