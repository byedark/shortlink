package org.xiatian.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xiatian.shortlink.admin.common.convention.result.Result;
import org.xiatian.shortlink.admin.common.convention.result.Results;
import org.xiatian.shortlink.admin.remote.ShortLinkRemoteService;
import org.xiatian.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.xiatian.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.xiatian.shortlink.admin.remote.dto.req.create.ShortLinkBatchCreateReqDTO;
import org.xiatian.shortlink.admin.remote.dto.req.create.ShortLinkCreateReqDTO;
import org.xiatian.shortlink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import org.xiatian.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import org.xiatian.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.xiatian.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.xiatian.shortlink.admin.toolkit.EasyExcelWebUtil;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkRemoteService shortLinkRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            //写到的为HttpServletResponse
            EasyExcelWebUtil.write(response, "all-shortlink", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam.getGid(),
                requestParam.getOrderTag(), requestParam.getCurrent(), requestParam.getSize());
    }
}
