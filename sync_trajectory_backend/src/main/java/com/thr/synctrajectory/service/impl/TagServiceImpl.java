package com.thr.synctrajectory.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.synctrajectory.mapper.TagMapper;
import com.thr.synctrajectory.model.domain.Tag;
import com.thr.synctrajectory.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author thr
 * @description 针对表【tag(标签)】的数据库操作Service实现
 * @createDate 2024-03-08 19:06:48
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

}




