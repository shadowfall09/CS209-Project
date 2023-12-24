package org.java2.backend.controller;

import com.alibaba.fastjson2.JSONArray;
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
import java.util.concurrent.*;
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
    public Result Exception(HttpServletResponse response, @PathVariable("limit") Integer limit) throws ExecutionException, InterruptedException {
        log.info("Request Exception Info");
        return getResult(response, limit, getException(false));
    }

    @GetMapping("/FatalError/{limit}")
    public Result FatalError(HttpServletResponse response, @PathVariable("limit") Integer limit) throws ExecutionException, InterruptedException {
        log.info("Request Fatal Error Info");
        return getResult(response, limit, getFatalError(false));
    }

    @NotNull
    private Result getResult(HttpServletResponse response, @PathVariable("limit") Integer limit, HashMap<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> -o1.getValue().compareTo(o2.getValue()));
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < Math.min(limit, map.size()); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", list.get(i).getKey());
            jsonObject.put("value", list.get(i).getValue());
            jsonArray.add(jsonObject);
        }
        JSONObject responseJson = new JSONObject();
        responseJson.put("list", jsonArray);
        return Result.success(response, responseJson);
    }

    @GetMapping("/SyntaxError/{limit}")
    public Result SyntaxError(HttpServletResponse response, @PathVariable("limit") Integer limit) throws ExecutionException, InterruptedException {
        log.info("Request Syntax Error Info");
        return getResult(response, limit, getSyntaxError());
    }

    //    @NotNull
