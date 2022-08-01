package cn.wolfcode.car.business.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class LeaveVo {
    private String  name;       // 请假人名称
    private String  reason;     // 请假原因
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    // 结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    private Long    director;   // 店长
    private Long    finance;    // 财务
}
