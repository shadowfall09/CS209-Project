package org.java2.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.java2.backend.entity.Answer;
import org.java2.backend.entity.AnswerTagRelation;
import org.java2.backend.entity.QuestionTagRelation;
import org.java2.backend.mapper.AnswerMapper;
import org.java2.backend.mapper.AnswerTagRelationMapper;
import org.java2.backend.service.IAnswerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements IAnswerService {
    @Resource
    private AnswerTagRelationMapper answerTagRelationMapper;

    @Override
    public List<String> getIdsByTagIds(List<Integer> tagIdList) {
        if (tagIdList.isEmpty()) {
            return new ArrayList<>();
        }
        else {
            return answerTagRelationMapper.selectList(new QueryWrapper<AnswerTagRelation>().in("tag_id", tagIdList)).stream().map(AnswerTagRelation::getAnswerId).collect(Collectors.toList());
        }
    }
}
