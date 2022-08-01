package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarPackageAuditQuery extends QueryObject {
    // 创建人
    private String creatorName;
    // 审核人Id
    private Long auditorId;
    // 审核状态
    private Integer status;
    // 备注模糊查询名字
    private String name;
}
