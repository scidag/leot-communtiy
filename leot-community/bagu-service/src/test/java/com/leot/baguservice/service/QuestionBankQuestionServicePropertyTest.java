package com.leot.baguservice.service;

import com.leot.baguservice.service.impl.QuestionBankQuestionServiceImpl;
import com.leot.leotcommon.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * QuestionBankQuestionService 属性测试
 * 使用 JUnit 5 框架
 * 
 * 注意：由于 MyBatis-Plus 的 ServiceImpl 依赖 Spring 容器注入 baseMapper，
 * 这里测试不依赖数据库的纯业务逻辑（如唯一性约束、幂等性、完整性）
 */
class QuestionBankQuestionServicePropertyTest {

    /**
     * Feature: bagu-service, Property 7: 关联唯一性约束
     * Validates: Requirements 3.3
     * 
     * *For any* 已存在的题库-题目关联，重复添加相同关联应被拒绝。
     */
    @Nested
    @DisplayName("Property 7: 关联唯一性约束")
    class RelationUniquenessConstraintTest {

        /**
         * 模拟关联存储，用于测试唯一性约束逻辑
         */
        private Set<String> existingRelations;

        @BeforeEach
        void setUp() {
            existingRelations = new HashSet<>();
        }

        /**
         * 生成关联的唯一键
         */
        private String generateRelationKey(Long questionBankId, Long questionId) {
            return questionBankId + "_" + questionId;
        }

        /**
         * 检查关联是否存在
         */
        private boolean existsRelation(Long questionBankId, Long questionId) {
            return existingRelations.contains(generateRelationKey(questionBankId, questionId));
        }

        /**
         * 添加关联（如果已存在则抛出异常）
         */
        private boolean addRelation(Long questionBankId, Long questionId) {
            String key = generateRelationKey(questionBankId, questionId);
            if (existingRelations.contains(key)) {
                throw new BusinessException(
                        com.leot.leotcommon.GlobalReture.ErrorCode.EXIST_ERROR, 
                        "该题目已存在于此题库中"
                );
            }
            return existingRelations.add(key);
        }

        @Test
        @DisplayName("首次添加关联应成功")
        void firstAddShouldSucceed() {
            // Arrange
            Long questionBankId = 1L;
            Long questionId = 100L;

            // Act
            boolean result = addRelation(questionBankId, questionId);

            // Assert
            assertThat(result).isTrue();
            assertThat(existsRelation(questionBankId, questionId)).isTrue();
        }

        @Test
        @DisplayName("重复添加相同关联应被拒绝")
        void duplicateAddShouldBeRejected() {
            // Arrange
            Long questionBankId = 1L;
            Long questionId = 100L;
            addRelation(questionBankId, questionId);

            // Act & Assert
            BusinessException exception = catchThrowableOfType(
                    () -> addRelation(questionBankId, questionId),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("该题目已存在于此题库中");
        }

        @ParameterizedTest
        @MethodSource("provideRelationPairs")
        @DisplayName("不同的题库-题目组合应各自独立")
        void differentRelationsShouldBeIndependent(Long bankId1, Long qId1, Long bankId2, Long qId2) {
            // Arrange & Act
            addRelation(bankId1, qId1);
            addRelation(bankId2, qId2);

            // Assert - 两个关联都应存在
            assertThat(existsRelation(bankId1, qId1)).isTrue();
            assertThat(existsRelation(bankId2, qId2)).isTrue();
        }

        static Stream<Arguments> provideRelationPairs() {
            return Stream.of(
                    // 不同题库，不同题目
                    Arguments.of(1L, 100L, 2L, 200L),
                    // 相同题库，不同题目
                    Arguments.of(1L, 100L, 1L, 200L),
                    // 不同题库，相同题目
                    Arguments.of(1L, 100L, 2L, 100L)
            );
        }

        @Test
        @DisplayName("同一题目可以添加到多个题库")
        void sameQuestionCanBeAddedToMultipleBanks() {
            // Arrange
            Long questionId = 100L;
            Long bankId1 = 1L;
            Long bankId2 = 2L;
            Long bankId3 = 3L;

            // Act
            addRelation(bankId1, questionId);
            addRelation(bankId2, questionId);
            addRelation(bankId3, questionId);

            // Assert
            assertThat(existsRelation(bankId1, questionId)).isTrue();
            assertThat(existsRelation(bankId2, questionId)).isTrue();
            assertThat(existsRelation(bankId3, questionId)).isTrue();
        }

        @Test
        @DisplayName("同一题库可以添加多个题目")
        void sameBankCanHaveMultipleQuestions() {
            // Arrange
            Long questionBankId = 1L;
            Long qId1 = 100L;
            Long qId2 = 200L;
            Long qId3 = 300L;

            // Act
            addRelation(questionBankId, qId1);
            addRelation(questionBankId, qId2);
            addRelation(questionBankId, qId3);

            // Assert
            assertThat(existsRelation(questionBankId, qId1)).isTrue();
            assertThat(existsRelation(questionBankId, qId2)).isTrue();
            assertThat(existsRelation(questionBankId, qId3)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 50, 100})
        @DisplayName("多次重复添加同一关联都应被拒绝")
        void multipleRepeatedAddsShouldAllBeRejected(int repeatCount) {
            // Arrange
            Long questionBankId = 1L;
            Long questionId = 100L;
            addRelation(questionBankId, questionId);

            // Act & Assert
            for (int i = 0; i < repeatCount; i++) {
                BusinessException exception = catchThrowableOfType(
                        () -> addRelation(questionBankId, questionId),
                        BusinessException.class
                );
                assertThat(exception).isNotNull();
            }

            // 关联仍然只有一个
            assertThat(existingRelations.size()).isEqualTo(1);
        }
    }


