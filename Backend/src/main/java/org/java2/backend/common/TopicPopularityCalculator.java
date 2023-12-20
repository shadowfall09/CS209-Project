package org.java2.backend.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.java2.backend.entity.Tag;
import org.java2.backend.service.IQuestionService;
import org.java2.backend.service.ITagService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TopicPopularityCalculator implements Runnable{
    private final ITagService tagService;
    private final IQuestionService questionService;

    private final boolean taskType;
    private final int pageNumber;
    private final int size;

    public TopicPopularityCalculator(ITagService tagService, IQuestionService questionService, boolean taskType, int pageNumber, int size) {
        this.tagService = tagService;
        this.questionService = questionService;
        this.taskType = taskType;
        this.pageNumber = pageNumber;
        this.size = size;
    }

    @Override
    public void run() {
        if (taskType) {
            List<Tag> tagList = tagService.page(new Page<>(pageNumber, size), new QueryWrapper<Tag>().select("id").orderByAsc("id")).getRecords();
            int currentTagNumber = 1;
            for (Tag tag : tagList) {
                Tag tagUpdate = new Tag();
                tagUpdate.setId(tag.getId());
                List<Integer> relatedTagIdList = new ArrayList<>();
                relatedTagIdList.add(tag.getId());
                List<String> questionIdList = questionService.getIdsByTagIds(relatedTagIdList);
                tagUpdate.setThreadNumber(questionIdList.size());
                tagUpdate.setThreadNumber2023(questionService.getQuestionCount2023ByIds(questionIdList));
                tagUpdate.setAverageViewCount(questionService.getAverageViewCountByIds(questionIdList));
                tagUpdate.setAverageVoteCount(questionService.getAverageVoteCountByIds(questionIdList));
                tagUpdate.setDiscussionPeopleNumber(questionService.getdiscussionPeopleNumberByIds(questionIdList));
                tagService.updateById(tagUpdate);
                log.info("TopicPopularityCalculator: " + pageNumber + ", size: " + size + ", current: " + currentTagNumber + ", id: " + tag.getId());
                currentTagNumber++;
            }
            log.info("TopicPopularityCalculator: " + pageNumber + " finish");
        }
        else {
            Tag sum = tagService.getOne(new QueryWrapper<Tag>().select("sum(thread_number) as thread_number, sum(thread_number_2023) as thread_number2023, sum(average_view_count) as average_view_count, sum(average_vote_count) as average_vote_count, sum(discussion_people_number) as discussion_people_number"));
            int totalSum = sum.getThreadNumber() + sum.getThreadNumber2023() + sum.getAverageViewCount() + sum.getAverageVoteCount() + sum.getDiscussionPeopleNumber();
            tagService.list(new QueryWrapper<Tag>().orderByAsc("id")).forEach(tag -> {
                Tag tagUpdate = new Tag();
                tagUpdate.setId(tag.getId());
                tagUpdate.setComprehensiveScore((int)(tag.getThreadNumber() * (((double) sum.getThreadNumber()) / totalSum) + tag.getThreadNumber2023() * (((double) sum.getThreadNumber2023()) / totalSum) + tag.getAverageViewCount() * (((double) sum.getAverageViewCount()) / totalSum) + tag.getAverageVoteCount() * (((double) sum.getAverageVoteCount()) / totalSum) + tag.getDiscussionPeopleNumber() * (((double) sum.getDiscussionPeopleNumber()) / totalSum)));
                tagService.updateById(tagUpdate);
            });
            log.info("CalculateComprehensiveScoreForAllTags: finish");
        }
    }
}
