package org.xiatian.shortlink.project.handler;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.xiatian.shortlink.project.common.base.ApplicationContextHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ShortLinkChainContext<T> implements CommandLineRunner {
    
    private final List<ShortLinkChainHandler> orderCreateChainHandlerContainer = new ArrayList<>();

    /**
     * 责任链组件执行
     *
     * @param requestParam 请求参数
     */
    public void handler(T requestParam, ServletRequest servletRequest, ServletResponse servletResponse) {
        // 此处根据 Ordered 实际值进行排序处理
        orderCreateChainHandlerContainer.stream()
                .sorted(Comparator.comparing(Ordered::getOrder)).forEach(each -> each.handler(requestParam, servletRequest, servletResponse));
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 通过 Spring 上下文容器，获取所有 CreateOrderChainContext Bean
        Map<String, ShortLinkChainHandler> chainFilterMap = ApplicationContextHolder.getBeansOfType(ShortLinkChainHandler.class);
        // 将对应 Bean 放入责任链上下文容器中
        chainFilterMap.forEach((beanName, bean) -> orderCreateChainHandlerContainer.add(bean));
    }
}