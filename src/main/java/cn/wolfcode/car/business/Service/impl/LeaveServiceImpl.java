package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.ILeaveService;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.mapper.LeaveMapper;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.business.vo.LeaveVo;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LeaveServiceImpl implements ILeaveService {

    @Autowired
    private LeaveMapper leaveMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;


    @Override
    public TablePageInfo<Leave> query(LeaveQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Leave>(leaveMapper.selectForList(qo));
    }


    @Override
    public List<Leave> list() {
        return leaveMapper.selectAll();
    }

    @Override
    public void startAudit(LeaveVo vo) {
        // 保存请假信息
        Leave leave = new Leave();
        leave.setName(vo.getName());
        leave.setReason(vo.getReason());
        leave.setStartTime(vo.getStartTime());
        leave.setEndTime(vo.getEndTime());
        leave.setStatus("审核中");
        leave.setAuditId(vo.getDirector());
        leave.setInfo("开始审核");
        leaveMapper.insert(leave);
        // 增加流程信息
        // 增加流程信息   经理  人事
        Map<String,Object> map = new HashMap<>();
        map.put("manager",vo.getDirector());
        map.put("personnel",vo.getFinance());
        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bus_leave", leave.getId().toString(), map);
        // 添加 instanceId 进表中
        leaveMapper.updateInstanceId(leave.getId(),processInstance.getProcessInstanceId());
    }

    // 审批
    @Override
    @Transactional
    public void audit(Long id, Integer auditStatus, String info) {
        // 根据id获取信息
        Leave leave = leaveMapper.selectOne(id);
        // 判断是否为审核中.否则抛出异常
        if (!"审核中".equals(leave.getStatus())){
            throw new BusinessException("非法操作");
        }
        // 根据流程实例id  instanceId 查询当前任务节点
        Task task = taskService.createTaskQuery().processInstanceId(leave.getInstanceId()).singleResult();
        taskService.complete(task.getId());
        // 判断 同意或拒绝
        // 拒绝
        if (CarPackageAudit.STATUS_REJECT.equals(auditStatus)){
            // leave 的状态为审核拒绝
            leave.setStatus("审核拒绝");
            // 修改备注信息 拒绝
            leave.setInfo(leave.getInfo() + " 审核人: " + ShiroUtils.getUser().getUserName() + "-拒绝 -备注: " + info);
            // 修改请假信息
            leaveMapper.update(leave);
            return;
        }
        // 同意
        // 获取下个任务节点
        Task newTask = taskService.createTaskQuery().processInstanceId(leave.getInstanceId()).singleResult();
        leave.setInfo(leave.getInfo() + " 审核人: " + ShiroUtils.getUser().getUserName() + "-同意 -备注: " + info);
        // 没有下一个节点
        if (newTask == null){
            // 修改CarPackageAudit 的状态为审核通过 , 添加审核备注信息
            leave.setStatus("审核通过");
            // 修改CarPackageAudit 结束方法
            leaveMapper.update(leave);
            return;
        }
        // 存在下一个节点
        // 把下个节点的审核人修改进carPackageAudit中
        leave.setAuditId(Long.parseLong(newTask.getAssignee()));
        // 修改CarPackageAudit
        leaveMapper.update(leave);
    }

}
