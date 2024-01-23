package org.xiatian.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.xiatian.shortlink.project.dao.entity.ShortLinkDO;
import org.xiatian.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;

/**
 * 短链接转跳服务
 */
public interface RedirectService extends IService<ShortLinkDO> {

    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 短链接统计
     *
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(ShortLinkStatsRecordDTO shortLinkStatsRecord);
}
