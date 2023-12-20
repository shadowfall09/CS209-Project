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
    private ITagService tagService;
    private IQuestionService questionService;

    private final int pageNumber;
    private final int size;

    public TopicPopularityCalculator(ITagService tagService, IQuestionService questionService, int pageNumber, int size) {
        this.tagService = tagService;
        this.questionService = questionService;
        this.pageNumber = pageNumber;
        this.size = size;
    }

    @Override
    public void run() {
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
    }
}
