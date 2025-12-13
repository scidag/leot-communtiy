package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.QuestionCommentThumb;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question_comment_thumb(评论点赞)】的数据库操作Mapper
 */
@Mapper
public interface QuestionCommentThumbMapper extends BaseMapper<QuestionCommentThumb> {

}
