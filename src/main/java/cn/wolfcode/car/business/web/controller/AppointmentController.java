package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.Service.IAppointmentService;
import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("/business/appointment")
public class AppointmentController {
    //模板前缀
    private static final String prefix = "business/appointment/";

    @Autowired
    private IAppointmentService appointmentService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:appointment:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }


    @RequiresPermissions("business:appointment:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }

    @RequiresPermissions("business:appointment:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("appointment", appointmentService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------     
    //列表
    @RequiresPermissions("business:appointment:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<Appointment> query(AppointmentQuery qo){
        return appointmentService.query(qo);
    }


    //新增
    @RequiresPermissions("business:appointment:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Appointment appointment){
        appointmentService.save(appointment);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:appointment:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(Appointment appointment){
        appointmentService.update(appointment);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:appointment:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        appointmentService.deleteBatch(ids);
        return AjaxResult.success();
    }

    @RequestMapping("/arriveShop")
    @ResponseBody
    public AjaxResult arriveShop(Long id){
        appointmentService.arriveShop(id);
        return AjaxResult.success();
    }
    @RequestMapping("/cancel")
    @ResponseBody
    public AjaxResult cancel(Long id){
        appointmentService.cancel(id);
        return AjaxResult.success();
    }
    @RequestMapping("/generateStatement")
    @ResponseBody
    public AjaxResult generateStatement(Long id){
        Long  statementId = appointmentService.generateStatement(id);
        return AjaxResult.success(statementId);
    }



}