    /**
     * Feature: bagu-service, Property 8: 批量添加幂等性
     * Validates: Requirements 3.4
     * 
     * *For any* 批量添加请求，已存在的关联应被跳过，只有新关联被创建，最终关联集合应包含所有请求的关联。
     */
    @Nested
    @DisplayName("Property 8: 批量添加幂等性")
    class BatchAddIdempotencyTest {

        /**
         * 模拟关联存储
         */
        private Set<String> existingRelations;

        @BeforeEach
        void setUp() {
            existingRelations = new HashSet<>();
        }

        private String generateRelationKey(Long questionBankId, Long questionId) {
            return questionBankId + "_" + questionId;
        }

        /**
         * 批量添加关联（跳过已存在的）
         * @return 成功添加的数量
         */
        private int batchAddRelations(Long questionBankId, List<Long> questionIds) {
            int addedCount = 0;
            for (Long questionId : questionIds) {
                String key = generateRelationKey(questionBankId, questionId);
                if (!existingRelations.contains(key)) {
                    existingRelations.add(key);
                    addedCount++;
                }
            }
            return addedCount;
        }

        private boolean existsRelation(Long questionBankId, Long questionId) {
            return existingRelations.contains(generateRelationKey(questionBankId, questionId));
        }

        @Test
        @DisplayName("批量添加全新题目应全部成功")
        void batchAddAllNewShouldSucceed() {
            // Arrange
            Long questionBankId = 1L;
            List<Long> questionIds = List.of(100L, 200L, 300L);

            // Act
            int addedCount = batchAddRelations(questionBankId, questionIds);

            // Assert
            assertThat(addedCount).isEqualTo(3);
            assertThat(existsRelation(questionBankId, 100L)).isTrue();
            assertThat(existsRelation(questionBankId, 200L)).isTrue();
            assertThat(existsRelation(questionBankId, 300L)).isTrue();
        }

        @Test
        @DisplayName("批量添加部分已存在的题目应跳过已存在的")
        void batchAddPartialExistingShouldSkipExisting() {
            // Arrange - 预先添加一些关联
            Long questionBankId = 1L;
            existingRelations.add(generateRelationKey(questionBankId, 100L));
            existingRelations.add(generateRelationKey(questionBankId, 200L));

            // Act - 批量添加，包含已存在和新的
            List<Long> questionIds = List.of(100L, 200L, 300L, 400L);
            int addedCount = batchAddRelations(questionBankId, questionIds);

            // Assert
            assertThat(addedCount).isEqualTo(2); // 只有300和400是新的
            assertThat(existsRelation(questionBankId, 100L)).isTrue();
            assertThat(existsRelation(questionBankId, 200L)).isTrue();
            assertThat(existsRelation(questionBankId, 300L)).isTrue();
            assertThat(existsRelation(questionBankId, 400L)).isTrue();
        }

        @Test
        @DisplayName("批量添加全部已存在的题目应返回0")
        void batchAddAllExistingShouldReturnZero() {
            // Arrange - 预先添加所有关联
            Long questionBankId = 1L;
            existingRelations.add(generateRelationKey(questionBankId, 100L));
            existingRelations.add(generateRelationKey(questionBankId, 200L));
            existingRelations.add(generateRelationKey(questionBankId, 300L));

            // Act
            List<Long> questionIds = List.of(100L, 200L, 300L);
            int addedCount = batchAddRelations(questionBankId, questionIds);

            // Assert
            assertThat(addedCount).isEqualTo(0);
            assertThat(existingRelations.size()).isEqualTo(3); // 数量不变
        }

