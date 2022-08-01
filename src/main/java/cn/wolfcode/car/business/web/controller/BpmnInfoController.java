package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.Service.IBpmnInfoService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("/business/bpmnInfo")
public class BpmnInfoController {
    //模板前缀
    private static final String prefix = "business/bpmnInfo/";

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    // 流程部署页面
    @RequestMapping("/deployPage")
    public String deployPage(){
        return prefix + "deployPage";
    }

    @RequiresPermissions("business:bpmnInfo:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("bpmnInfo", bpmnInfoService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------     
    //列表
    @RequiresPermissions("business:bpmnInfo:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo){
        return bpmnInfoService.query(qo);
    }



    // 删除流程
    @RequestMapping("/delete")
    @ResponseBody
    public AjaxResult delete(Long id){
        bpmnInfoService.deleteProcess(id);
        return AjaxResult.success();
    }
    // 流程文件上传
    @RequestMapping("/uploadDeploymentFile")
    @ResponseBody
    public AjaxResult uploadDeploymentFile(MultipartFile file){
        String path = bpmnInfoService.uploadDeploymentFile(file);
        if (path == null){
            return AjaxResult.success("上传失败");
        }
        return AjaxResult.success("操作成功",path);
    }
    // 流程部署
    @RequestMapping("/deploy")
    @ResponseBody
    public AjaxResult deploy(String path,String info,String bpmnType) throws IOException {
        bpmnInfoService.deploy(path,info,bpmnType);
        return AjaxResult.success();
    }

    // 流程文件查看  readResource
    @RequestMapping("/readResource")
    @ResponseBody
    public void readResource(String deploymentId, String type, HttpServletResponse response) {
        InputStream inputStream = bpmnInfoService.readResource(deploymentId, type);
        try {
            IOUtils.copy(inputStream,response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
