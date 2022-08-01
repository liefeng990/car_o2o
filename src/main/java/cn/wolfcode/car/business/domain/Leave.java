package cn.wolfcode.car.business.domain;

import cn.wolfcode.car.base.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Getter
@Setter
public class Leave {
    /** */
    private Long id;

    /**
     * 请假人名称
     * */
    private String name;

    /**
     * 请假原因
     * */
    private String reason;

    /**
     * 开始时间
     * */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;

    /**
     * 结束时间
     * */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;

    /**
     * 审核人id
     * */
    private Long auditId;
    //当前审核人对象
    private User auditor;
    /**
     * 状态
     * */
    private String status;

    /**
     * 备注信息
     * */
    private String info;

    /**
     * 流程实例id
     */
    private String instanceId;
}