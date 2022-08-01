package cn.wolfcode.car.business.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceItemVo {
    private Long    id;         // 服务单项id
    private Long    bpmnInfoId; // 部署信息表id
    private Long    director;   // 店长
    private Long    finance;    // 财务
    private String  info;       // 备注信息
}
