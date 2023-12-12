package org.java2.backend.service.impl;

import org.java2.backend.entity.Question;
import org.java2.backend.mapper.QuestionMapper;
import org.java2.backend.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

}
