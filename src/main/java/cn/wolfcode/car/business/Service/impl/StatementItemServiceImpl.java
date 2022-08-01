package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.IStatementItemService;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.StatementItemMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class StatementItemServiceImpl implements IStatementItemService {

    @Autowired
    private StatementItemMapper statementItemMapper;

    @Autowired
    private StatementMapper statementMapper;

    @Override
    public TablePageInfo<StatementItem> query(StatementItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<StatementItem>(statementItemMapper.selectForList(qo));
    }


    @Override
    public StatementItem get(Long id) {
        return statementItemMapper.selectByPrimaryKey(id);
    }


    @Override
    public List<StatementItem> list() {
        return statementItemMapper.selectAll();
    }

    @Override
    public void saveItems(List<StatementItem> list) {
        // 获取list中的最后一个参数 获取传过来的优惠金额,客单id
        StatementItem statementItem = list.remove(list.size() - 1);
        // 优惠金额
        BigDecimal discountAmount = statementItem.getItemPrice();
        // 客单id
        Long statementId = statementItem.getStatementId();
        // 删除中间表中的旧数据
        statementItemMapper.deleteByStatementId(statementId);
        // 总金额,总数量
        BigDecimal totalAmount = new BigDecimal(0.00);
        BigDecimal totalCount = new BigDecimal(0.00);
        // 遍历list 将数据存入中间表中
        for (StatementItem item : list) {
            // 计算总金额
            totalAmount = totalAmount.add(item.getItemPrice().multiply(item.getItemQuantity()));
            totalCount = totalCount.add(item.getItemQuantity());
            statementItemMapper.insert(item);
        }
        // 修改客单表中的总金额,总数量,优惠金额
        statementMapper.updateByAmount(statementId,totalAmount,totalCount,discountAmount);
    }

    @Override
    public void payStatement(Long statementId) {
        // 根据客单id获取数据
        Statement statement = statementMapper.selectByPrimaryKey(statementId);
        // 如果状态不为消费中,则抛出异常
        if (!Statement.STATUS_CONSUME.equals(statement.getStatus())){
            throw new BusinessException("非法操作");
        }
        // 修改支付状态,支付人id,支付时间
        statementMapper.updateByPay(statementId,1, ShiroUtils.getUser().getId());
    }

}
