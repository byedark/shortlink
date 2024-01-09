package org.xiatian.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiatian.shortlink.project.common.convention.result.Result;
import org.xiatian.shortlink.project.common.convention.result.Results;
import org.xiatian.shortlink.project.service.UrlTitleService;

/**
 * URL 标题控制层
 * 获取原始网站的title和图标
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    private final UrlTitleService urlTitleService;

    /**
     * 根据 URL 获取对应网站的标题
     */
    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
