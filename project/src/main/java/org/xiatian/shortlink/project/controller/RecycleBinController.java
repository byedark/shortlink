package org.xiatian.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

//    private final RecycleBinService recycleBinService;
//
//    /**
//     * 保存回收站
//     */
//    @PostMapping("/api/short-link/v1/recycle-bin/save")
//    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
//        recycleBinService.saveRecycleBin(requestParam);
//        return Results.success();
//    }

//    /**
//     * 分页查询回收站短链接
//     */
//    @GetMapping("/api/short-link/v1/recycle-bin/page")
//    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
//        return Results.success(recycleBinService.pageShortLink(requestParam));
//    }
//
//    /**
//     * 恢复短链接
//     */
//    @PostMapping("/api/short-link/v1/recycle-bin/recover")
//    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
//        recycleBinService.recoverRecycleBin(requestParam);
//        return Results.success();
//    }
//
//    /**
//     * 移除短链接
//     */
//    @PostMapping("/api/short-link/v1/recycle-bin/remove")
//    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
//        recycleBinService.removeRecycleBin(requestParam);
//        return Results.success();
//    }
}
