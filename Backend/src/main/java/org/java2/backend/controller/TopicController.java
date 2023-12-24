package org.java2.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.java2.backend.common.Result;
import org.java2.backend.common.TopicPopularityCalculator;
import org.java2.backend.entity.*;
import org.java2.backend.exception.ServiceException;
import org.java2.backend.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/topic")
public class TopicController {
    @Resource
    private ITagService tagService;

    @Resource
    private IQuestionService questionService;
    @Resource
    private IQuestionTagRelationService questionTagRelationService;
    @Resource
    private IAnswerService answerService;
    @Resource
    private IAnswerTagRelationService answerTagRelationService;

    private final String[] topics = new String[]{"generics", "lambda", "multithreading", "exception", "spring", "stream", "junit", "reflection", "socket", "javafx"};

    @GetMapping("popularity/{limit}")
    public Result popularity(HttpServletResponse response, @PathVariable("limit") Long limit) {
        if (limit < -1) {
            throw new ServiceException("400", "Invalid path variable");
        }
        Stream<String> topicStream = Arrays.stream(topics);
        if (limit != -1) {
            topicStream = topicStream.limit(limit);
        }
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        List<Future<JSONObject>> resultFutureList = new ArrayList<>();
        topicStream.forEach(topic -> {
            resultFutureList.add(executor.submit(() -> getPopularityByTopicName(topic)));
        });
        List<JSONObject> resultList = resultFutureList.stream().map(resultFuture -> {
            try {
                return resultFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new ServiceException("500", "An error occur when calculating the popularity of topics");
            }
        }).collect(Collectors.toList());
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("popularity", resultList);
        return Result.success(response, resultJSONObject);
    }

    @GetMapping("popularity/search/{topic}")
    public Result searchTopic(HttpServletResponse response, @PathVariable("topic") String topic) {
        return Result.success(response, getPopularityByTopicName(topic));
    }

