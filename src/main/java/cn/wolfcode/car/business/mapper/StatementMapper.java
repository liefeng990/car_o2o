package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.StatementQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StatementMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Statement record);

    Statement selectByPrimaryKey(Long id);

    List<Statement> selectAll();

    int updateByPrimaryKey(Statement record);

    List<Statement> selectForList(StatementQuery qo);

    void deleteCentreData(Long statementId);

    void updateByAmount(@Param("statementId") Long statementId, @Param("totalAmount") BigDecimal totalAmount, @Param("totalCount") BigDecimal totalCount, @Param("discountAmount") BigDecimal discountAmount);

    void updateByPay(@Param("statementId") Long statementId, @Param("status") int status, @Param("payeeId") Long payeeId);

    Statement queryByAppId(Long appId);
}