package com.leot.baguservice.service;

import com.leot.baguservice.domain.dto.AddQuestionBankDTO;
import com.leot.baguservice.domain.dto.UpdateQuestionBankDTO;
import com.leot.baguservice.service.impl.QuestionBankServiceImpl;
import com.leot.leotcommon.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * QuestionBankService 单元测试
 * 使用 JUnit 5 框架
 * 
 * 注意：由于 MyBatis-Plus 的 ServiceImpl 依赖 Spring 容器注入 baseMapper，
 * 这里只测试不依赖数据库的纯业务逻辑（如参数校验）
 */
class QuestionBankServicePropertyTest {

    private QuestionBankServiceImpl questionBankService;

    @BeforeEach
    void setUp() {
        questionBankService = new QuestionBankServiceImpl();
    }

    /**
     * Property 4: 标题验证拒绝无效输入
     * Validates: Requirements 1.4
     * 
     * 测试 validateTitle 方法的校验逻辑
     */
    @Nested
    @DisplayName("Property 4: 标题验证拒绝无效输入")
    class TitleValidationTest {

        @Test
        @DisplayName("空字符串标题应被拒绝")
        void shouldRejectEmptyTitle() {
            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle("", true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("null标题应被拒绝")
        void shouldRejectNullTitle() {
            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle(null, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("纯空格标题应被拒绝")
        void shouldRejectWhitespaceOnlyTitle() {
            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle("   ", true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("超过256字符的标题应被拒绝")
        void shouldRejectTooLongTitle() {
            // Arrange
            String longTitle = "a".repeat(257);

            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle(longTitle, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }

        @ParameterizedTest
        @ValueSource(ints = {257, 300, 500, 1000})
        @DisplayName("各种超长标题都应被拒绝")
        void shouldRejectVariousLongTitles(int length) {
            // Arrange
            String longTitle = "a".repeat(length);

            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle(longTitle, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }

        @Test
        @DisplayName("更新时超长标题也应被拒绝")
        void shouldRejectTooLongTitleOnUpdate() {
            // Arrange
            String longTitle = "a".repeat(257);

            // Act & Assert - isAdd = false 表示更新操作
            BusinessException exception = catchThrowableOfType(
                    () -> questionBankService.validateTitle(longTitle, false),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }

        @Test
        @DisplayName("256字符的标题应被接受（边界值）")
        void shouldAcceptMaxLengthTitle() {
            // Arrange
            String maxTitle = "a".repeat(256);

            // Act & Assert - 不应抛出异常
            questionBankService.validateTitle(maxTitle, true);
            // 如果没有抛出异常，测试通过
        }

        @Test
        @DisplayName("有效标题应被接受")
        void shouldAcceptValidTitle() {
            // Act & Assert - 不应抛出异常
            questionBankService.validateTitle("Java面试题库", true);
            questionBankService.validateTitle("Spring Boot 入门", true);
            questionBankService.validateTitle("算法与数据结构", true);
        }

        @Test
        @DisplayName("更新时空标题应被接受（允许不修改标题）")
        void shouldAcceptEmptyTitleOnUpdate() {
            // Act & Assert - 更新时空标题不应抛出异常
            questionBankService.validateTitle("", false);
            questionBankService.validateTitle(null, false);
        }
    }
}
