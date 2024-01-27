package org.xiatian.shortlink.project.quartz.job;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.dao.entity.ShortLinkDO;
import org.xiatian.shortlink.project.dao.mapper.ShortLinkMapper;

import java.util.Date;
import java.util.List;

import static org.xiatian.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_LINK_SCAN_NUM;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class ShortLinkTimeScanJob implements Job {

    public final ShortLinkMapper shortLinkMapper;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        //TODO: 扫描数据库的过期任务细节
        //if 当前时间大于设定时间，直接设置过期
        //同时需要保存缓存一致性
        long flagUuid = 0L;
        while(true){
            List<ShortLinkDO> listTime = shortLinkMapper.selectScanLink(flagUuid, DEFAULT_LINK_SCAN_NUM);
            if(listTime.size()==0) break;
            listTime.forEach(each -> {
                if(each.getValidDate()!=null && each.getValidDate().before(new Date())){
                    each.setDelFlag(1);
                    shortLinkMapper.updateById(each);
                }
            });
            //更新id
            flagUuid = listTime.get(listTime.size()-1).getId();
        }
    }
}