//    private HashMap<String,Integer> getSyntaxError() {
//        Long answerCount = answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%syntax error%"
//        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%"));
//        Long questionCount = questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%syntax error%"
//        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%"));
//        Long commentCount = commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%syntax error%"
//        ).or().like("content", "%compile error%"));
//        HashMap<String, Integer> map = new HashMap<>();
//        map.put("General Syntax Error", answerCount.intValue() + questionCount.intValue() + commentCount.intValue());
//        for (String syntaxError : SyntaxErrors.ERRORS) {
//            Long answerCount1 = answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%" + syntaxError + "%"
//            ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
//            Long questionCount1 = questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%" + syntaxError + "%"
//            ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
//            Long commentCount1 = commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%" + syntaxError + "%"
//            ).or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
//            int count = answerCount1.intValue() + questionCount1.intValue() + commentCount1.intValue();
//            if (count != 0)
//                map.put(syntaxError, count);
//        }
//        return map;
//    }
    @NotNull
    private HashMap<String, Integer> getSyntaxError() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> answerCountFuture = CompletableFuture.supplyAsync(() -> answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%")));
        CompletableFuture<Long> questionCountFuture = CompletableFuture.supplyAsync(() -> questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%").or().like("title", "%syntax error%").or().like("title", "%compile error%")));
        CompletableFuture<Long> commentCountFuture = CompletableFuture.supplyAsync(() -> commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%syntax error%"
        ).or().like("content", "%compile error%")));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(answerCountFuture, questionCountFuture, commentCountFuture);

        allFutures.get(); // wait for all futures to complete

        HashMap<String, Integer> map = new HashMap<>();
        map.put("General Syntax Error", answerCountFuture.join().intValue() + questionCountFuture.join().intValue() + commentCountFuture.join().intValue());

        ExecutorService executor = Executors.newFixedThreadPool(10); // adjust to your needs
        List<Future<?>> futures = new ArrayList<>();

        for (String syntaxError : SyntaxErrors.ERRORS) {
            futures.add(executor.submit(() -> {
                Long answerCount1 = answerService.getBaseMapper().selectCount(new QueryWrapper<Answer>().like("content", "%" + syntaxError + "%"
                ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
                Long questionCount1 = questionService.getBaseMapper().selectCount(new QueryWrapper<Question>().like("content", "%" + syntaxError + "%"
                ).or().like("title", "%" + syntaxError + "%").or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%").or().like("title", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
                Long commentCount1 = commentService.getBaseMapper().selectCount(new QueryWrapper<Comment>().like("content", "%" + syntaxError + "%"
                ).or().like("content", "%" + SyntaxErrors.ErrorMessages.get(syntaxError) + "%"));
                int count = answerCount1.intValue() + questionCount1.intValue() + commentCount1.intValue();
                if (count != 0)
                    map.put(syntaxError, count);
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for all futures to complete
        }

        executor.shutdown();

        return map;
    }
//
//    @NotNull
//    private HashMap<String,Integer> getFatalError() {
//        List<Answer> answer = answerService.getBaseMapper().selectList(null);
//        List<Question> question = questionService.getBaseMapper().selectList(null);
//        List<Comment> comment = commentService.getBaseMapper().selectList(null);
//        List<String> answerToken = answer.stream().map(Answer::getTokenization).toList();
//        List<String> questionToken = question.stream().map(Question::getTokenization).toList();
//        List<String> commentToken = comment.stream().map(Comment::getTokenization).toList();
//        List<String> allToken = Stream.of(answerToken, questionToken, commentToken).flatMap(List::stream).toList();
//        List<String> answerContent = answer.stream().map(s -> s.getContent() + " " + s.getTitle()).toList();
//        List<String> questionContent = question.stream().map(s -> s.getContent() + " " + s.getTitle()).toList();
//        List<String> commentContent = comment.stream().map(Comment::getContent).toList();
//        List<String> allContent = Stream.of(answerContent, questionContent, commentContent).flatMap(List::stream).toList();
//        HashMap<String, Integer> map = new HashMap<>();
//        for (String content : allContent) {
//            if (content.toLowerCase().contains("a fatal error has been detected by the java runtime environment") || content.toLowerCase().contains("fatal error")) {
//                if (map.containsKey("General Fatal Error")) {
//                    map.put("General Fatal Error", map.get("General Fatal Error") + 1);
//                } else {
//                    map.put("General Fatal Error", 1);
//                }
//            }
//        }
//        for (String token : allToken) {
//            if (token.toLowerCase().contains("error") || token.toLowerCase().contains("threaddeath")) {
//                List<String> tokenList = Stream.of(token.split(" \\| ")).map(s -> {
//                    String[] split = s.split("\\.");
//                    if (split.length == 0)
//                        return s;
//                    else
//                        return split[split.length - 1];
//                }).toList();
//                for (String s : tokenList) {
//                    if (s.toLowerCase().endsWith("error") || s.toLowerCase().endsWith("threaddeath")) {
//                        if (map.containsKey(s)) {
//                            map.put(s, map.get(s) + 1);
//                        } else {
//                            map.put(s, 1);
//                        }
//                    }
//                }
//            }
//        }
//        map.remove("error");
//        map.remove("errors");
//        map.remove("Error");
//        map.remove("Errors");
//        map.remove("ERROR");
//        return map;
//    }

    @NotNull
    private HashMap<String, Integer> getFatalError(boolean isGeneral) throws ExecutionException, InterruptedException {
        CompletableFuture<List<Answer>> answerFuture = CompletableFuture.supplyAsync(() -> answerService.getBaseMapper().selectList(null));
        CompletableFuture<List<Question>> questionFuture = CompletableFuture.supplyAsync(() -> questionService.getBaseMapper().selectList(null));
        CompletableFuture<List<Comment>> commentFuture = CompletableFuture.supplyAsync(() -> commentService.getBaseMapper().selectList(null));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(answerFuture, questionFuture, commentFuture);

        allFutures.get(); // wait for all futures to complete

        CompletableFuture<List<String>> answerTokenFuture = CompletableFuture.supplyAsync(() -> answerFuture.join().stream().map(Answer::getTokenization).toList());
        CompletableFuture<List<String>> questionTokenFuture = CompletableFuture.supplyAsync(() -> questionFuture.join().stream().map(Question::getTokenization).toList());
        CompletableFuture<List<String>> commentTokenFuture = CompletableFuture.supplyAsync(() -> commentFuture.join().stream().map(Comment::getTokenization).toList());

        CompletableFuture<List<String>> answerContentFuture = CompletableFuture.supplyAsync(() -> answerFuture.join().stream().map(s -> s.getContent() + " " + s.getTitle()).toList());
        CompletableFuture<List<String>> questionContentFuture = CompletableFuture.supplyAsync(() -> questionFuture.join().stream().map(s -> s.getContent() + " " + s.getTitle()).toList());
        CompletableFuture<List<String>> commentContentFuture = CompletableFuture.supplyAsync(() -> commentFuture.join().stream().map(Comment::getContent).toList());

        CompletableFuture<Void> allTokenAndContentFutures = CompletableFuture.allOf(answerTokenFuture, questionTokenFuture, commentTokenFuture, answerContentFuture, questionContentFuture, commentContentFuture);

        allTokenAndContentFutures.get(); // wait for all futures to complete

        List<String> allToken = Stream.of(answerTokenFuture.join(), questionTokenFuture.join(), commentTokenFuture.join()).flatMap(List::stream).toList();
        List<String> allContent = Stream.of(answerContentFuture.join(), questionContentFuture.join(), commentContentFuture.join()).flatMap(List::stream).toList();

        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(() -> {
            allContent.parallelStream().forEach(content -> {
                if (content.toLowerCase().contains("a fatal error has been detected by the java runtime environment") || content.toLowerCase().contains("fatal error")) {
                    map.put("General Fatal Error", map.getOrDefault("General Fatal Error", 0) + 1);
                }
            });
            allToken.parallelStream().forEach(token -> {
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
                            map.put(s, map.getOrDefault(s, 0) + 1);
                        }
                    }
                }
            });
        }).get();
        if (!isGeneral) {
            map.remove("error");
            map.remove("errors");
            map.remove("Error");
            map.remove("Errors");
            map.remove("ERROR");
        }
        return new HashMap<>(map);
    }

    @NotNull
    private HashMap<String, Integer> getException(boolean isGeneral) throws InterruptedException, ExecutionException {
        CompletableFuture<List<String>> answerTokenFuture = CompletableFuture.supplyAsync(() -> answerService.getBaseMapper().selectList(null).stream().map(Answer::getTokenization).toList());
        CompletableFuture<List<String>> questionTokenFuture = CompletableFuture.supplyAsync(() -> questionService.getBaseMapper().selectList(null).stream().map(Question::getTokenization).toList());
        CompletableFuture<List<String>> commentTokenFuture = CompletableFuture.supplyAsync(() -> commentService.getBaseMapper().selectList(null).stream().map(Comment::getTokenization).toList());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(answerTokenFuture, questionTokenFuture, commentTokenFuture);
        allFutures.get();
        List<String> allToken = Stream.of(answerTokenFuture.join(), questionTokenFuture.join(), commentTokenFuture.join()).flatMap(List::stream).toList();
        ConcurrentMap<String, Integer> map = new ConcurrentHashMap<>();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(() ->
                allToken.parallelStream().forEach(token -> {
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
                                map.put(s, map.getOrDefault(s, 0) + 1);
                            }
                        }
                    }
                })
        ).get();
        if (!isGeneral) {
            map.remove("exception");
            map.remove("exceptions");
            map.remove("Exception");
            map.remove("Exceptions");
            map.remove("EXCEPTION");
        }
        return new HashMap<>(map);
    }

