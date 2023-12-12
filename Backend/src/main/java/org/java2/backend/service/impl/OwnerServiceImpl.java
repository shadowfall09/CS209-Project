package org.java2.backend.service.impl;

import org.java2.backend.entity.Owner;
import org.java2.backend.mapper.OwnerMapper;
import org.java2.backend.service.IOwnerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl extends ServiceImpl<OwnerMapper, Owner> implements IOwnerService {

}
