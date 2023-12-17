package org.java2.backend.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.java2.backend.common.Result;
import org.java2.backend.constant.FatalErrors;
import org.java2.backend.entity.Answer;
import org.java2.backend.entity.Comment;
import org.java2.backend.entity.Question;
import org.java2.backend.service.IAnswerService;
import org.java2.backend.service.ICommentService;
import org.java2.backend.service.IQuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    @GetMapping("/Exception")
    public Result getException(HttpServletResponse response) {
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
        map.remove("exception");
        map.remove("exceptions");
        map.remove("Exception");
        map.remove("Exceptions");
        map.remove("EXCEPTION");
        TreeMap<String, Integer> sortedMap = new TreeMap<>(new ValueComparator(map));
        sortedMap.putAll(map);
        return Result.success(response, sortedMap);
    }

    @GetMapping("/FatalError")
    public Result getFatalError(HttpServletResponse response) {
        List<String> answerToken = answerService.getBaseMapper().selectList(null).stream().map(Answer::getTokenization).toList();
        List<String> questionToken = questionService.getBaseMapper().selectList(null).stream().map(Question::getTokenization).toList();
        List<String> commentToken = commentService.getBaseMapper().selectList(null).stream().map(Comment::getTokenization).toList();
        List<String> allToken = Stream.of(answerToken, questionToken, commentToken).flatMap(List::stream).toList();
        HashMap<String, Integer> map = new HashMap<>();
        for (String token : allToken) {
            if (token.toLowerCase().contains("error")||token.toLowerCase().contains("threaddeath")) {
                List<String> tokenList = Stream.of(token.split(" \\| ")).map(s -> {
                    String[] split = s.split("\\.");
                    if (split.length == 0)
                        return s;
                    else
                        return split[split.length - 1];
                }).toList();
                for (String s : tokenList) {
                    if (s.toLowerCase().endsWith("error")) {
                        if (map.containsKey(s)) {
                            map.put(s, map.get(s) + 1);
                        } else {
                            map.put(s, 1);
                        }
                    }
                }
            }
        }
        map.remove("error");
        map.remove("errors");
        map.remove("Error");
        map.remove("Errors");
        map.remove("ERROR");
        TreeMap<String, Integer> sortedMap = new TreeMap<>(new ValueComparator(map));
        sortedMap.putAll(map);
        return Result.success(response, sortedMap);
    }

    @GetMapping("/SyntaxError")
    public Result getSyntaxError(HttpServletResponse response) {

        return Result.success(response);
    }

    @GetMapping("/Error")
    public Result getError(HttpServletResponse response) {
        return Result.success(response);
    }

    @GetMapping("/ErrorAndException")
    public Result getErrorAndException(HttpServletResponse response) {
        return Result.success(response);
    }

    static class ValueComparator implements Comparator<String> {
        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}

