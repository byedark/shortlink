package org.xiatian.shortlink.project.common.constant;

/**
 * RocketMQ 常量类
 */
public class RocketMQConstant {

    //名字之间用_,单词之间用-
    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_QUEUE_TOPIC_KEY = "short-link_stats-queue";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_QUEUE_GROUP_KEY = "short-link_stats-queue_only-group";

    /**
     * 短链接监控消息保存队列 Tag Key
     */
    public static final String SHORT_LINK_STATS_QUEUE_TAG_KEY = "short-link_stats-queue_tag";

    /**
     * 短链接监控消息延迟消息队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_DELAY_QUEUE_TOPIC_KEY = "short-link_delay-queue";

    /**
     * 短链接监控消息延迟消息队列 Group 缓存标识
     */
    public static final String SHORT_LINK_DELAY_QUEUE_GROUP_KEY = "short-link_delay-queue_only-group";

    /**
     * 短链接监控消息保存队列 Tag Key
     */
    public static final String SHORT_LINK_DELAY_QUEUE_TAG_KEY = "short-link_delay-queue_tag";
}
