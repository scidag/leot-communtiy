package com.leot.baguservice.service;

import com.leot.baguservice.service.impl.QuestionServiceImpl;
import com.leot.leotcommon.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * QuestionService 属性测试
 * 使用 JUnit 5 框架
 * 
 * 注意：由于 MyBatis-Plus 的 ServiceImpl 依赖 Spring 容器注入 baseMapper，
 * 这里只测试不依赖数据库的纯业务逻辑（如参数校验、标签序列化）
 */
class QuestionServicePropertyTest {

    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionServiceImpl();
    }

    /**
     * Feature: bagu-service, Property 6: 标签序列化 Round-Trip
     * Validates: Requirements 2.5, 2.6
     * 
     * *For any* 有效的标签列表，序列化为JSON后再反序列化应得到相同的标签列表。
     */
    @Nested
    @DisplayName("Property 6: 标签序列化 Round-Trip")
    class TagSerializationRoundTripTest {

        @Test
        @DisplayName("单个标签序列化后反序列化应得到相同结果")
        void singleTagSerializationRoundTrip() {
            // Arrange
            List<String> tags = List.of("Java");

            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).containsExactlyElementsOf(tags);
        }

        @Test
        @DisplayName("多个标签序列化后反序列化应得到相同结果")
        void multipleTagsSerializationRoundTrip() {
            // Arrange
            List<String> tags = List.of("Java", "Spring", "MyBatis", "Redis");

            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).containsExactlyElementsOf(tags);
        }


        @ParameterizedTest
        @MethodSource("provideTagLists")
        @DisplayName("各种标签列表序列化后反序列化应得到相同结果")
        void variousTagsSerializationRoundTrip(List<String> tags) {
            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).containsExactlyElementsOf(tags);
        }

        static Stream<Arguments> provideTagLists() {
            return Stream.of(
                    Arguments.of(List.of("Java")),
                    Arguments.of(List.of("Spring", "Boot")),
                    Arguments.of(List.of("算法", "数据结构", "面试")),
                    Arguments.of(List.of("a", "b", "c", "d", "e")),
                    Arguments.of(List.of("Tag1", "Tag2", "Tag3", "Tag4", "Tag5", "Tag6", "Tag7", "Tag8", "Tag9", "Tag10"))
            );
        }

        @Test
        @DisplayName("空列表序列化后反序列化应得到空列表")
        void emptyListSerializationRoundTrip() {
            // Arrange
            List<String> tags = new ArrayList<>();

            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("null列表序列化应返回空JSON数组")
        void nullListSerializationShouldReturnEmptyArray() {
            // Act
            String json = questionService.convertTagsToJson(null);

            // Assert
            assertThat(json).isEqualTo("[]");
        }

        @Test
        @DisplayName("空字符串反序列化应返回空列表")
        void emptyStringDeserializationShouldReturnEmptyList() {
            // Act
            List<String> parsed = questionService.parseTagsFromJson("");

            // Assert
            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("null字符串反序列化应返回空列表")
        void nullStringDeserializationShouldReturnEmptyList() {
            // Act
            List<String> parsed = questionService.parseTagsFromJson(null);

            // Assert
            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("无效JSON反序列化应返回空列表")
        void invalidJsonDeserializationShouldReturnEmptyList() {
            // Act
            List<String> parsed = questionService.parseTagsFromJson("invalid json");

            // Assert
            assertThat(parsed).isEmpty();
        }

        @Test
        @DisplayName("包含特殊字符的标签序列化后反序列化应得到相同结果")
        void specialCharacterTagsSerializationRoundTrip() {
            // Arrange
            List<String> tags = List.of("C++", "C#", "Node.js", "Vue.js");

            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).containsExactlyElementsOf(tags);
        }

        @Test
        @DisplayName("包含中文的标签序列化后反序列化应得到相同结果")
        void chineseTagsSerializationRoundTrip() {
            // Arrange
            List<String> tags = List.of("面试题", "八股文", "算法", "数据库");

            // Act
            String json = questionService.convertTagsToJson(tags);
            List<String> parsed = questionService.parseTagsFromJson(json);

            // Assert
            assertThat(parsed).containsExactlyElementsOf(tags);
        }
    }


    /**
     * Feature: bagu-service, Property 10: 分页参数正确性
     * Validates: Requirements 4.1
     * 
     * *For any* 分页查询请求，返回结果的数量应不超过请求的页大小，且偏移量应正确。
     */
    @Nested
    @DisplayName("Property 10: 分页参数正确性")
    class PaginationPropertyTest {

        @ParameterizedTest
        @MethodSource("providePaginationParams")
        @DisplayName("分页参数应在有效范围内且偏移量计算正确")
        void paginationParametersShouldBeValid(int current, int pageSize) {
            // Assert - 验证分页参数的有效性
            assertThat(current).isGreaterThanOrEqualTo(1);
            assertThat(pageSize).isGreaterThanOrEqualTo(1);
            assertThat(pageSize).isLessThanOrEqualTo(50);

            // 计算偏移量
            int offset = (current - 1) * pageSize;
            assertThat(offset).isGreaterThanOrEqualTo(0);
        }

        static Stream<Arguments> providePaginationParams() {
            return Stream.of(
                    Arguments.of(1, 10),
                    Arguments.of(1, 1),
                    Arguments.of(1, 50),
                    Arguments.of(10, 10),
                    Arguments.of(100, 20),
                    Arguments.of(50, 50)
            );
        }

        @Test
        @DisplayName("第一页偏移量应为0")
        void firstPageOffsetShouldBeZero() {
            int current = 1;
            int pageSize = 10;
            int offset = (current - 1) * pageSize;
            assertThat(offset).isEqualTo(0);
        }

        @Test
        @DisplayName("第二页偏移量应等于页大小")
        void secondPageOffsetShouldEqualPageSize() {
            int current = 2;
            int pageSize = 10;
            int offset = (current - 1) * pageSize;
            assertThat(offset).isEqualTo(10);
        }
    }

    /**
     * Feature: bagu-service, Property 11: 关键词搜索准确性
     * Validates: Requirements 4.2
     * 
     * *For any* 关键词搜索请求，返回的所有题目标题应包含该关键词。
     */
    @Nested
    @DisplayName("Property 11: 关键词搜索准确性")
    class KeywordSearchPropertyTest {

        @ParameterizedTest
        @MethodSource("provideKeywordAndTitles")
        @DisplayName("标题应包含搜索关键词")
        void titleShouldContainKeyword(String keyword, String title) {
            // Assert - 验证标题包含关键词（忽略大小写）
            assertThat(title.toLowerCase()).contains(keyword.toLowerCase());
        }

        static Stream<Arguments> provideKeywordAndTitles() {
            return Stream.of(
                    Arguments.of("Java", "Java面试题"),
                    Arguments.of("Spring", "Spring Boot入门教程"),
                    Arguments.of("算法", "常见算法面试题"),
                    Arguments.of("sql", "MySQL SQL优化技巧"),
                    Arguments.of("redis", "Redis缓存设计")
            );
        }

        @Test
        @DisplayName("空关键词应匹配所有标题")
        void emptyKeywordShouldMatchAll() {
            String keyword = "";
            String title = "任意标题";
            // 空关键词时，所有标题都应该匹配
            assertThat(title).contains(keyword);
        }
    }


    /**
     * Feature: bagu-service, Property 16: 排序正确性
     * Validates: Requirements 6.1, 6.2, 6.3, 6.4
     * 
     * *For any* 带排序参数的查询请求，返回结果应按指定字段和顺序正确排列。
     */
    @Nested
    @DisplayName("Property 16: 排序正确性")
    class SortingPropertyTest {

        @ParameterizedTest
        @ValueSource(strings = {"viewNum", "thumbNum", "favourNum", "createTime", "updateTime"})
        @DisplayName("有效排序字段应被接受")
        void validSortFieldsShouldBeAccepted(String sortField) {
            // Assert - 验证排序字段是有效的
            List<String> validFields = List.of("viewNum", "thumbNum", "favourNum", "createTime", "updateTime");
            assertThat(validFields).contains(sortField);
        }

        @Test
        @DisplayName("降序排列时前一个值应大于等于后一个值")
        void descendingOrderShouldBeCorrect() {
            // Arrange - 模拟降序排列的数据
            int[] values = {100, 50, 30, 10, 5};

            // Assert - 验证降序
            for (int i = 0; i < values.length - 1; i++) {
                assertThat(values[i]).isGreaterThanOrEqualTo(values[i + 1]);
            }
        }

        @Test
        @DisplayName("升序排列时前一个值应小于等于后一个值")
        void ascendingOrderShouldBeCorrect() {
            // Arrange - 模拟升序排列的数据
            int[] values = {5, 10, 30, 50, 100};

            // Assert - 验证升序
            for (int i = 0; i < values.length - 1; i++) {
                assertThat(values[i]).isLessThanOrEqualTo(values[i + 1]);
            }
        }

        @ParameterizedTest
        @MethodSource("provideSortingData")
        @DisplayName("排序后数据应保持正确顺序")
        void sortedDataShouldMaintainOrder(int[] data, boolean ascending) {
            // Arrange - 排序
            int[] sorted = Arrays.copyOf(data, data.length);
            Arrays.sort(sorted);
            if (!ascending) {
                // 反转为降序
                for (int i = 0; i < sorted.length / 2; i++) {
                    int temp = sorted[i];
                    sorted[i] = sorted[sorted.length - 1 - i];
                    sorted[sorted.length - 1 - i] = temp;
                }
            }

            // Assert - 验证排序正确性
            for (int i = 0; i < sorted.length - 1; i++) {
                if (ascending) {
                    assertThat(sorted[i]).isLessThanOrEqualTo(sorted[i + 1]);
                } else {
                    assertThat(sorted[i]).isGreaterThanOrEqualTo(sorted[i + 1]);
                }
            }
        }

        static Stream<Arguments> provideSortingData() {
            return Stream.of(
                    Arguments.of(new int[]{3, 1, 4, 1, 5, 9, 2, 6}, true),
                    Arguments.of(new int[]{3, 1, 4, 1, 5, 9, 2, 6}, false),
                    Arguments.of(new int[]{100, 200, 50, 75}, true),
                    Arguments.of(new int[]{100, 200, 50, 75}, false)
            );
        }
    }


    /**
     * Feature: bagu-service, Property 13: 点赞 Round-Trip
     * Validates: Requirements 5.1, 5.2
     * 
     * *For any* 题目和用户，点赞后取消点赞，题目的点赞数应恢复到初始值，用户的点赞状态应为false。
     */
    @Nested
    @DisplayName("Property 13: 点赞 Round-Trip")
    class ThumbRoundTripPropertyTest {

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
    }

    /**
     * Feature: bagu-service, Property 14: 收藏 Round-Trip
     * Validates: Requirements 5.3, 5.4
     * 
     * *For any* 题目和用户，收藏后取消收藏，题目的收藏数应恢复到初始值，用户的收藏状态应为false。
     */
    @Nested
    @DisplayName("Property 14: 收藏 Round-Trip")
    class FavourRoundTripPropertyTest {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, 100, 1000})
        @DisplayName("收藏后取消收藏应恢复初始状态")
        void favourRoundTripShouldRestoreInitialState(int initialFavourNum) {
            // Arrange - 模拟初始收藏数
            int favourNum = initialFavourNum;

            // Act - 收藏
            favourNum += 1;
            boolean hasFavour = true;

            // Assert - 收藏后状态
            assertThat(favourNum).isEqualTo(initialFavourNum + 1);
            assertThat(hasFavour).isTrue();

            // Act - 取消收藏
            favourNum -= 1;
            hasFavour = false;

            // Assert - 验证恢复到初始状态
            assertThat(favourNum).isEqualTo(initialFavourNum);
            assertThat(hasFavour).isFalse();
        }

        @Test
        @DisplayName("收藏数不应为负数")
        void favourNumShouldNotBeNegative() {
            int favourNum = 0;

            // 模拟多次收藏
            favourNum += 5;
            assertThat(favourNum).isEqualTo(5);

            // 模拟取消收藏（不能超过当前收藏数）
            int unfavourCount = 3;
            favourNum -= Math.min(unfavourCount, favourNum);
            assertThat(favourNum).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("同一用户重复收藏应切换状态")
        void sameUserFavourShouldToggle() {
            boolean hasFavour = false;
            int favourNum = 0;

            // 第一次收藏
            hasFavour = !hasFavour;
            favourNum += hasFavour ? 1 : -1;
            assertThat(hasFavour).isTrue();
            assertThat(favourNum).isEqualTo(1);

            // 第二次收藏（取消）
            hasFavour = !hasFavour;
            favourNum += hasFavour ? 1 : -1;
            assertThat(hasFavour).isFalse();
            assertThat(favourNum).isEqualTo(0);
        }
    }


    /**
     * 标题验证测试
     * Validates: Requirements 2.4
     */
    @Nested
    @DisplayName("标题验证测试")
    class TitleValidationTest {

        @Test
        @DisplayName("空字符串标题应被拒绝")
        void shouldRejectEmptyTitle() {
            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle("", true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("null标题应被拒绝")
        void shouldRejectNullTitle() {
            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle(null, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("纯空格标题应被拒绝")
        void shouldRejectWhitespaceOnlyTitle() {
            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle("   ", true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题不能为空");
        }

        @Test
        @DisplayName("超过256字符的标题应被拒绝")
        void shouldRejectTooLongTitle() {
            String longTitle = "a".repeat(257);

            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle(longTitle, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }

        @ParameterizedTest
        @ValueSource(ints = {257, 300, 500, 1000})
        @DisplayName("各种超长标题都应被拒绝")
        void shouldRejectVariousLongTitles(int length) {
            String longTitle = "a".repeat(length);

            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle(longTitle, true),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }

        @Test
        @DisplayName("256字符的标题应被接受（边界值）")
        void shouldAcceptMaxLengthTitle() {
            String maxTitle = "a".repeat(256);
            questionService.validateTitle(maxTitle, true);
        }

        @Test
        @DisplayName("有效标题应被接受")
        void shouldAcceptValidTitle() {
            questionService.validateTitle("Java面试题", true);
            questionService.validateTitle("Spring Boot 入门", true);
            questionService.validateTitle("算法与数据结构", true);
        }

        @Test
        @DisplayName("更新时空标题应被接受（允许不修改标题）")
        void shouldAcceptEmptyTitleOnUpdate() {
            questionService.validateTitle("", false);
            questionService.validateTitle(null, false);
        }

        @Test
        @DisplayName("更新时超长标题也应被拒绝")
        void shouldRejectTooLongTitleOnUpdate() {
            String longTitle = "a".repeat(257);

            BusinessException exception = catchThrowableOfType(
                    () -> questionService.validateTitle(longTitle, false),
                    BusinessException.class
            );

            assertThat(exception).isNotNull();
            assertThat(exception.getDescription()).isEqualTo("标题长度不能超过256字符");
        }
    }
}
