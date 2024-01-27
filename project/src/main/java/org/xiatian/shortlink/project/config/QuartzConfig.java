package org.xiatian.shortlink.project.config;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xiatian.shortlink.project.quartz.job.ShortLinkTimeScanJob;

import static org.xiatian.shortlink.project.quartz.constant.ScanCronExpresson.SCAN_DB_CRON;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail scanDbJob(){
        return JobBuilder.newJob(ShortLinkTimeScanJob.class)
                .storeDurably()
                .withIdentity("scan_job","group1")
                .build();
    }

    @Bean
    public Trigger scanTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity("scan_trigger","group1")
                .forJob(scanDbJob())
                .withSchedule(CronScheduleBuilder.cronSchedule(SCAN_DB_CRON))
                .build();
    }
}
