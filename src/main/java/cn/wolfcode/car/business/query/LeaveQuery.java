package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
public class LeaveQuery extends QueryObject {
    // 开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    // 结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    // 状态
    private String status;
    // 审核人id
    private Long auditId;
    // 备注里面的名字 (已办使用)
    private String name;

    public Date getEndTime() {
        if (endTime != null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime);
            // 加一天
            calendar.add(Calendar.DAY_OF_MONTH,1);
            // 减一秒
            calendar.add(Calendar.SECOND,-1);
            return calendar.getTime();
        }
        return null;
    }
}
