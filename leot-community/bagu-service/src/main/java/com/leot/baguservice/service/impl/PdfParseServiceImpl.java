package com.leot.baguservice.service.impl;

import com.leot.baguservice.domain.dto.ParsedQuestionDTO;
import com.leot.baguservice.domain.vo.PdfParseResultVO;
import com.leot.baguservice.service.PdfParseService;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF解析服务实现
 */
@Slf4j
@Service
public class PdfParseServiceImpl implements PdfParseService {

    /**
     * 最大文件大小：100MB
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 允许的文件类型
     */
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    /**
     * 题目编号匹配模式
     * 支持: "1."、"1、"、"Q1:"、"Q1："、"题目1"、"第1题"、"问题1"
     */
    private static final Pattern QUESTION_PATTERN = Pattern.compile(
            "(?:^|\\n)\\s*(?:" +
                    "(\\d+)[.、．]|" +                    // 1. 或 1、
                    "[QqＱ](\\d+)[:：]|" +                // Q1: 或 Q1：
                    "题目\\s*(\\d+)|" +                   // 题目1
                    "第\\s*(\\d+)\\s*题|" +               // 第1题
                    "问题\\s*(\\d+)" +                    // 问题1
                    ")\\s*"
    );

    /**
     * 答案标记匹配模式
     * 支持: "答案:"、"答案："、"Answer:"、"A:"、"参考答案:"
     */
    private static final Pattern ANSWER_PATTERN = Pattern.compile(
            "(?:^|\\n)\\s*(?:答案|Answer|参考答案|[AaＡ])\\s*[:：]\\s*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public PdfParseResultVO parsePdf(MultipartFile file) {
        long startTime = System.currentTimeMillis();

        // 1. 校验文件
        validateFile(file);

        // 2. 提取PDF文本
        String pdfText = extractTextFromPdf(file);

        // 3. 解析题目
        List<ParsedQuestionDTO> questions = parseQuestions(pdfText);

        // 4. 构建返回结果
        PdfParseResultVO result = new PdfParseResultVO();
        result.setQuestions(questions);
        result.setTotalCount(questions.size());
        result.setParseTime(System.currentTimeMillis() - startTime);

        log.info("PDF解析完成，共提取{}道题目，耗时{}ms", questions.size(), result.getParseTime());
        return result;
    }

    /**
     * 校验上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请选择要上传的PDF文件");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持PDF格式的文件");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过100MB");
        }

        // 校验文件名后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持PDF格式的文件");
        }
    }

    /**
     * 从PDF文件中提取文本内容
     */
    private String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDF文件解析失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PDF文件解析失败，请确保文件未损坏");
        }
    }

    /**
     * 从文本中解析题目列表
     */
    private List<ParsedQuestionDTO> parseQuestions(String text) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return questions;
        }

        // 查找所有题目的起始位置
        Matcher matcher = QUESTION_PATTERN.matcher(text);
        List<Integer> questionStarts = new ArrayList<>();

        while (matcher.find()) {
            questionStarts.add(matcher.start());
        }

        if (questionStarts.isEmpty()) {
            // 如果没有找到题目编号，尝试将整个文本作为一道题目
            ParsedQuestionDTO question = parseAsWholeQuestion(text);
            if (question != null) {
                questions.add(question);
            }
            return questions;
        }

        // 解析每道题目
        for (int i = 0; i < questionStarts.size(); i++) {
            int start = questionStarts.get(i);
            int end = (i + 1 < questionStarts.size()) ? questionStarts.get(i + 1) : text.length();

            String questionText = text.substring(start, end).trim();
            ParsedQuestionDTO question = parseQuestionText(questionText);

            if (question != null && isValidQuestion(question)) {
                questions.add(question);
            }
        }

        return questions;
    }

    /**
     * 解析单道题目文本
     */
    private ParsedQuestionDTO parseQuestionText(String questionText) {
        if (questionText == null || questionText.trim().isEmpty()) {
            return null;
        }

        ParsedQuestionDTO question = new ParsedQuestionDTO();

        // 移除题目编号前缀
        String cleanText = QUESTION_PATTERN.matcher(questionText).replaceFirst("").trim();

        // 查找答案分隔位置
        Matcher answerMatcher = ANSWER_PATTERN.matcher(cleanText);

        if (answerMatcher.find()) {
            // 有答案标记，分离题目和答案
            String titleAndContent = cleanText.substring(0, answerMatcher.start()).trim();
            String answer = cleanText.substring(answerMatcher.end()).trim();

            // 提取标题（第一行或第一句）
            extractTitleAndContent(question, titleAndContent);
            question.setAnswer(answer);
        } else {
            // 没有答案标记，整个作为题目内容
            extractTitleAndContent(question, cleanText);
            question.setAnswer("");
        }

        return question;
    }

    /**
     * 提取标题和内容
     */
    private void extractTitleAndContent(ParsedQuestionDTO question, String text) {
        if (text == null || text.trim().isEmpty()) {
            question.setTitle("");
            question.setContent("");
            return;
        }

        // 按换行分割，第一行作为标题
        String[] lines = text.split("\\n", 2);
        String title = lines[0].trim();

        // 如果标题太长，截取前100个字符
        if (title.length() > 100) {
            // 尝试在标点处截断
            int cutIndex = findCutIndex(title, 100);
            title = title.substring(0, cutIndex);
        }

        question.setTitle(title);

        // 剩余部分作为内容
        if (lines.length > 1) {
            question.setContent(lines[1].trim());
        } else {
            question.setContent("");
        }
    }

    /**
     * 查找合适的截断位置
     */
    private int findCutIndex(String text, int maxLength) {
        // 在标点符号处截断
        String punctuation = "。？！?!；;，,";
        for (int i = Math.min(maxLength, text.length()) - 1; i >= maxLength / 2; i--) {
            if (punctuation.indexOf(text.charAt(i)) >= 0) {
                return i + 1;
            }
        }
        return Math.min(maxLength, text.length());
    }

    /**
     * 将整个文本作为一道题目解析
     */
    private ParsedQuestionDTO parseAsWholeQuestion(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        ParsedQuestionDTO question = new ParsedQuestionDTO();
        extractTitleAndContent(question, text.trim());
        question.setAnswer("");
        return question;
    }

    /**
     * 验证题目是否有效
     */
    private boolean isValidQuestion(ParsedQuestionDTO question) {
        // 标题不能为空
        return question != null &&
                question.getTitle() != null &&
                !question.getTitle().trim().isEmpty();
    }
}
