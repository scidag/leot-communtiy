package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.QuestionFavour;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question_favour(题目收藏)】的数据库操作Mapper
 */
@Mapper
public interface QuestionFavourMapper extends BaseMapper<QuestionFavour> {

}
