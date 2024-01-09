package org.xiatian.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

import static org.xiatian.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;


/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接缓存有效期时间
     *
     * @param validDate 有效期时间
     * @return 有限期时间戳
     */
    public static long getLinkCacheValidTime(Date validDate) {
        //增加一个随机值，防止同一时间段创建，过期时间一样，导致大量key同时过期造成数据库压力过大
        Random random = new Random();
        int randomNumber = random.nextInt(100000);
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME+randomNumber);
    }
}
