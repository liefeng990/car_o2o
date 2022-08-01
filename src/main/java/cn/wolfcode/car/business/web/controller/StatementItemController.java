package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.Service.IStatementItemService;
import cn.wolfcode.car.business.Service.IStatementService;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("/business/statementItem")
public class StatementItemController {
    //模板前缀
    private static final String prefix = "business/statementItem/";

    @Autowired
    private IStatementService statementService;

    @Autowired
    private IStatementItemService statementItemService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:statementItem:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }



    //数据-----------------------------------------------------------     
    //列表
    @RequiresPermissions("business:statementItem:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<StatementItem> query(StatementItemQuery qo){
        return statementItemService.query(qo);
    }



    @RequestMapping("/itemDetail")
    public String itemDetail(Long statementId, Model model){
        Statement statement = statementService.get(statementId);
        model.addAttribute("statement",statement);
        // 判断结算状态是 已支付则进入明细查看  消费中则进入明细结算
        if (Statement.STATUS_CONSUME.equals(statement.getStatus())){
            return prefix + "editDetail";
        } else if (Statement.STATUS_PAID.equals(statement.getStatus())){
            return prefix + "showDetail";
        }
        return null;
    }

    @RequestMapping("/saveItems")
    @ResponseBody
    public AjaxResult saveItems(@RequestBody List<StatementItem> list){
        statementItemService.saveItems(list);
        return AjaxResult.success();
    }

    @RequestMapping("/payStatement")
    @ResponseBody
    public AjaxResult payStatement(Long statementId){
        statementItemService.payStatement(statementId);
        return AjaxResult.success();
    }


}
