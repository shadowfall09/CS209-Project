package org.java2.backend.service.impl;

import org.java2.backend.entity.Comment;
import org.java2.backend.mapper.CommentMapper;
import org.java2.backend.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
