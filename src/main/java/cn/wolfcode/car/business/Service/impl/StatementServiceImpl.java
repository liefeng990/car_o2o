package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.IStatementService;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementQuery;
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
public class StatementServiceImpl implements IStatementService {

    @Autowired
    private StatementMapper statementMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public TablePageInfo<Statement> query(StatementQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Statement>(statementMapper.selectForList(qo));
    }


    @Override
    public void save(Statement statement) {
        // 设置创建时间
        statement.setCreateTime(new Date());
        statementMapper.insert(statement);
    }

    @Override
    public Statement get(Long id) {
        return statementMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Statement statement) {
        Statement oldStatement = statementMapper.selectByPrimaryKey(statement.getId());
        if (!Statement.STATUS_CONSUME.equals(oldStatement.getStatus())){
            throw new BusinessException("非法操作");
        }
        statementMapper.updateByPrimaryKey(statement);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            Statement statement = statementMapper.selectByPrimaryKey(dictId);
            if (!Statement.STATUS_CONSUME.equals(statement.getStatus())){
                throw new BusinessException("非法操作");
            }
            // 删除中间表数据
            statementMapper.deleteCentreData(dictId);
            // 删除结算表数据
            statementMapper.deleteByPrimaryKey(dictId);
            // 如果是预约的,将预约状态设置回到店
            if (statement.getAppointmentId() != null){
                appointmentMapper.changeState(statement.getAppointmentId(),1);
            }
        }
    }

    @Override
    public List<Statement> list() {
        return statementMapper.selectAll();
    }

}
