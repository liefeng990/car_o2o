package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.ICarPackageAuditService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class CarPackageAuditServiceImpl implements ICarPackageAuditService {

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;
    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;
    @Autowired
    private ServiceItemMapper serviceItemMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;


    @Override
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));
    }


    @Override
    public CarPackageAudit get(Long id) {
        return carPackageAuditMapper.selectByPrimaryKey(id);
    }



    @Override
    public List<CarPackageAudit> list() {
        return carPackageAuditMapper.selectAll();
    }


    // 流程进度图
    @Override
    @Transactional
    public InputStream processImg(Long id) {
        // 根据id先获取数据
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // 创建高亮节点 默认为空
        List<String> highlightList = Collections.emptyList();
        // 判断是否为审核中
        if (CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            // 如果为审核中,则给高亮节点赋值
            highlightList = runtimeService.getActiveActivityIds(carPackageAudit.getInstanceId());
        }
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(carPackageAudit.getBpmnInfoId());
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();  // 标准流程图发生器
        InputStream inputStream = diagramGenerator.generateDiagram(repositoryService.getBpmnModel(bpmnInfo.getActProcessId()),// bpmnModel
                highlightList,// 高亮节点集合
                Collections.emptyList(),// 高亮连线集合
                "方正楷体", "方正楷体", "方正楷体"  // 字体
        );
        return inputStream;
    }


    // 撤销
    @Override
    @Transactional
    public void cancelApply(Long id) {
        // 根据id查询数据
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // 判断状态是否在审核中,  否则抛出异常
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            throw new BusinessException("非法操作");
        }
        // 修改carPackageAudit状态修改成撤销状态
        carPackageAudit.setStatus(CarPackageAudit.STATUS_CANCEL);
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
        // 修改 服务单项状态 为 初始化
        serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_INIT);
        // 删除流程实例
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(),"撤销了");
    }

    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        // 根据id 将 CarPackageAudit 套餐审核对象表  信息查询出来
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // 判断状态是否为审核中 , 否则抛出异常
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
           throw new BusinessException("非法操作");
        }
        // 根据流程实例id  instanceId 查询当前任务节点
        Task task = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        // 需要设置流程变量 auditStatus
        Map<String,Object> map = new HashMap<>();
        map.put("auditStatus",auditStatus);
        // 调用 activiti 的api去完成任务
        taskService.complete(task.getId(),map);
        // 修改审核时间
        carPackageAudit.setAuditTime(new Date());
        // 传入状态为拒绝
        if (CarPackageAudit.STATUS_REJECT.equals(auditStatus)){
            // 修改服务单项状态为重新调整
            serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_REPLY);
            // 修改CarPackageAudit 的状态为审核拒绝
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
            // 修改备注信息 拒绝
            carPackageAudit.setInfo(carPackageAudit.getInfo() + " 审核人: " + ShiroUtils.getUser().getUserName() + "-拒绝 -备注: " + info);
            // 修改CarPackageAudit 结束方法
            carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
            return;
        }
        // 获取下一个任务节点
        Task newTask = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        // 修改备注信息 同意
        carPackageAudit.setInfo(carPackageAudit.getInfo() + " 审核人: " + ShiroUtils.getUser().getUserName() + "-同意 -备注: " + info);
        // 下一个任务节点为空
        if (newTask == null){
            // 修改服务单项状态为 审核通过
            serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_APPROVED);
            // 修改CarPackageAudit 的状态为审核通过 , 添加审核备注信息
            carPackageAudit.setStatus(CarPackageAudit.STATUS_PASS);
            // 修改CarPackageAudit 结束方法
            carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
            return;
        }
        // 存在下一个节点
        // 把下个节点的审核人修改进carPackageAudit中
        carPackageAudit.setAuditorId(Long.parseLong(newTask.getAssignee()));
        // 修改CarPackageAudit
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }

}
