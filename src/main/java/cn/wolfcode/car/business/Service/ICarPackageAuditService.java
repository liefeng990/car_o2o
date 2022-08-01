package cn.wolfcode.car.business.Service;

import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.io.InputStream;
import java.util.List;

/**
 * 岗位服务接口
 */
public interface ICarPackageAuditService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo);
    
    /**
     * 查单个
     * @param id
     * @return
     */
    CarPackageAudit get(Long id);

    
    /**
     * 查询全部
     * @return
     */
    List<CarPackageAudit> list();

    /**
     * 查看进度图
     * @param id
     * @return
     */
    InputStream processImg(Long id);

    /**
     * 撤销
     * @param id
     */
    void cancelApply(Long id);

    /**
     * 审批确定
     * @param id
     * @param auditStatus
     * @param info
     */
    void audit(Long id, Integer auditStatus, String info);
}
