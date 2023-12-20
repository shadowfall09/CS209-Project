package org.java2.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.java2.backend.common.Result;
import org.java2.backend.common.TopicPopularityCalculator;
import org.java2.backend.entity.Tag;
import org.java2.backend.exception.ServiceException;
import org.java2.backend.service.IQuestionService;
import org.java2.backend.service.ITagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    private final String[] topics = new String[]{"generics", "lambda", "multithreading", "exception", "spring", "stream", "spring", "reflection", "socket", "javafx"};

    @GetMapping("popularity/{limit}")
    public Result popularity(HttpServletResponse response, @PathVariable("limit") Long limit) {
        if (limit < -1) {
            throw new ServiceException("400", "Invalid path variable");
        }
        Stream<String> topicStream = Arrays.stream(topics);
        if (limit != -1) {
            topicStream = topicStream.limit(limit);
        }
        List<JSONObject> resultList = topicStream.map(this::getPopularityByTopicName).collect(Collectors.toList());
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
        return result;
    }
}
