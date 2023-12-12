package org.java2.backend.service.impl;

import org.java2.backend.entity.Answer;
import org.java2.backend.mapper.AnswerMapper;
import org.java2.backend.service.IAnswerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements IAnswerService {

}
