package com.leot.baguservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.baguservice.domain.pojo.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【question_bank(题库)】的数据库操作Mapper
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

}