//    @NotNull
//    private HashMap<String,Integer> getException() {
//        Long startTime = System.currentTimeMillis();
//        List<String> answerToken = answerService.getBaseMapper().selectList(null).stream().map(Answer::getTokenization).toList();
//        List<String> questionToken = questionService.getBaseMapper().selectList(null).stream().map(Question::getTokenization).toList();
//        List<String> commentToken = commentService.getBaseMapper().selectList(null).stream().map(Comment::getTokenization).toList();
//        List<String> allToken = Stream.of(answerToken, questionToken, commentToken).flatMap(List::stream).toList();
//        HashMap<String, Integer> map = new HashMap<>();
//        for (String token : allToken) {
//            if (token.toLowerCase().contains("exception")) {
//                List<String> tokenList = Stream.of(token.split(" \\| ")).map(s -> {
//                    String[] split = s.split("\\.");
//                    if (split.length == 0)
//                        return s;
//                    else
//                        return split[split.length - 1];
//                }).toList();
//                for (String s : tokenList) {
//                    if (s.toLowerCase().endsWith("exception")) {
//                        if (map.containsKey(s)) {
//                            map.put(s, map.get(s) + 1);
//                        } else {
//                            map.put(s, 1);
//                        }
//                    }
//                }
//            }
//        }
//        map.remove("exception");
//        map.remove("exceptions");
//        map.remove("Exception");
//        map.remove("Exceptions");
//        map.remove("EXCEPTION");
//        System.out.println(System.currentTimeMillis()-startTime);
//        return map;
//    }

    @GetMapping("/Error/{limit}")
    public Result getError(HttpServletResponse response, @PathVariable("limit") Integer limit) throws ExecutionException, InterruptedException {
        log.info("Request Error Info");
        HashMap<String, Integer> errors = getSyntaxError();
        errors.putAll(getFatalError(false));
        return getResult(response, limit, errors);
    }

    @GetMapping("/Exception/search/{exception}")
    public Result getException(HttpServletResponse response, @PathVariable("exception") String exception) throws ExecutionException, InterruptedException {
        log.info("Request Specific Exception Info");
        HashMap<String, Integer> exceptions = getException(false);
        return getSearchResult(response, exception, exceptions);
    }

    @NotNull
    private Result getSearchResult(HttpServletResponse response, @PathVariable("exception") String exception, HashMap<String, Integer> exceptions) {
        JSONObject jsonObject = new JSONObject();
        String highest = "";
        int highestCount = 0;
        for (String s : exceptions.keySet()) {
            String regex = ".*" + s.toLowerCase() + ".*";
            if (Pattern.matches(regex, exception.toLowerCase()) || LevenshteinDistance.getDefaultInstance().apply(s, exception.toLowerCase()) <= 3) {
                if (exceptions.get(s) > highestCount) {
                    highest = s;
                    highestCount = exceptions.get(s);
                }
            }
        }
        if (highestCount == 0) {
            jsonObject.put("Result", null);
        } else jsonObject.put(highest, highestCount);
        return Result.success(response, jsonObject);
    }

    @GetMapping("/Error/search/{error}")
    public Result getError(HttpServletResponse response, @PathVariable("error") String error) throws ExecutionException, InterruptedException {
        log.info("Request Specific Error Info");
        HashMap<String, Integer> errors = getSyntaxError();
        errors.putAll(getFatalError(false));
        return getSearchResult(response, error, errors);
    }

    //    @GetMapping("/ErrorAndException")
