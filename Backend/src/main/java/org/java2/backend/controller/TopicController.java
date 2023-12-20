package org.java2.backend.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.java2.backend.common.Result;
import org.java2.backend.common.TopicPopularityCalculator;
import org.java2.backend.entity.Tag;
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

    @GetMapping("popularity/{metric}/{limit}")
    public Result popularitySorted(HttpServletResponse response, @PathVariable("metric") Integer metric, @PathVariable("limit") Long limit) {
        Stream<String> topicStream = Arrays.stream(topics);
        if ((limit != -1) && (metric == -1)) {
            topicStream = topicStream.limit(limit);
        }
        Stream<JSONObject> resultStream = topicStream.map(this::getPopularityByTopicName);
        if (metric != -1) {
            AtomicInteger threadNumberSum = new AtomicInteger();
            AtomicInteger averageViewCountSum = new AtomicInteger();
            AtomicInteger averageVoteCountSum = new AtomicInteger();
            resultStream = resultStream.peek(result -> {
                threadNumberSum.addAndGet(result.getIntValue("threadNumber"));
                averageViewCountSum.addAndGet(result.getIntValue("averageViewCount"));
                averageVoteCountSum.addAndGet(result.getIntValue("averageVoteCount"));
            }).sorted(Comparator.comparing(result -> -switch (metric) {
                case 0 ->
                        (int)(result.getIntValue("threadNumber") * (((double) threadNumberSum.get()) / (threadNumberSum.get() + averageViewCountSum.get() + averageVoteCountSum.get())) + result.getIntValue("averageViewCount") * (((double) averageViewCountSum.get()) / (threadNumberSum.get() + averageViewCountSum.get() + averageVoteCountSum.get())) + result.getIntValue("averageVoteCount") * (((double) averageVoteCountSum.get()) / (threadNumberSum.get() + averageViewCountSum.get() + averageVoteCountSum.get())));
                case 1 -> result.getIntValue("threadNumber");
                case 2 -> result.getIntValue("averageViewCount");
                case 3 -> result.getIntValue("averageVoteCount");
                default -> 0;
            }));
        }
        if ((metric != -1) && (limit != -1)) {
            resultStream = resultStream.limit(limit);
        }
        List<JSONObject> resultList = resultStream.collect(Collectors.toList());
        JSONObject resultJSONObject = new JSONObject();
        resultJSONObject.put("popularity", resultList);
        return Result.success(response, resultJSONObject);
    }

    @GetMapping("popularity/calculateMetricsForAlTags")
    public Result calculateMetricsForAlTags(HttpServletResponse response) {
        int coreNumber = Runtime.getRuntime().availableProcessors();
        int tagNumber = (int) tagService.count();
        int size = tagNumber / coreNumber + 1;
        for (int i = 1; i <= coreNumber; i++) {
            TopicPopularityCalculator topicPopularityCalculator = new TopicPopularityCalculator(tagService, questionService, i, size);
            Thread topicPopularityCalculatorThread = new Thread(topicPopularityCalculator);
            topicPopularityCalculatorThread.start();
        }
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
