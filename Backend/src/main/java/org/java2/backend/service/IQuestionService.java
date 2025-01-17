package org.java2.backend.service;

import org.java2.backend.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IQuestionService extends IService<Question> {
    List<String> getIdsByTagIds(List<Integer> tagIdList);
    int getQuestionCount2023ByIds(List<String> questionIdList);
    int getAverageViewCountByIds(List<String> questionIdList);
    int getAverageVoteCountByIds(List<String> questionIdList);
    int getdiscussionPeopleNumberByIds(List<String> questionIdList);
}
