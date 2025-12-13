package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.QuestionThumb;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question_thumb(题目点赞)】的数据库操作Mapper
 */
@Mapper
public interface QuestionThumbMapper extends BaseMapper<QuestionThumb> {

}
