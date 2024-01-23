package org.xiatian.shortlink.admin.remote.dto.req.recyclebin;

import lombok.Data;

/**
 * 回收站移除功能
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
