package org.java2.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.java2.backend.entity.Answer;
import org.java2.backend.entity.Question;
import org.java2.backend.entity.QuestionTagRelation;
import org.java2.backend.mapper.AnswerMapper;
import org.java2.backend.mapper.QuestionMapper;
import org.java2.backend.mapper.QuestionTagRelationMapper;
import org.java2.backend.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {
    @Resource
    private QuestionTagRelationMapper questionTagRelationMapper;
    @Resource
    private AnswerMapper answerMapper;

    @Override
    public List<String> getIdsByTagIds(List<Integer> tagIdList) {
        if (tagIdList.isEmpty()) {
            return new ArrayList<>();
        }
        else {
            return questionTagRelationMapper.selectList(new QueryWrapper<QuestionTagRelation>().in("tag_id", tagIdList)).stream().map(QuestionTagRelation::getQuestionId).collect(Collectors.toList());
        }
    }

    @Override
    public int getAverageViewCountByIds(List<String> questionIdList) {
        if (questionIdList.isEmpty()) {
            return 0;
        }
        else {
            return baseMapper.selectList(new QueryWrapper<Question>().select("view_count").in("id", questionIdList)).stream().map(Question::getViewCount).reduce(0, Integer::sum) / questionIdList.size();
        }
    }

    @Override
    public int getAverageVoteCountByIds(List<String> questionIdList) {
        if (questionIdList.isEmpty()) {
            return 0;
        }
        else {
            int count = baseMapper.selectList(new QueryWrapper<Question>().select("(up_vote + down_vote) as up_vote").in("id", questionIdList)).stream().map(Question::getUpVote).reduce(0, Integer::sum);
            count += answerMapper.selectList(new QueryWrapper<Answer>().select("(up_vote + down_vote) as up_vote").in("question_id", questionIdList)).stream().map(Answer::getUpVote).reduce(0, Integer::sum);
            return count / questionIdList.size();
        }
    }
}
