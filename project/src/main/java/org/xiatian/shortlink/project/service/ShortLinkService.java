package org.xiatian.shortlink.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.xiatian.shortlink.project.dao.entity.ShortLinkDO;
import org.xiatian.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.xiatian.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接,借助布隆过滤器
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);
}
