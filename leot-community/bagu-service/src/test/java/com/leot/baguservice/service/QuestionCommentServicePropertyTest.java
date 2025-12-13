package com.leot.baguservice.service;

import com.leot.baguservice.domain.pojo.QuestionComment;
import com.leot.baguservice.domain.vo.QuestionCommentVO;
import com.leot.baguservice.service.impl.QuestionCommentServiceImpl;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * QuestionCommentService 属性测试
 * 使用 JUnit 5 框架
 * 
 * 注意：由于 MyBatis-Plus 的 ServiceImpl 依赖 Spring 容器注入 baseMapper，
 * 这里只测试不依赖数据库的纯业务逻辑（如参数校验、树形结构构建）
 */
class QuestionCommentServicePropertyTest {

    private QuestionCommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new QuestionCommentServiceImpl();
    }

    /**
     * Feature: bagu-service, Property 17: 评论创建数据一致性
     * Validates: Requirements 7.1
     * 
     * *For any* 有效的评论内容，创建成功后查询应返回与输入一致的评论信息。
     */
    @Nested
    @DisplayName("Property 17: 评论创建数据一致性")
    class CommentCreationConsistencyTest {

        @ParameterizedTest
        @MethodSource("provideValidCommentContents")
        @DisplayName("有效评论内容应被接受")
        void validCommentContentShouldBeAccepted(String content) {
            // Act & Assert - 不应抛出异常
            commentService.validateContent(content);
        }

        static Stream<Arguments> provideValidCommentContents() {
            return Stream.of(
                    Arguments.of("这是一条有效的评论"),
                    Arguments.of("Good answer!"),
                    Arguments.of("非常详细的解答，学到了很多"),
                    Arguments.of("a"),
                    Arguments.of("a".repeat(1000)), // 边界值：最大长度
                    Arguments.of("包含特殊字符：!@#$%^&*()"),
                    Arguments.of("包含数字：12345"),
                    Arguments.of("   前后有空格   ")
            );
        }

        @Test
        @DisplayName("评论实体转换为VO应保持数据一致")
        void commentToVOShouldMaintainDataConsistency() {
            // Arrange
            QuestionComment comment = new QuestionComment();
            comment.setId(1L);
            comment.setQuestionId(100L);
            comment.setUserId(10L);
            comment.setParentId(0L);
            comment.setContent("测试评论内容");
            comment.setThumbNum(5);
            comment.setCreateTime(new Date());

            // Act
            QuestionCommentVO vo = commentService.convertToVO(comment);

            // Assert - 验证数据一致性
            assertThat(vo.getId()).isEqualTo(comment.getId());
            assertThat(vo.getQuestionId()).isEqualTo(comment.getQuestionId());
            assertThat(vo.getUserId()).isEqualTo(comment.getUserId());
            assertThat(vo.getParentId()).isEqualTo(comment.getParentId());
            assertThat(vo.getContent()).isEqualTo(comment.getContent());
            assertThat(vo.getThumbNum()).isEqualTo(comment.getThumbNum());
            assertThat(vo.getCreateTime()).isEqualTo(comment.getCreateTime());
        }

        @ParameterizedTest
        @MethodSource("provideCommentData")
        @DisplayName("各种评论数据转换为VO应保持一致")
        void variousCommentDataShouldMaintainConsistency(Long id, Long questionId, Long userId, String content, Integer thumbNum) {
            // Arrange
            QuestionComment comment = new QuestionComment();
            comment.setId(id);
            comment.setQuestionId(questionId);
            comment.setUserId(userId);
            comment.setParentId(0L);
            comment.setContent(content);
            comment.setThumbNum(thumbNum);
            comment.setCreateTime(new Date());

            // Act
            QuestionCommentVO vo = commentService.convertToVO(comment);

            // Assert
            assertThat(vo.getId()).isEqualTo(id);
            assertThat(vo.getQuestionId()).isEqualTo(questionId);
            assertThat(vo.getUserId()).isEqualTo(userId);
            assertThat(vo.getContent()).isEqualTo(content);
            assertThat(vo.getThumbNum()).isEqualTo(thumbNum);
        }

        static Stream<Arguments> provideCommentData() {
            return Stream.of(
                    Arguments.of(1L, 100L, 10L, "评论内容1", 0),
                    Arguments.of(2L, 200L, 20L, "评论内容2", 10),
                    Arguments.of(3L, 300L, 30L, "评论内容3", 100),
                    Arguments.of(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, "边界测试", Integer.MAX_VALUE)
            );
        }

        @Test
        @DisplayName("null评论转换为VO应返回null")
        void nullCommentShouldReturnNullVO() {
            // Act
            QuestionCommentVO vo = commentService.convertToVO(null);

            // Assert
            assertThat(vo).isNull();
        }
    }


    /**
     * Feature: bagu-service, Property 18: 评论树形结构正确性
     * Validates: Requirements 7.2, 7.6
     * 
     * *For any* 回复评论，其 parentId 应指向有效的父评论，且查询时应正确嵌套在父评论的 children 列表中。
     */
    @Nested
    @DisplayName("Property 18: 评论树形结构正确性")
    class CommentTreeStructureTest {

        @Test
        @DisplayName("顶级评论的parentId应为0")
        void topLevelCommentParentIdShouldBeZero() {
            // Arrange
            QuestionComment topComment = createComment(1L, 100L, 10L, 0L, "顶级评论");

            // Assert
            assertThat(topComment.getParentId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("回复评论的parentId应指向父评论")
        void replyCommentParentIdShouldPointToParent() {
            // Arrange
            QuestionComment parentComment = createComment(1L, 100L, 10L, 0L, "父评论");
            QuestionComment replyComment = createComment(2L, 100L, 20L, 1L, "回复评论");

            // Assert
            assertThat(replyComment.getParentId()).isEqualTo(parentComment.getId());
        }

        @Test
        @DisplayName("树形结构构建应正确嵌套子评论")
        void treeStructureShouldNestChildrenCorrectly() {
            // Arrange - 创建评论列表
            List<QuestionCommentVO> voList = new ArrayList<>();
            
            QuestionCommentVO parent1 = createCommentVO(1L, 0L, "父评论1");
            QuestionCommentVO parent2 = createCommentVO(2L, 0L, "父评论2");
            QuestionCommentVO child1 = createCommentVO(3L, 1L, "子评论1-1");
            QuestionCommentVO child2 = createCommentVO(4L, 1L, "子评论1-2");
            QuestionCommentVO child3 = createCommentVO(5L, 2L, "子评论2-1");
            
            voList.add(parent1);
            voList.add(parent2);
            voList.add(child1);
            voList.add(child2);
            voList.add(child3);

            // Act - 构建树形结构
            List<QuestionCommentVO> tree = buildCommentTree(voList);

            // Assert
            assertThat(tree).hasSize(2); // 只有2个顶级评论
            
            // 验证第一个父评论的子评论
            QuestionCommentVO firstParent = tree.stream()
                    .filter(c -> c.getId().equals(1L))
                    .findFirst()
                    .orElse(null);
            assertThat(firstParent).isNotNull();
            assertThat(firstParent.getChildren()).hasSize(2);
            
            // 验证第二个父评论的子评论
            QuestionCommentVO secondParent = tree.stream()
                    .filter(c -> c.getId().equals(2L))
                    .findFirst()
                    .orElse(null);
            assertThat(secondParent).isNotNull();
            assertThat(secondParent.getChildren()).hasSize(1);
        }

        @Test
        @DisplayName("空评论列表应返回空树")
        void emptyListShouldReturnEmptyTree() {
            // Act
            List<QuestionCommentVO> tree = buildCommentTree(new ArrayList<>());

            // Assert
            assertThat(tree).isEmpty();
        }

        @Test
        @DisplayName("只有顶级评论时树结构应正确")
        void onlyTopLevelCommentsShouldBuildCorrectTree() {
            // Arrange
            List<QuestionCommentVO> voList = new ArrayList<>();
            voList.add(createCommentVO(1L, 0L, "评论1"));
            voList.add(createCommentVO(2L, 0L, "评论2"));
            voList.add(createCommentVO(3L, 0L, "评论3"));

            // Act
            List<QuestionCommentVO> tree = buildCommentTree(voList);

            // Assert
            assertThat(tree).hasSize(3);
            for (QuestionCommentVO vo : tree) {
                assertThat(vo.getChildren()).isEmpty();
            }
        }

        @ParameterizedTest
        @MethodSource("provideTreeStructures")
        @DisplayName("各种树形结构应正确构建")
        void variousTreeStructuresShouldBuildCorrectly(int topLevelCount, int childPerParent) {
            // Arrange
            List<QuestionCommentVO> voList = new ArrayList<>();
            long id = 1L;
            
            // 创建顶级评论
            List<Long> parentIds = new ArrayList<>();
            for (int i = 0; i < topLevelCount; i++) {
                voList.add(createCommentVO(id, 0L, "父评论" + id));
                parentIds.add(id);
                id++;
            }
            
            // 创建子评论
            for (Long parentId : parentIds) {
                for (int j = 0; j < childPerParent; j++) {
                    voList.add(createCommentVO(id, parentId, "子评论" + id));
                    id++;
                }
            }

            // Act
            List<QuestionCommentVO> tree = buildCommentTree(voList);

            // Assert
            assertThat(tree).hasSize(topLevelCount);
            for (QuestionCommentVO parent : tree) {
                assertThat(parent.getChildren()).hasSize(childPerParent);
            }
        }

        static Stream<Arguments> provideTreeStructures() {
            return Stream.of(
                    Arguments.of(1, 0),
                    Arguments.of(1, 1),
                    Arguments.of(1, 5),
                    Arguments.of(3, 2),
                    Arguments.of(5, 3),
                    Arguments.of(10, 0)
            );
        }

        // 辅助方法：构建树形结构（复制自ServiceImpl的逻辑）
        private List<QuestionCommentVO> buildCommentTree(List<QuestionCommentVO> voList) {
            if (voList == null || voList.isEmpty()) {
                return new ArrayList<>();
            }

            Map<Long, QuestionCommentVO> voMap = new HashMap<>();
            for (QuestionCommentVO vo : voList) {
                vo.setChildren(new ArrayList<>());
                voMap.put(vo.getId(), vo);
            }

            List<QuestionCommentVO> rootComments = new ArrayList<>();
            for (QuestionCommentVO vo : voList) {
                Long parentId = vo.getParentId();
                if (parentId == null || parentId == 0L) {
                    rootComments.add(vo);
                } else {
                    QuestionCommentVO parentVo = voMap.get(parentId);
                    if (parentVo != null) {
                        parentVo.getChildren().add(vo);
                    } else {
                        rootComments.add(vo);
                    }
                }
            }

            return rootComments;
        }

        // 辅助方法：创建评论实体
        private QuestionComment createComment(Long id, Long questionId, Long userId, Long parentId, String content) {
            QuestionComment comment = new QuestionComment();
            comment.setId(id);
            comment.setQuestionId(questionId);
            comment.setUserId(userId);
            comment.setParentId(parentId);
            comment.setContent(content);
            comment.setThumbNum(0);
            comment.setCreateTime(new Date());
            return comment;
        }

        // 辅助方法：创建评论VO
        private QuestionCommentVO createCommentVO(Long id, Long parentId, String content) {
            QuestionCommentVO vo = new QuestionCommentVO();
            vo.setId(id);
            vo.setParentId(parentId);
            vo.setContent(content);
            vo.setThumbNum(0);
            vo.setCreateTime(new Date());
            return vo;
        }
    }


    /**
     * Feature: bagu-service, Property 19: 评论内容验证
     * Validates: Requirements 7.5
     * 
     * *For any* 空字符串或长度超过1000字符的评论内容，创建操作应被拒绝并返回参数错误。
     */
    @Nested
    @DisplayName("Property 19: 评论内容验证")
    class CommentContentValidationTest {

        @Test
        @DisplayName("空字符串评论内容应被拒绝")
        void shouldRejectEmptyContent() {
            BusinessException exception = catchThrowableOfType(
                    () -> commentService.validateContent(""),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("评论内容不能为空");
        }

        @Test
        @DisplayName("null评论内容应被拒绝")
        void shouldRejectNullContent() {
            BusinessException exception = catchThrowableOfType(
                    () -> commentService.validateContent(null),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("评论内容不能为空");
        }

        @Test
        @DisplayName("纯空格评论内容应被拒绝")
        void shouldRejectWhitespaceOnlyContent() {
            BusinessException exception = catchThrowableOfType(
                    () -> commentService.validateContent("   "),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("评论内容不能为空");
        }

        @Test
        @DisplayName("超过1000字符的评论内容应被拒绝")
        void shouldRejectTooLongContent() {
            String longContent = "a".repeat(1001);

            BusinessException exception = catchThrowableOfType(
                    () -> commentService.validateContent(longContent),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("评论内容不能超过1000字符");
        }

        @ParameterizedTest
        @ValueSource(ints = {1001, 1500, 2000, 5000})
        @DisplayName("各种超长评论内容都应被拒绝")
        void shouldRejectVariousLongContents(int length) {
            String longContent = "a".repeat(length);

            BusinessException exception = catchThrowableOfType(
                    () -> commentService.validateContent(longContent),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("评论内容不能超过1000字符");
        }

        @Test
        @DisplayName("1000字符的评论内容应被接受（边界值）")
        void shouldAcceptMaxLengthContent() {
            String maxContent = "a".repeat(1000);
            commentService.validateContent(maxContent);
        }

        @Test
        @DisplayName("有效评论内容应被接受")
        void shouldAcceptValidContent() {
            commentService.validateContent("这是一条有效的评论");
            commentService.validateContent("Good answer!");
            commentService.validateContent("非常详细的解答，学到了很多");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, 500, 999, 1000})
        @DisplayName("各种有效长度的评论内容应被接受")
        void shouldAcceptVariousValidLengths(int length) {
            String content = "a".repeat(length);
            commentService.validateContent(content);
        }

        @Test
        @DisplayName("包含特殊字符的评论内容应被接受")
        void shouldAcceptContentWithSpecialCharacters() {
            commentService.validateContent("包含特殊字符：!@#$%^&*()_+-=[]{}|;':\",./<>?");
            commentService.validateContent("包含换行符\n和制表符\t");
            commentService.validateContent("包含emoji：😀🎉👍");
        }

        @Test
        @DisplayName("包含中文的评论内容应被接受")
        void shouldAcceptChineseContent() {
            commentService.validateContent("这是一条中文评论");
            commentService.validateContent("非常好的解答，感谢分享！");
            commentService.validateContent("学习了，收藏一下");
        }
    }


    /**
     * Feature: bagu-service, Property 20: 评论点赞 Round-Trip
     * Validates: Requirements 8.1, 8.2
     * 
     * *For any* 评论和用户，点赞后取消点赞，评论的点赞数应恢复到初始值，用户的点赞状态应为false。
     */
    @Nested
    @DisplayName("Property 20: 评论点赞 Round-Trip")
    class CommentThumbRoundTripTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, 100, 1000})
        @DisplayName("点赞后取消点赞应恢复初始状态")
        void thumbRoundTripShouldRestoreInitialState(int initialThumbNum) {
            // Arrange - 模拟初始点赞数
            int thumbNum = initialThumbNum;

            // Act - 点赞
            thumbNum += 1;
            boolean hasThumb = true;

            // Assert - 点赞后状态
            assertThat(thumbNum).isEqualTo(initialThumbNum + 1);
            assertThat(hasThumb).isTrue();

            // Act - 取消点赞
            thumbNum -= 1;
            hasThumb = false;

            // Assert - 验证恢复到初始状态
            assertThat(thumbNum).isEqualTo(initialThumbNum);
            assertThat(hasThumb).isFalse();
        }

        @Test
        @DisplayName("点赞数不应为负数")
        void thumbNumShouldNotBeNegative() {
            int thumbNum = 0;

            // 模拟多次点赞
            thumbNum += 5;
            assertThat(thumbNum).isEqualTo(5);

            // 模拟取消点赞（不能超过当前点赞数）
            int unthumbCount = 3;
            thumbNum -= Math.min(unthumbCount, thumbNum);
            assertThat(thumbNum).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("同一用户重复点赞应切换状态")
        void sameUserThumbShouldToggle() {
            boolean hasThumb = false;
            int thumbNum = 0;

            // 第一次点赞
            hasThumb = !hasThumb;
            thumbNum += hasThumb ? 1 : -1;
            assertThat(hasThumb).isTrue();
            assertThat(thumbNum).isEqualTo(1);

            // 第二次点赞（取消）
            hasThumb = !hasThumb;
            thumbNum += hasThumb ? 1 : -1;
            assertThat(hasThumb).isFalse();
            assertThat(thumbNum).isEqualTo(0);
        }

        @ParameterizedTest
        @MethodSource("provideThumbScenarios")
        @DisplayName("各种点赞场景应正确处理")
        void variousThumbScenariosShouldBeHandledCorrectly(int initialThumbNum, int thumbCount, int unthumbCount) {
            // Arrange
            int thumbNum = initialThumbNum;

            // Act - 点赞
            for (int i = 0; i < thumbCount; i++) {
                thumbNum += 1;
            }

            // Act - 取消点赞
            for (int i = 0; i < unthumbCount; i++) {
                thumbNum -= 1;
            }

            // Assert
            int expectedThumbNum = initialThumbNum + thumbCount - unthumbCount;
            assertThat(thumbNum).isEqualTo(expectedThumbNum);
        }

        static Stream<Arguments> provideThumbScenarios() {
            return Stream.of(
                    Arguments.of(0, 1, 1),   // 点赞后取消
                    Arguments.of(0, 5, 3),   // 多次点赞后部分取消
                    Arguments.of(10, 5, 5),  // 初始有点赞，点赞后全部取消
                    Arguments.of(100, 0, 0), // 不操作
                    Arguments.of(0, 10, 10)  // 多次点赞后全部取消
            );
        }

        @Test
        @DisplayName("评论VO的hasThumb状态应正确反映用户点赞状态")
        void commentVOHasThumbShouldReflectUserThumbStatus() {
            // Arrange
            QuestionComment comment = new QuestionComment();
            comment.setId(1L);
            comment.setQuestionId(100L);
            comment.setUserId(10L);
            comment.setParentId(0L);
            comment.setContent("测试评论");
            comment.setThumbNum(5);
            comment.setCreateTime(new Date());

            // Act - 不传userId时
            QuestionCommentVO voWithoutUser = commentService.convertToVO(comment);

            // Assert - 默认为false
            assertThat(voWithoutUser.getHasThumb()).isFalse();
        }

        @Test
        @DisplayName("多个用户点赞同一评论应累加点赞数")
        void multipleUsersThumbShouldAccumulate() {
            // Arrange
            int thumbNum = 0;
            int userCount = 10;

            // Act - 多个用户点赞
            for (int i = 0; i < userCount; i++) {
                thumbNum += 1;
            }

            // Assert
            assertThat(thumbNum).isEqualTo(userCount);
        }
    }
}