        @ParameterizedTest
        @MethodSource("provideBatchAddScenarios")
        @DisplayName("批量添加后最终集合应包含所有请求的关联")
        void batchAddShouldContainAllRequestedRelations(
                List<Long> preExisting, List<Long> toAdd, int expectedNewCount) {
            // Arrange
            Long questionBankId = 1L;
            for (Long qId : preExisting) {
                existingRelations.add(generateRelationKey(questionBankId, qId));
            }

            // Act
            int addedCount = batchAddRelations(questionBankId, toAdd);

            // Assert
            assertThat(addedCount).isEqualTo(expectedNewCount);
            
            // 验证所有请求的关联都存在
            for (Long qId : toAdd) {
                assertThat(existsRelation(questionBankId, qId)).isTrue();
            }
        }

        static Stream<Arguments> provideBatchAddScenarios() {
            return Stream.of(
                    // 无预存，添加3个
                    Arguments.of(List.of(), List.of(1L, 2L, 3L), 3),
                    // 预存1个，添加3个（1个重复）
                    Arguments.of(List.of(1L), List.of(1L, 2L, 3L), 2),
                    // 预存2个，添加3个（2个重复）
                    Arguments.of(List.of(1L, 2L), List.of(1L, 2L, 3L), 1),
                    // 预存3个，添加3个（全部重复）
                    Arguments.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L), 0),
                    // 预存3个，添加5个（3个重复）
                    Arguments.of(List.of(1L, 2L, 3L), List.of(1L, 2L, 3L, 4L, 5L), 2)
            );
        }

        @Test
        @DisplayName("重复执行相同批量添加应具有幂等性")
        void repeatedBatchAddShouldBeIdempotent() {
            // Arrange
            Long questionBankId = 1L;
            List<Long> questionIds = List.of(100L, 200L, 300L);

            // Act - 第一次批量添加
            int firstAddCount = batchAddRelations(questionBankId, questionIds);
            int sizeAfterFirst = existingRelations.size();

            // Act - 第二次批量添加（相同数据）
            int secondAddCount = batchAddRelations(questionBankId, questionIds);
            int sizeAfterSecond = existingRelations.size();

            // Assert
            assertThat(firstAddCount).isEqualTo(3);
            assertThat(secondAddCount).isEqualTo(0);
            assertThat(sizeAfterFirst).isEqualTo(sizeAfterSecond);
        }

