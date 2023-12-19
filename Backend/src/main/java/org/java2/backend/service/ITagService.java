package org.java2.backend.service;

import org.java2.backend.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ITagService extends IService<Tag> {
    List<Integer> getIdsByKeyword(String keyword);
}