    @GetMapping("popularityAllTopics/{metric}/{limit}")
    public Result popularityAllTopics(HttpServletResponse response, @PathVariable("metric") Integer metric, @PathVariable("limit") Integer limit) {
        if ((metric < -1) || (metric > 5) || (limit < -1)) {
            throw new ServiceException("400", "Invalid path variable");
        }
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        if (limit != -1) {
            tagQueryWrapper = tagQueryWrapper.last("limit " + limit);
        }
        switch (metric) {
            case 0: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("comprehensive_score");
                break;
            }
            case 1: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("thread_number");
                break;
            }
            case 2: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("thread_number_2023");
                break;
            }
            case 3: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("average_view_count");
                break;
            }
            case 4: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("average_vote_count");
                break;
            }
            case 5: {
                tagQueryWrapper = tagQueryWrapper.orderByDesc("discussion_people_number");
                break;
            }
        }
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("popularity", tagService.list(tagQueryWrapper));
        return Result.success(response, resultJSONObject);
    }

    @GetMapping("popularity/calculateMetricsForAllTags")
    public Result calculateMetricsForAllTags(HttpServletResponse response) {
        int coreNumber = Runtime.getRuntime().availableProcessors();
        int tagNumber = (int) tagService.count();
        int size = tagNumber / coreNumber + 1;
        for (int i = 1; i <= coreNumber; i++) {
            TopicPopularityCalculator topicPopularityCalculator = new TopicPopularityCalculator(tagService, questionService, true, i, size);
            Thread topicPopularityCalculatorThread = new Thread(topicPopularityCalculator);
            topicPopularityCalculatorThread.start();
        }
        return Result.success(response, "Calculate start");
    }

    @GetMapping("popularity/calculateComprehensiveScoreForAllTags")
    public Result calculateComprehensiveScoreForAllTags(HttpServletResponse response) {
        TopicPopularityCalculator topicPopularityCalculator = new TopicPopularityCalculator(tagService, questionService, false, -1, -1);
        Thread topicPopularityCalculatorThread = new Thread(topicPopularityCalculator);
        topicPopularityCalculatorThread.start();
        return Result.success(response, "Calculate start");
    }

    private JSONObject getPopularityByTopicName(String topic) {
        JSONObject result = new JSONObject();
        result.put("topic", topic);
        List<Integer> relatedTagIdList = tagService.getIdsByKeyword(topic);
        List<String> questionIdList = questionService.getIdsByTagIds(relatedTagIdList);
        result.put("threadNumber", questionIdList.size());
        result.put("threadNumber2023", questionService.getQuestionCount2023ByIds(questionIdList));
        result.put("averageViewCount", questionService.getAverageViewCountByIds(questionIdList));
        result.put("averageVoteCount", questionService.getAverageVoteCountByIds(questionIdList));
        result.put("discussionPeopleNumber", questionService.getdiscussionPeopleNumberByIds(questionIdList));
        Tag sum = tagService.getOne(new QueryWrapper<Tag>().select("sum(thread_number) as thread_number, sum(thread_number_2023) as thread_number2023, sum(average_view_count) as average_view_count, sum(average_vote_count) as average_vote_count, sum(discussion_people_number) as discussion_people_number"));
        int totalSum = sum.getThreadNumber() + sum.getThreadNumber2023() + sum.getAverageViewCount() + sum.getAverageVoteCount() + sum.getDiscussionPeopleNumber();
        result.put("comprehensiveScore", (int)(result.getIntValue("threadNumber") * (((double) sum.getThreadNumber()) / totalSum) + result.getIntValue("threadNumber2023") * (((double) sum.getThreadNumber2023()) / totalSum) + result.getIntValue("averageViewCount") * (((double) sum.getAverageViewCount()) / totalSum) + result.getIntValue("averageVoteCount") * (((double) sum.getAverageVoteCount()) / totalSum) + result.getIntValue("discussionPeopleNumber") * (((double) sum.getDiscussionPeopleNumber()) / totalSum)));
        return result;
    }

    @GetMapping("related/search/{topic}")
    public Result searchRelatedTopic(HttpServletResponse response, @PathVariable("topic") String topic) {
        String topicLowerCase = topic.toLowerCase();
        List<String> questionIdList = questionService.list(new QueryWrapper<Question>().select("id").like("lower(title)", topicLowerCase).or().like("lower(content)", topicLowerCase)).stream().map(Question::getId).toList();
        List<String> answerIdList = answerService.list(new QueryWrapper<Answer>().select("id").like("lower(title)", topicLowerCase).or().like("lower(content)", topicLowerCase)).stream().map(Answer::getId).toList();
        Map<Integer, Integer> relatedTopicMap = new HashMap<>();
        questionIdList.forEach(questionId -> questionTagRelationService.list(new QueryWrapper<QuestionTagRelation>().eq("question_id", questionId)).forEach(questionTagRelation -> {
            int tagId = questionTagRelation.getTagId();
            if (relatedTopicMap.containsKey(tagId)) {
                relatedTopicMap.put(tagId, relatedTopicMap.get(tagId) + 1);
            }
            else {
                relatedTopicMap.put(tagId, 1);
            }
        }));
        answerIdList.forEach(answerId -> answerTagRelationService.list(new QueryWrapper<AnswerTagRelation>().eq("answer_id", answerId)).forEach(answerTagRelation -> {
            int tagId = answerTagRelation.getTagId();
            if (relatedTopicMap.containsKey(tagId)) {
                relatedTopicMap.put(tagId, relatedTopicMap.get(tagId) + 1);
            }
            else {
                relatedTopicMap.put(tagId, 1);
            }
        }));
        List<JSONObject> relatedTopicList = relatedTopicMap.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(tagService.getOne(new QueryWrapper<Tag>().select("tag_name").eq("id", entry.getKey())).getTagName(), entry.getValue())).filter(entry -> {
            String temp = entry.getKey().toLowerCase();
            return (LevenshteinDistance.getDefaultInstance().apply(topicLowerCase, temp) > 3) && (!temp.contains("java"));
        }).sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).map(entry -> {
            JSONObject resultJSONObject = new JSONObject();
            resultJSONObject.put("topicName", entry.getKey());
            resultJSONObject.put("relevance", entry.getValue());
            return resultJSONObject;
        }).collect(Collectors.toList());
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("relatedTopicList", relatedTopicList);
        return Result.success(response, resultJSONObject);
    }

    @GetMapping("related/search/{topic1}/{topic2}")
    public Result searchRelatedTopic(HttpServletResponse response, @PathVariable("topic1") String topic1, @PathVariable("topic2") String topic2) {
        String topic1LowerCase = topic1.toLowerCase();
        String topic2LowerCase = topic2.toLowerCase();
        List<String> questionIdList = questionService.list(new QueryWrapper<Question>().select("id").nested(wrapper -> wrapper.like("lower(title)", topic1LowerCase).or().like("lower(content)", topic1LowerCase)).and(wrapper -> wrapper.like("lower(title)", topic2LowerCase).or().like("lower(content)", topic2LowerCase))).stream().map(Question::getId).toList();
        List<String> answerIdList = answerService.list(new QueryWrapper<Answer>().select("id").nested(wrapper -> wrapper.like("lower(title)", topic1LowerCase).or().like("lower(content)", topic1LowerCase)).and(wrapper -> wrapper.like("lower(title)", topic2LowerCase).or().like("lower(content)", topic2LowerCase))).stream().map(Answer::getId).toList();
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("relevance", questionIdList.size() + answerIdList.size());
        return Result.success(response, resultJSONObject);
    }
}
