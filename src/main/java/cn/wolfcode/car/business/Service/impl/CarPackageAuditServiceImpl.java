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


    // ???????????????
    @Override
    @Transactional
    public InputStream processImg(Long id) {
        // ??????id???????????????
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // ?????????????????? ????????????
        List<String> highlightList = Collections.emptyList();
        // ????????????????????????
        if (CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            // ??????????????????,????????????????????????
            highlightList = runtimeService.getActiveActivityIds(carPackageAudit.getInstanceId());
        }
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(carPackageAudit.getBpmnInfoId());
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();  // ????????????????????????
        InputStream inputStream = diagramGenerator.generateDiagram(repositoryService.getBpmnModel(bpmnInfo.getActProcessId()),// bpmnModel
                highlightList,// ??????????????????
                Collections.emptyList(),// ??????????????????
                "????????????", "????????????", "????????????"  // ??????
        );
        return inputStream;
    }


    // ??????
    @Override
    @Transactional
    public void cancelApply(Long id) {
        // ??????id????????????
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // ??????????????????????????????,  ??????????????????
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
            throw new BusinessException("????????????");
        }
        // ??????carPackageAudit???????????????????????????
        carPackageAudit.setStatus(CarPackageAudit.STATUS_CANCEL);
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
        // ?????? ?????????????????? ??? ?????????
        serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_INIT);
        // ??????????????????
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(),"?????????");
    }

    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        // ??????id ??? CarPackageAudit ?????????????????????  ??????????????????
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // ?????????????????????????????? , ??????????????????
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())){
           throw new BusinessException("????????????");
        }
        // ??????????????????id  instanceId ????????????????????????
        Task task = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        // ???????????????????????? auditStatus
        Map<String,Object> map = new HashMap<>();
        map.put("auditStatus",auditStatus);
        // ?????? activiti ???api???????????????
        taskService.complete(task.getId(),map);
        // ??????????????????
        carPackageAudit.setAuditTime(new Date());
        // ?????????????????????
        if (CarPackageAudit.STATUS_REJECT.equals(auditStatus)){
            // ???????????????????????????????????????
            serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_REPLY);
            // ??????CarPackageAudit ????????????????????????
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
            // ?????????????????? ??????
            carPackageAudit.setInfo(carPackageAudit.getInfo() + " ?????????: " + ShiroUtils.getUser().getUserName() + "-?????? -??????: " + info);
            // ??????CarPackageAudit ????????????
            carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
            return;
        }
        // ???????????????????????????
        Task newTask = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        // ?????????????????? ??????
        carPackageAudit.setInfo(carPackageAudit.getInfo() + " ?????????: " + ShiroUtils.getUser().getUserName() + "-?????? -??????: " + info);
        // ???????????????????????????
        if (newTask == null){
            // ??????????????????????????? ????????????
            serviceItemMapper.updateAuditStatus(carPackageAudit.getServiceItemId(),ServiceItem.AUDITSTATUS_APPROVED);
            // ??????CarPackageAudit ???????????????????????? , ????????????????????????
            carPackageAudit.setStatus(CarPackageAudit.STATUS_PASS);
            // ??????CarPackageAudit ????????????
            carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
            return;
        }
        // ?????????????????????
        // ????????????????????????????????????carPackageAudit???
        carPackageAudit.setAuditorId(Long.parseLong(newTask.getAssignee()));
        // ??????CarPackageAudit
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }

}
