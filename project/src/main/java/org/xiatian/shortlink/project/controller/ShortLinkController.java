package org.xiatian.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xiatian.shortlink.project.common.convention.result.Result;
import org.xiatian.shortlink.project.common.convention.result.Results;
import org.xiatian.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.xiatian.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.xiatian.shortlink.project.service.ShortLinkService;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;
    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

}
