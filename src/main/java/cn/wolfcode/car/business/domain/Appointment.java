package cn.wolfcode.car.business.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
public class Appointment {
    private static final long serialVersionUID = 1L;
    public static final Integer STATUS_APPOINTMENT = 0;//预约中
    public static final Integer STATUS_ARRIVAL = 1;//已到店
    public static final Integer STATUS_CANCEL = 2;//用户取消
    public static final Integer STATUS_OVERTIME = 3;//超时取消
    public static final Integer STATUS_SETTLE  = 4;//已结算

    private Long id;

    private String customerName;                    //客户姓名

    private String customerPhone;                   //客户联系方式

    // JsonFormat      主要是后台给前台的时间格式转换  可以贴在属性上方,也可以贴在属性的get方法上方   使用的是标准时间,北京时间需要+8时
    // DataTimeFormat  主要是前台到后台的时间格式转换  只能在非json数据上生效
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date appointmentTime;                   //预约时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date actualArrivalTime;                 //实际到店时间

    private String licensePlate;                    //车牌号码

    private String carSeries;                       //汽车类型

    private Long serviceType;                    //客服务类型【维修0/保养1】

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;                        //创建时间

    private String info;                            //备注信息

    private Integer status = STATUS_APPOINTMENT;    //状态【预约中0/已到店1/用户取消2/超时取消3/已结算4】

}