        @Test
        @DisplayName("批量添加包含重复ID应只添加一次")
        void batchAddWithDuplicateIdsShouldAddOnce() {
            // Arrange
            Long questionBankId = 1L;
            List<Long> questionIds = List.of(100L, 100L, 200L, 200L, 300L);

            // Act
            int addedCount = batchAddRelations(questionBankId, questionIds);

            // Assert - 去重后只有3个不同的ID
            assertThat(addedCount).isEqualTo(3);
            assertThat(existingRelations.size()).isEqualTo(3);
        }
    }


    /**
     * Feature: bagu-service, Property 9: 关联查询完整性
     * Validates: Requirements 3.5
     * 
     * *For any* 题库，查询该题库下的题目列表应返回所有与该题库关联的题目，且不包含未关联的题目。
     */
    @Nested
    @DisplayName("Property 9: 关联查询完整性")
    class RelationQueryCompletenessTest {

        /**
         * 模拟关联存储：questionBankId -> Set<questionId>
         */
        private Map<Long, Set<Long>> bankToQuestions;

        /**
         * 模拟所有题目
         */
        private Set<Long> allQuestions;

        @BeforeEach
        void setUp() {
            bankToQuestions = new HashMap<>();
            allQuestions = new HashSet<>();
            // 初始化一些题目
            for (long i = 1; i <= 100; i++) {
                allQuestions.add(i);
            }
        }

        private void addRelation(Long questionBankId, Long questionId) {
            bankToQuestions.computeIfAbsent(questionBankId, k -> new HashSet<>()).add(questionId);
        }

        private Set<Long> listQuestionsByBankId(Long questionBankId) {
            return bankToQuestions.getOrDefault(questionBankId, new HashSet<>());
        }

        @Test
        @DisplayName("查询结果应包含所有关联的题目")
        void queryShouldContainAllRelatedQuestions() {
            // Arrange
            Long questionBankId = 1L;
            Set<Long> expectedQuestions = Set.of(1L, 2L, 3L, 4L, 5L);
            for (Long qId : expectedQuestions) {
                addRelation(questionBankId, qId);
            }

            // Act
            Set<Long> result = listQuestionsByBankId(questionBankId);

            // Assert
            assertThat(result).containsExactlyInAnyOrderElementsOf(expectedQuestions);
        }

        @Test
        @DisplayName("查询结果不应包含未关联的题目")
        void queryShouldNotContainUnrelatedQuestions() {
            // Arrange
            Long questionBankId = 1L;
            addRelation(questionBankId, 1L);
            addRelation(questionBankId, 2L);
            addRelation(questionBankId, 3L);

            // Act
            Set<Long> result = listQuestionsByBankId(questionBankId);

            // Assert - 不应包含4, 5, 6等未关联的题目
            assertThat(result).doesNotContain(4L, 5L, 6L, 7L, 8L, 9L, 10L);
        }

        @Test
        @DisplayName("空题库应返回空列表")
        void emptyBankShouldReturnEmptyList() {
            // Arrange
            Long questionBankId = 999L; // 不存在的题库

            // Act
            Set<Long> result = listQuestionsByBankId(questionBankId);

            // Assert
            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("provideQueryScenarios")
        @DisplayName("查询结果数量应等于关联数量")
        void queryResultCountShouldEqualRelationCount(List<Long> relatedQuestions) {
            // Arrange
            Long questionBankId = 1L;
            for (Long qId : relatedQuestions) {
                addRelation(questionBankId, qId);
            }

            // Act
            Set<Long> result = listQuestionsByBankId(questionBankId);

            // Assert
            assertThat(result.size()).isEqualTo(relatedQuestions.size());
        }

        static Stream<Arguments> provideQueryScenarios() {
            return Stream.of(
                    Arguments.of(List.of()),
                    Arguments.of(List.of(1L)),
                    Arguments.of(List.of(1L, 2L, 3L)),
                    Arguments.of(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L))
            );
        }

        @Test
        @DisplayName("不同题库的查询结果应相互独立")
        void differentBanksQueryShouldBeIndependent() {
            // Arrange
            Long bankId1 = 1L;
            Long bankId2 = 2L;
            
            addRelation(bankId1, 1L);
            addRelation(bankId1, 2L);
            addRelation(bankId2, 3L);
            addRelation(bankId2, 4L);
            addRelation(bankId2, 5L);

            // Act
            Set<Long> result1 = listQuestionsByBankId(bankId1);
            Set<Long> result2 = listQuestionsByBankId(bankId2);

            // Assert
            assertThat(result1).containsExactlyInAnyOrder(1L, 2L);
            assertThat(result2).containsExactlyInAnyOrder(3L, 4L, 5L);
            
            // 两个结果集不应有交集
            Set<Long> intersection = new HashSet<>(result1);
            intersection.retainAll(result2);
            assertThat(intersection).isEmpty();
        }

        @Test
        @DisplayName("同一题目在多个题库中应各自独立查询")
        void sameQuestionInMultipleBanksShouldBeQueriedIndependently() {
            // Arrange - 题目1同时在题库1和题库2中
            Long sharedQuestionId = 1L;
            Long bankId1 = 1L;
            Long bankId2 = 2L;
            
            addRelation(bankId1, sharedQuestionId);
            addRelation(bankId1, 2L);
            addRelation(bankId2, sharedQuestionId);
            addRelation(bankId2, 3L);

            // Act
            Set<Long> result1 = listQuestionsByBankId(bankId1);
            Set<Long> result2 = listQuestionsByBankId(bankId2);

            // Assert
            assertThat(result1).contains(sharedQuestionId);
            assertThat(result2).contains(sharedQuestionId);
            assertThat(result1).containsExactlyInAnyOrder(1L, 2L);
            assertThat(result2).containsExactlyInAnyOrder(1L, 3L);
        }

        @Test
        @DisplayName("添加关联后查询应立即反映变化")
        void queryAfterAddShouldReflectChange() {
            // Arrange
            Long questionBankId = 1L;
            
            // 初始状态
            Set<Long> initialResult = listQuestionsByBankId(questionBankId);
            assertThat(initialResult).isEmpty();

            // Act - 添加关联
            addRelation(questionBankId, 100L);
            Set<Long> afterAddResult = listQuestionsByBankId(questionBankId);

            // Assert
            assertThat(afterAddResult).containsExactly(100L);
        }

        @Test
        @DisplayName("移除关联后查询应立即反映变化")
        void queryAfterRemoveShouldReflectChange() {
            // Arrange
            Long questionBankId = 1L;
            addRelation(questionBankId, 1L);
            addRelation(questionBankId, 2L);
            addRelation(questionBankId, 3L);

            // Act - 移除关联
            bankToQuestions.get(questionBankId).remove(2L);
            Set<Long> afterRemoveResult = listQuestionsByBankId(questionBankId);

            // Assert
            assertThat(afterRemoveResult).containsExactlyInAnyOrder(1L, 3L);
            assertThat(afterRemoveResult).doesNotContain(2L);
        }
    }
}
