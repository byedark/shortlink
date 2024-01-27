package org.xiatian.shortlink.project.common.constant;

/**
 * 短链接常量类
 */
public class ShortLinkConstant {

    /**
     * 永久短链接默认缓存有效时间，默认一个月
     */
    public static final long DEFAULT_CACHE_VALID_TIME = 2626560000L;

    /**
     * 短链接一天的有效期
     */
    public static final long DEFAULT_CACHE_ONE_DAY_TIME = 86400000L;

    /**
     * 深分页默认一次查1000条
     */
    public static final int DEFAULT_LINK_SCAN_NUM = 1000;

    /**
     * 高德获取地区接口地址
     */
    public static final String AMAP_REMOTE_URL = "https://restapi.amap.com/v3/ip";
}
