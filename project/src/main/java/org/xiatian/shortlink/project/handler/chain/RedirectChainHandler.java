package org.xiatian.shortlink.project.handler.chain;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.dao.entity.ShortLinkDO;
import org.xiatian.shortlink.project.dao.entity.ShortLinkGotoDO;
import org.xiatian.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import org.xiatian.shortlink.project.dao.mapper.ShortLinkMapper;
import org.xiatian.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.xiatian.shortlink.project.handler.ShortLinkChainHandler;
import org.xiatian.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import org.xiatian.shortlink.project.toolkit.LinkUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.xiatian.shortlink.project.common.constant.RedisKeyConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectChainHandler implements ShortLinkChainHandler<String> {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;
    private final ShortLinkMapper shortLinkMapper;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @SneakyThrows
    @Override
    public void handler(String shortUri, ServletRequest request, ServletResponse response) {
        //短链接已经存在了布隆过滤器里面,先去布隆过滤器查询
        //缓存需要缓存链接和空值
        //这里直接根据配置文件得到完整的短链接地址
        String fullShortUrl = createShortLinkDefaultDomain + "/" + shortUri;
        //先查缓存里有没有存一份这个短链接的对应地址
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        //查询到了短链接里有对应地址，直接跳转
        if (StrUtil.isNotBlank(originalLink)) {
            //这里直接传gid就会导致gid在转跳时候不知道需要查询数据库造成数据库压力
            //TODO: 数据库查询需要优化
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(statsRecord);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        //没有查询到，查看布隆过滤器里面是否有，防止缓存穿透，查找不存在的
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(shortUri);
        if (!contains) {
            //布隆过滤器里面找不到说明这个链接的确是不存在的
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        //创建这个短链接对应的缓存，准备查找数据库并将对应的数据写入缓存
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        //有大量请求进来没有缓存就会造成缓存击穿，这时候只能让一个请求进去把缓存写好了
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //双重检查锁判定，由于线程并发都会导致找不到缓存到当前这一步，所以锁住有了缓存以后还要判定一次，以免重复查询数据库
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            //查询到了缓存进行跳转
            if (StrUtil.isNotBlank(originalLink)) {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(statsRecord);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            //由于有两次数据库的查询，第一个表都查不到就可以直接认为空了
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            //如果查询不到，说明需要缓存空值，这个值拿额外其他的头进行存储
            if (shortLinkGotoDO == null) {
                //缓存空值
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            //通过路由表已经得到了分片键gid的信息就可以去对应分表里查询数据库了
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = shortLinkMapper.selectOne(queryWrapper);
            //查询到后仍然要判断是否可以转跳，可能已经过期，这时候如果一个人访问大量过期链接依旧会导致数据库压力过大，继续加缓存
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            statsRecord.setGid(shortLinkDO.getGid());
            //调用监控函数，异步
            shortLinkStats(statsRecord);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getOrder() {
        return 20;
    }

    // 为访问短链接的人传递一个cookie记录其临时的身份信息，并获得他的所有网络设备游览器等信息返回
    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(cn.hutool.core.lang.UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            //这个缓存的主要作用是判重，判断一个用户是否已经加入，加入了就不算，因为uip和uv都是只算一次
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY  + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY  + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    // 更新短链接监控信息（根据buildLinkStatsRecordAndSetUser函数里面获取到的相关信息）
    public void shortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
        String uuid = UUID.randomUUID().toString();
        statsRecord.setKeys(uuid);
        //传入消息队列
        shortLinkStatsSaveProducer.sendMessage(statsRecord);
    }
}
