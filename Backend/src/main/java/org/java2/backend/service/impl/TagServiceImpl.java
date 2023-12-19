package org.java2.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.java2.backend.entity.Tag;
import org.java2.backend.mapper.TagMapper;
import org.java2.backend.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    @Override
    public List<Integer> getIdsByKeyword(String keyword) {
        return baseMapper.selectList(new QueryWrapper<Tag>().like("tag_name", keyword)).stream().map(Tag::getId).collect(Collectors.toList());
    }
}
