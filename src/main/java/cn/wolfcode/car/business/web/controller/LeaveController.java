package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.Service.ILeaveService;
import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.business.vo.LeaveVo;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("/business/leave")
public class LeaveController {
    //模板前缀
    private static final String prefix = "business/leave/";

    @Autowired
    private ILeaveService leaveService;
    @Autowired
    private IUserService userService;

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
    public TablePageInfo<Leave> query(LeaveQuery qo){
        return leaveService.query(qo);
    }


    // 待办页
    @RequestMapping("/todoPage")
    public String todoPage(){
        return prefix + "todoPage";
    }
    // 待办页数据
    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<Leave> todoQuery(LeaveQuery qo){
        // 根据审查人id查询
        qo.setAuditId(ShiroUtils.getUserId());
        // 审核状态
        qo.setStatus("审核中");
        return leaveService.query(qo);
    }

    // 审批页
    @RequestMapping("/auditPage")
    public String auditPage(Model model,Long id){
        model.addAttribute("id",id);
        return prefix + "auditPage";
    }


    // 已办页
    @RequestMapping("/donePage")
    public String donePage(){
        return prefix + "donePage";
    }
    // 已办页数据
    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<Leave> doneQuery(LeaveQuery qo){
        qo.setName(ShiroUtils.getUser().getUserName());
        return leaveService.query(qo);
    }

    // 请假申请页面
    @RequestMapping("/startAuditPage")
    public String startAuditPage(Model model,Long id){
        // 加入店长信息
        model.addAttribute("directors",userService.queryByRoleKey("shopOwner"));
        // 加入财务专员信息
        model.addAttribute("finances",userService.queryByRoleKey("financial"));
        return prefix + "audit";
    }
    // 请假申请确定
    @RequestMapping("/startAudit")
    @ResponseBody
    public AjaxResult startAudit(LeaveVo vo){
        leaveService.startAudit(vo);
        return AjaxResult.success();
    }

    // 审批页
    @RequestMapping("/initiatePage")
    public String initiatePage(Model model,Long id){
        model.addAttribute("id",id);
        return prefix + "auditPage";
    }
    // 审批确定
    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id,Integer auditStatus,String info){
        leaveService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }
}
