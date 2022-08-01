package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServiceItemMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ServiceItem record);

    ServiceItem selectByPrimaryKey(Long id);

    List<ServiceItem> selectAll();

    int updateByPrimaryKey(ServiceItem record);

    List<ServiceItem> selectForList(ServiceItemQuery qo);

    void changeSaleStatus(@Param("saleStatus") Integer saleStatus, @Param("id") Long id);

    void updateAuditStatus(@Param("id") Long id, @Param("status") Integer status);
}