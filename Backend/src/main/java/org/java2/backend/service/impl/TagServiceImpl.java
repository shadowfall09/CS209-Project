package org.java2.backend.service.impl;

import org.java2.backend.entity.Tag;
import org.java2.backend.mapper.TagMapper;
import org.java2.backend.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

}
