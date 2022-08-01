package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.IServiceItemService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.vo.ServiceItemVo;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ServiceItemServiceImpl implements IServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;
    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;
    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;
    @Autowired
    private RuntimeService runtimeService;


    @Override
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }


    @Override
    public void save(ServiceItem serviceItem) {
        // 设置上架时间
        serviceItem.setCreateTime(new Date());
        // 设置上架状态为未上架
        serviceItem.setSaleStatus(ServiceItem.SALESTATUS_OFF);
        // 判断是否为餐套
        if (ServiceItem.CARPACKAGE_YES.equals(serviceItem.getCarPackage())){
            // 是, 设置审核状态为初始化
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        } else if (ServiceItem.CARPACKAGE_NO.equals(serviceItem.getCarPackage())){
            // 否, 设置审核状态为无需审核
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_NO_REQUIRED);
        }
        serviceItemMapper.insert(serviceItem);
    }

    @Override
    public ServiceItem get(Long id) {
        return serviceItemMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(ServiceItem serviceItem) {
        ServiceItem oldService = serviceItemMapper.selectByPrimaryKey(serviceItem.getId());

        // 如果上架状态为已上架  抛出异常非法操作
        if (ServiceItem.SALESTATUS_ON.equals(serviceItem.getSaleStatus())){
            throw new BusinessException("非法操作");
        }

        // 如果是套餐且未审核完成, 抛出异常非法操作
        if (ServiceItem.CARPACKAGE_YES.equals(oldService.getCarPackage())  && !ServiceItem.AUDITSTATUS_APPROVED.equals(oldService.getAuditStatus())){
            throw new BusinessException("非法操作");
        }

        // 设置审核状态为原先状态
        serviceItem.setAuditStatus(oldService.getAuditStatus());

        // 如果是套餐且审核状态为审核完毕, 将审核状态设置为初始化
        if (ServiceItem.CARPACKAGE_YES.equals(serviceItem.getCarPackage()) && ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())){
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        }

        serviceItemMapper.updateByPrimaryKey(serviceItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            serviceItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<ServiceItem> list() {
        return serviceItemMapper.selectAll();
    }

    @Override
    public void saleOn(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        // 如果已经上架了,抛出异常
        if (ServiceItem.SALESTATUS_ON.equals(serviceItem.getSaleStatus())){
            throw new BusinessException("已经上架了");
        }
        // 如果是套餐且不为审核通过状态 抛出异常
        if (ServiceItem.CARPACKAGE_YES.equals(serviceItem.getCarPackage()) && !ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())){
            serviceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        }
        // 修改为上架状态
        serviceItemMapper.changeSaleStatus(ServiceItem.SALESTATUS_ON,id);
    }

    @Override
    public void saleOff(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        // 如果已经是下架抛出异常
        if (ServiceItem.SALESTATUS_OFF.equals(serviceItem.getSaleStatus())){
            throw new BusinessException("已经下架了");
        }
        // 修改为下架状态
        serviceItemMapper.changeSaleStatus(ServiceItem.SALESTATUS_OFF,id);
    }

    @Override
    @Transactional
    public void startAudit(ServiceItemVo vo) {
        // 获取服务单项信息
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(vo.getId());
        // 保存数据
        CarPackageAudit carPackageAudit = new CarPackageAudit();
        carPackageAudit.setServiceItemId(serviceItem.getId());              // 服务单项id
        carPackageAudit.setServiceItemInfo(serviceItem.getInfo());          // 服务单项备注
        carPackageAudit.setServiceItemPrice(serviceItem.getDiscountPrice());// 服务单项审核价格
        carPackageAudit.setCreator(ShiroUtils.getUser().getUserName());     // 创建人
        carPackageAudit.setInfo(vo.getInfo());                              // 备注信息
        carPackageAudit.setAuditorId(vo.getDirector());                     // 当前审核人id
        carPackageAudit.setCreateTime(new Date());                          // 创建时间
        carPackageAudit.setBpmnInfoId(vo.getBpmnInfoId());                  // 关联流程id
        // 数据保存进数据库
        carPackageAuditMapper.insert(carPackageAudit);
        // 增加流程信息   店长 财务  折扣金额
        Map<String,Object> map = new HashMap<>();
        map.put("director",vo.getDirector());
        if (vo.getFinance() != null){
            map.put("finance",vo.getFinance());
        }
        map.put("discountPrice",serviceItem.getDiscountPrice().longValue());
        // 获取bpmnInfo对象来获取其中的 activity流程定义生成的key
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(vo.getBpmnInfoId());
        // 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(bpmnInfo.getActProcessKey(), carPackageAudit.getId().toString(), map);
        // 获取流程实例id到carPackageAudit中
        carPackageAudit.setInstanceId(processInstance.getProcessInstanceId());
        // 修改carPackageAudit表信息
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
        // 修改服务单项为审核中
        serviceItemMapper.updateAuditStatus(serviceItem.getId(),ServiceItem.AUDITSTATUS_AUDITING);
    }

}
