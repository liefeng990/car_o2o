package cn.wolfcode.car.business.Service;

import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface IStatementItemService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<StatementItem> query(StatementItemQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    StatementItem get(Long id);


    /**
     * 查询全部
     * @return
     */
    List<StatementItem> list();

    /**
     * 保存
     * @param list
     */
    void saveItems(List<StatementItem> list);

    /**
     * 支付
     * @param statementId
     */
    void payStatement(Long statementId);
}
