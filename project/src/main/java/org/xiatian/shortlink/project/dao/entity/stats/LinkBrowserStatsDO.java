package org.xiatian.shortlink.project.dao.entity.stats;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.xiatian.shortlink.project.common.database.BaseDO;

import java.util.Date;

/**
 * 浏览器统计访问实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_link_browser_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkBrowserStatsDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 浏览器
     */
    private String browser;
}
