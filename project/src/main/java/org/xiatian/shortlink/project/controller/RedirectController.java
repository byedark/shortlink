package org.xiatian.shortlink.project.controller;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.xiatian.shortlink.project.service.RedirectService;

/**
 * 短链接转跳控制层
 */
@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final RedirectService redirectService;

    /**
     * 核心功能
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        redirectService.restoreUrl(shortUri, request, response);
    }
}
