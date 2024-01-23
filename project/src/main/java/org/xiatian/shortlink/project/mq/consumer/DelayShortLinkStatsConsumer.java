package org.xiatian.shortlink.project.mq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.common.convention.exception.ServiceException;
import org.xiatian.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.xiatian.shortlink.project.mq.domain.MessageWrapper;
import org.xiatian.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import org.xiatian.shortlink.project.service.RedirectService;

import static org.xiatian.shortlink.project.common.constant.RocketMQConstant.*;

/**
 * 延迟记录短链接统计组件
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = SHORT_LINK_DELAY_QUEUE_TOPIC_KEY,
        selectorExpression = SHORT_LINK_DELAY_QUEUE_TAG_KEY,
        consumerGroup = SHORT_LINK_DELAY_QUEUE_GROUP_KEY
)
public class DelayShortLinkStatsConsumer implements RocketMQListener<MessageWrapper<ShortLinkStatsRecordDTO>> {

    private final RedirectService redirectService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Override
    public void onMessage(MessageWrapper<ShortLinkStatsRecordDTO> message) {
        ShortLinkStatsRecordDTO statsRecord = message.getMessage();
        //通过循环保证始终消费成功之前不会退出循环
        for (; ; ) {
            try {
                if (!messageQueueIdempotentHandler.isMessageProcessed(statsRecord.getKeys())) {
                    // 判断当前的这个消息流程是否执行完成
                    if (messageQueueIdempotentHandler.isAccomplish(statsRecord.getKeys())) {
                        return;
                    }
                    throw new ServiceException("消息未完成流程，需要消息队列重试");
                }
                try {
                    redirectService.shortLinkStats(statsRecord);
                } catch (Throwable ex) {
                    messageQueueIdempotentHandler.delMessageProcessed(statsRecord.getKeys());
                    log.error("延迟记录短链接监控消费异常", ex);
                }
                messageQueueIdempotentHandler.setAccomplish(statsRecord.getKeys());
            } catch (Throwable ignored) {
            }
        }
    }
}
