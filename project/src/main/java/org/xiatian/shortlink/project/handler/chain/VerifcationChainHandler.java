package org.xiatian.shortlink.project.handler.chain;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.xiatian.shortlink.project.common.convention.exception.ClientException;
import org.xiatian.shortlink.project.config.GotoDomainWhiteListConfiguration;
import org.xiatian.shortlink.project.handler.ShortLinkChainHandler;
import org.xiatian.shortlink.project.toolkit.LinkUtil;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VerifcationChainHandler implements ShortLinkChainHandler<String> {

    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Override
    public void handler(String requestParam, ServletRequest request, ServletResponse response) {
        verificationWhitelist(request);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 白名单验证
     * 扩展功能
     */
    private void verificationWhitelist(ServletRequest request) {
        String ip = LinkUtil.getActualIp((HttpServletRequest) request);
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(ip)) {
            throw new ClientException("访问失败");
        }
    }
}
