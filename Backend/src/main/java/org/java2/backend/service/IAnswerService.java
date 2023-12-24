package org.java2.backend.service;

import org.java2.backend.entity.Answer;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IAnswerService extends IService<Answer> {

    List<String> getIdsByTagIds(List<Integer> tagIdList);
}
