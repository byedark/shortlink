package org.xiatian.shortlink.project.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.xiatian.shortlink.project.common.convention.exception.ServiceException;
import org.xiatian.shortlink.project.dao.entity.ShortLinkDO;
import org.xiatian.shortlink.project.dao.mapper.ShortLinkMapper;
import org.xiatian.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.xiatian.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.xiatian.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.xiatian.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.xiatian.shortlink.project.service.ShortLinkService;
import org.xiatian.shortlink.project.toolkit.HashUtil;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        //将requestParam也就是原始链接相关的信息存入DO中,toBean难以可视化
        //ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().domain(requestParam.getDomain())
                .domain(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        try{
            baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException ex){
            //由于布隆过滤器有一定的概率会误判，所以一旦出现了唯一索引报错的问题就去数据库看是不是真的存在这个数据
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShortLinkDO::getFullShortUrl,fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if(hasShortLinkDO != null) {
                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        //加入布隆过滤器，考虑布隆过滤器无法删除，可以加一个计数器，虽然不能完全解决但能一定程度上减轻影响
        shortUriCreateCachePenetrationBloomFilter.add(shortLinkSuffix);
        //建造者模式构建Response_DTO
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = new LambdaQueryWrapper<>();
        lambdaQuery().eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam,queryWrapper);
        return resultPage.convert(each-> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    /**
     * 短链接生成算法
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam){
        int customGenerateCount = 0;
        String shorUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shorUri = HashUtil.hashToBase62(originUrl);
            //可能存在误判，但是由于url是唯一索引所以导致了
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shorUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shorUri;
    }
}
