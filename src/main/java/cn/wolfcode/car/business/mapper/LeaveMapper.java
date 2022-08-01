package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.LeaveQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LeaveMapper {
    int insert(Leave record);

    Leave selectOne(Long id);

    List<Leave> selectAll();

    List<Leave> selectForList(LeaveQuery qo);

    void updateInstanceId(@Param("id") Long id, @Param("instanceId") String instanceId);

    void update(Leave leave);
}