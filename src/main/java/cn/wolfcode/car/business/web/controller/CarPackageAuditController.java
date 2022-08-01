package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.Service.ICarPackageAuditService;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("/business/carPackageAudit")
public class CarPackageAuditController {
    //模板前缀
    private static final String prefix = "business/carPackageAudit/";

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    //页面------------------------------------------------------------
    //列表
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }



    //数据-----------------------------------------------------------     
    //列表
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo){
        qo.setCreatorName(ShiroUtils.getUser().getUserName());
        return carPackageAuditService.query(qo);
    }

    // 进程图
    @RequestMapping("/processImg")
    @ResponseBody
    public void processImg(Long id, HttpServletResponse response) {
        InputStream inputStream = carPackageAuditService.processImg(id);
        try {
            IOUtils.copy(inputStream,response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 撤销
    @RequestMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply(Long id){
        carPackageAuditService.cancelApply(id);
        return AjaxResult.success();
    }

    // 待办页
    @RequestMapping("/todoPage")
    public String todoPage(){
        return prefix + "todoPage";
    }
    // 待办页数据
    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo){
        // 根据审查人id查询
        qo.setAuditorId(ShiroUtils.getUserId());
        // 必须是为审核中
        qo.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        return carPackageAuditService.query(qo);
    }

    // 审批页
    @RequestMapping("/auditPage")
    public String auditPage(Model model,Long id){
        model.addAttribute("id",id);
        return prefix + "auditPage";
    }

    // 审批确定
    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id,Integer auditStatus,String info){
        carPackageAuditService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }

    // 待办页
    @RequestMapping("/donePage")
    public String donePage(){
        return prefix + "donePage";
    }
    // 待办页数据
    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> doneQuery(CarPackageAuditQuery qo){
        qo.setName(ShiroUtils.getUser().getUserName());
        return carPackageAuditService.query(qo);
    }
}
