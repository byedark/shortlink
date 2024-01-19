package org.xiatian.shortlink.project.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xiatian.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.xiatian.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import org.xiatian.shortlink.project.service.LinkStatsTodayService;

/**
 * 短链接今日统计接口实现层
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