//    public Result getErrorAndException(HttpServletResponse response) throws ExecutionException, InterruptedException {
//        log.info("Request Error And Exception Info");
//        HashMap<String,Integer> errors = getSyntaxError();
//        errors.putAll(getFatalError(true));
//        long errorCount = 0;
//        for (String s : errors.keySet()) {
//            errorCount += errors.get(s);
//        }
//        HashMap<String,Integer> exceptions = getException(true);
//        long exceptionCount = 0;
//        for (String s : exceptions.keySet()) {
//            exceptionCount += exceptions.get(s);
//        }
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("Error", errorCount);
//        jsonObject.put("Exception", exceptionCount);
//        return Result.success(response, jsonObject);
//    }
    @GetMapping("/ErrorAndException")
    public Result getErrorAndException(HttpServletResponse response) throws ExecutionException, InterruptedException {
        log.info("Request Error And Exception Info");
        CompletableFuture<HashMap<String, Integer>> syntaxErrorsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getSyntaxError();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<HashMap<String, Integer>> fatalErrorsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getFatalError(true);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<HashMap<String, Integer>> exceptionsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getException(true);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(syntaxErrorsFuture, fatalErrorsFuture, exceptionsFuture);
        allFutures.get(); // wait for all futures to complete
        HashMap<String, Integer> errors = new HashMap<>();
        errors.putAll(syntaxErrorsFuture.join());
        errors.putAll(fatalErrorsFuture.join());
        HashMap<String, Integer> exceptions = exceptionsFuture.join();
        long errorCount = errors.values().stream().mapToLong(Integer::longValue).sum();
        long exceptionCount = exceptions.values().stream().mapToLong(Integer::longValue).sum();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Error", errorCount);
        jsonObject.put("Exception", exceptionCount);
        return Result.success(response, jsonObject);
    }
}

