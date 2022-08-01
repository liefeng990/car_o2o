package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.IAppointmentService;
import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private StatementMapper statementMapper;


    @Override
    public TablePageInfo<Appointment> query(AppointmentQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Appointment>(appointmentMapper.selectForList(qo));
    }


    @Override
    public void save(Appointment appointment) {
        // 设置创建时间
        appointment.setCreateTime(new Date());
        // 设置状态为预约中
        appointment.setStatus(0);
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment get(Long id) {
        return appointmentMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Appointment appointment) {
        // 如果不是在预约状态,则抛出异常
        if ( !Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus()) ){
            throw new BusinessException("非法操作");
        }
        appointmentMapper.updateByPrimaryKey(appointment);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            Appointment appointment = appointmentMapper.selectByPrimaryKey(dictId);
            if (Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())){
                appointmentMapper.deleteByPrimaryKey(dictId);
            } else {
                throw new BusinessException("非法操作");
            }
        }
    }

    @Override
    public List<Appointment> list() {
        return appointmentMapper.selectAll();
    }

    @Override
    public void arriveShop(Long id) {
        // 获取预约数据
        Appointment oldAppointment = appointmentMapper.selectByPrimaryKey(id);
        // 如果不是在预约状态,则抛出异常
        if ( !Appointment.STATUS_APPOINTMENT.equals(oldAppointment.getStatus()) ){
            throw new BusinessException("非法操作");
        }
        // 设置状态为到店状态
        appointmentMapper.arriveShop(id,1,new Date());
    }

    @Override
    public void cancel(Long id) {
        // 获取预约数据
        Appointment oldAppointment = appointmentMapper.selectByPrimaryKey(id);
        // 如果不是在预约状态,则抛出异常
        if ( !Appointment.STATUS_APPOINTMENT.equals(oldAppointment.getStatus()) ){
            throw new BusinessException("非法操作");
        }
        // 设置状态为取消状态
        appointmentMapper.changeState(id,Appointment.STATUS_CANCEL);
    }

    @Override
    public Long generateStatement(Long id) {
        // 查询出预约单数据
        Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
        // 根据预约id查询结算单数据
        Statement statement  = statementMapper.queryByAppId(id);
        // 如果结算单数据为空,则新增一个结算单数
        if (statement == null){
            // 将数据封装进去
            statement = new Statement();
            statement.setCreateTime(new Date());
            statement.setCustomerName(appointment.getCustomerName());
            statement.setCustomerPhone(appointment.getCustomerPhone());
            statement.setActualArrivalTime(appointment.getActualArrivalTime());
            statement.setCarSeries(appointment.getCarSeries());
            statement.setServiceType(appointment.getServiceType());
            statement.setAppointmentId(appointment.getId());
            statement.setInfo(appointment.getInfo());
            // 将数据存入数据库中
            statementMapper.insert(statement);
        }
        // 修改预约单状态为已结算
        appointmentMapper.changeState(id,Appointment.STATUS_SETTLE);
        // 返回结算单id
        return statement.getId();
    }

}
