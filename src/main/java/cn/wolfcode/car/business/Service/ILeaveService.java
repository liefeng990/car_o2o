package cn.wolfcode.car.business.Service;

import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.business.vo.LeaveVo;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface ILeaveService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<Leave> query(LeaveQuery qo);


    
    /**
     * 查询全部
     * @return
     */
    List<Leave> list();


    /**
     *  发起申请
     * @param vo
     */
    void startAudit(LeaveVo vo);

    /**
     * 审批
     * @param id
     * @param auditStatus
     * @param info
     */
    void audit(Long id, Integer auditStatus, String info);
}
