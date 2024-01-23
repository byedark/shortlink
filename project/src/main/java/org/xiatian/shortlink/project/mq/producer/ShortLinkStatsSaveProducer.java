package org.xiatian.shortlink.project.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.xiatian.shortlink.project.mq.domain.MessageWrapper;

import static org.xiatian.shortlink.project.common.constant.RocketMQConstant.SHORT_LINK_STATS_QUEUE_TAG_KEY;
import static org.xiatian.shortlink.project.common.constant.RocketMQConstant.SHORT_LINK_STATS_QUEUE_TOPIC_KEY;

/**
 * 短链接监控状态保存消息队列生产者
 * ShortLinkStatsRecordDTO消息发送实体
 */
@Slf4j
@Component
public class ShortLinkStatsSaveProducer extends AbstractCommonSendProduceTemplate<ShortLinkStatsRecordDTO>{

    /**
     * 发送延迟消费短链接统计
     */
    public ShortLinkStatsSaveProducer(@Autowired RocketMQTemplate rocketMQTemplate) {
        super(rocketMQTemplate);
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(ShortLinkStatsRecordDTO messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("监控消息")
                .keys(messageSendEvent.getKeys())
                .topic(SHORT_LINK_STATS_QUEUE_TOPIC_KEY)
                .tag(SHORT_LINK_STATS_QUEUE_TAG_KEY)
                .sentTimeout(2000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(ShortLinkStatsRecordDTO messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        return MessageBuilder
                .withPayload(new MessageWrapper<>(requestParam.getKeys(), messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
