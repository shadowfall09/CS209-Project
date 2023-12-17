package org.java2.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.java2.backend.common.Result;
import org.java2.backend.constant.SyntaxErrors;
import org.java2.backend.entity.Answer;
import org.java2.backend.entity.Comment;
import org.java2.backend.entity.Question;
import org.java2.backend.service.IAnswerService;
import org.java2.backend.service.ICommentService;
import org.java2.backend.service.IQuestionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RestController
@RequestMapping("/bug")
@Slf4j
public class BugController {

    @Resource
    private IAnswerService answerService;

    @Resource
    private IQuestionService questionService;

    @Resource
    private ICommentService commentService;

    @GetMapping("/Exception/{limit}")
    public Result Exception(HttpServletResponse response, @PathVariable("limit") Integer limit) {
        log.info("Request Exception Info");
        return getResult(response, limit, getException(false));
    }

    @GetMapping("/FatalError/{limit}")
    public Result FatalError(HttpServletResponse response, @PathVariable("limit") Integer limit) {
        log.info("Request Fatal Error Info");
        return getResult(response, limit, getFatalError(false));
    }

    @NotNull
    private Result getResult(HttpServletResponse response, @PathVariable("limit") Integer limit, HashMap<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < Math.min(limit, map.size()); i++) {
            jsonObject.put(list.get(i).getKey(), list.get(i).getValue());
        }
        return Result.success(response, jsonObject);
    }

    @GetMapping("/SyntaxError/{limit}")
    public Result SyntaxError(HttpServletResponse response, @PathVariable("limit") Integer limit) {
        log.info("Request Syntax Error Info");
        return getResult(response, limit, getSyntaxError());
    }

    @NotNull
    private HashMap<String,Integer> getSyntaxError() {
        Long answerCount = answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%"));
        Long questionCount = questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%"));
        Long commentCount = commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%"));
        HashMap<String, Integer> map = new HashMap<>();
        map.put("General Syntax Error", answerCount.intValue() + questionCount.intValue() + commentCount.intValue());
        for (String syntaxError : SyntaxErrors.ERRORS) {
            Long answerCount1 = answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%" + syntaxError + "%"
            ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
            Long questionCount1 = questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%" + syntaxError + "%"
            ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
            Long commentCount1 = commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%" + syntaxError + "%"
            ).or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
            int count = answerCount1.intValue() + questionCount1.intValue() + commentCount1.intValue();
            if (count != 0)
                map.put(syntaxError, count);
        }
        return map;
    }

    @NotNull
    private HashMap<String,Integer> getFatalError(boolean isGeneral) {
        List<Answer> answer = answerService.getBaseMapper().selectList(null);
        List<Question> question = questionService.getBaseMapper().selectList(null);
        List<Comment> comment = commentService.getBaseMapper().selectList(null);
        List<String> answerToken = answer.stream().map(Answer::getTokenization).toList();
        List<String> questionToken = question.stream().map(Question::getTokenization).toList();
        List<String> commentToken = comment.stream().map(Comment::getTokenization).toList();
        List<String> allToken = Stream.of(answerToken, questionToken, commentToken).flatMap(List::stream).toList();
        List<String> answerContent = answer.stream().map(s -> s.getContent() + " " + s.getTitle()).toList();
        List<String> questionContent = question.stream().map(s -> s.getContent() + " " + s.getTitle()).toList();
        List<String> commentContent = comment.stream().map(Comment::getContent).toList();
        List<String> allContent = Stream.of(answerContent, questionContent, commentContent).flatMap(List::stream).toList();
        HashMap<String, Integer> map = new HashMap<>();
        for (String content : allContent) {
            if (content.toLowerCase().contains("a fatal error has been detected by the java runtime environment") || content.toLowerCase().contains("fatal error")) {
                if (map.containsKey("General Fatal Error")) {
                    map.put("General Fatal Error", map.get("General Fatal Error") + 1);
                } else {
                    map.put("General Fatal Error", 1);
                }
            }
        }
        for (String token : allToken) {
            if (token.toLowerCase().contains("error") || token.toLowerCase().contains("threaddeath")) {
                List<String> tokenList = Stream.of(token.split(" \\| ")).map(s -> {
                    String[] split = s.split("\\.");
                    if (split.length == 0)
                        return s;
                    else
                        return split[split.length - 1];
                }).toList();
                for (String s : tokenList) {
                    if (s.toLowerCase().endsWith("error") || s.toLowerCase().endsWith("threaddeath")) {
                        if (map.containsKey(s)) {
                            map.put(s, map.get(s) + 1);
                        } else {
                            map.put(s, 1);
                        }
                    }
                }
            }
        }
        if (!isGeneral) {
            map.remove("error");
            map.remove("errors");
            map.remove("Error");
            map.remove("Errors");
            map.remove("ERROR");
        }
        return map;
    }

    @NotNull
    private HashMap<String,Integer> getException(boolean isGeneral) {
        List<String> answerToken = answerService.getBaseMapper().selectList(null).stream().map(Answer::getTokenization).toList();
        List<String> questionToken = questionService.getBaseMapper().selectList(null).stream().map(Question::getTokenization).toList();
        List<String> commentToken = commentService.getBaseMapper().selectList(null).stream().map(Comment::getTokenization).toList();
        List<String> allToken = Stream.of(answerToken, questionToken, commentToken).flatMap(List::stream).toList();
        HashMap<String, Integer> map = new HashMap<>();
        for (String token : allToken) {
            if (token.toLowerCase().contains("exception")) {
                List<String> tokenList = Stream.of(token.split(" \\| ")).map(s -> {
                    String[] split = s.split("\\.");
                    if (split.length == 0)
                        return s;
                    else
                        return split[split.length - 1];
                }).toList();
                for (String s : tokenList) {
                    if (s.toLowerCase().endsWith("exception")) {
                        if (map.containsKey(s)) {
                            map.put(s, map.get(s) + 1);
                        } else {
                            map.put(s, 1);
                        }
                    }
                }
            }
        }
        if (!isGeneral) {
            map.remove("exception");
            map.remove("exceptions");
            map.remove("Exception");
            map.remove("Exceptions");
            map.remove("EXCEPTION");
        }
        return map;
    }

    @GetMapping("/Error/{limit}")
    public Result getError(HttpServletResponse response, @PathVariable("limit") Integer limit) {
        log.info("Request Error Info");
        HashMap<String,Integer> errors = getSyntaxError();
        errors.putAll(getFatalError(false));
        return getResult(response, limit, errors);
    }

    @GetMapping("/Exception/search/{exception}")
    public Result getException(HttpServletResponse response, @PathVariable("exception") String exception) {
        log.info("Request Specific Exception Info");
        HashMap<String,Integer> exceptions = getException(false);
        return getSearchResult(response, exception, exceptions);
    }

    @NotNull
    private Result getSearchResult(HttpServletResponse response, @PathVariable("exception") String exception, HashMap<String, Integer> exceptions) {
        JSONObject jsonObject = new JSONObject();
        String highest = "";
        int highestCount = 0;
        for (String s : exceptions.keySet()) {
            String regex = ".*"+s.toLowerCase()+".*";
            if (Pattern.matches(regex, exception.toLowerCase())|| LevenshteinDistance.getDefaultInstance().apply(s, exception.toLowerCase())<=3) {
                if (exceptions.get(s)>highestCount) {
                    highest = s;
                    highestCount = exceptions.get(s);
                }
            }
        }
        if (highestCount==0) {
            jsonObject.put("Result", null);
        }else jsonObject.put(highest,highestCount);
        return Result.success(response, jsonObject);
    }

    @GetMapping("/Error/search/{error}")
    public Result getError(HttpServletResponse response, @PathVariable("error") String error) {
        log.info("Request Specific Error Info");
        HashMap<String,Integer> errors = getSyntaxError();
        errors.putAll(getFatalError(false));
        return getSearchResult(response, error, errors);
    }

    @GetMapping("/ErrorAndException")
    public Result getErrorAndException(HttpServletResponse response) {
        log.info("Request Error And Exception Info");
        HashMap<String,Integer> errors = getSyntaxError();
        errors.putAll(getFatalError(true));
        long errorCount = 0;
        for (String s : errors.keySet()) {
            errorCount += errors.get(s);
        }
        HashMap<String,Integer> exceptions = getException(true);
        long exceptionCount = 0;
        for (String s : exceptions.keySet()) {
            exceptionCount += exceptions.get(s);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Error", errorCount);
        jsonObject.put("Exception", exceptionCount);
        return Result.success(response, jsonObject);
    }
}

