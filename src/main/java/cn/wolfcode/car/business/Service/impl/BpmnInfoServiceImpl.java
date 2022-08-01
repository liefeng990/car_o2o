package cn.wolfcode.car.business.Service.impl;

import cn.wolfcode.car.business.Service.IBpmnInfoService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.common.util.file.FileUploadUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class BpmnInfoServiceImpl implements IBpmnInfoService {

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Autowired
    private RepositoryService repositoryService;
    


    @Override
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<BpmnInfo>(bpmnInfoMapper.selectForList(qo));
    }


    @Override
    public void save(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public BpmnInfo get(Long id) {
        return bpmnInfoMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.updateByPrimaryKey(bpmnInfo);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(dictId);
                bpmnInfoMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<BpmnInfo> list() {
        return bpmnInfoMapper.selectAll();
    }

    @Override
    @Transactional
    public String uploadDeploymentFile(MultipartFile file) {
        // 获取文件扩展名
        String extension = FileUploadUtils.getExtension(file);
        // 判断传入文件的扩展名不为bpmn则返回错误信息
        if (!"bpmn".equals(extension)){
            throw new BusinessException("传入文件必须为bpmn文件");
        }
        // 保存文件
        String path = null;
        try {
            path = FileUploadUtils.upload(SystemConfig.getProfile(), file);
        } catch (IOException e) {
            throw new BusinessException("上传失败");
        }
        return path;
    }

    // 流程部署
    @Override
    @Transactional
    public void deploy(String path, String info, String bpmnType) throws IOException {
        // 根据地址获取存放的部署文件
        File file = new File(SystemConfig.getProfile(), path);
        // 进行流程部署
        FileInputStream fileInputStream = new FileInputStream(file);
        Deployment deployment = repositoryService.
                createDeployment().
                addInputStream(path, fileInputStream).
                deploy();
        // 释放资源
        fileInputStream.close();
        // 获取流程定义 信息 (获取 id 和 key)
        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                deploymentId(deployment.getId()).
                singleResult();
        // 将部署信息存入部署信息表
        BpmnInfo bpmnInfo = new BpmnInfo();
        bpmnInfo.setDeploymentId(deployment.getId());           // 流程部署id
        bpmnInfo.setBpmnName(processDefinition.getName());      // 流程(图)名称
        bpmnInfo.setBpmnType(bpmnType);                         // 流程图类型
        bpmnInfo.setInfo(info);                                 // 备注信息
        bpmnInfo.setDeployTime(deployment.getDeploymentTime()); // 部署时间
        bpmnInfo.setActProcessId(processDefinition.getId());    // 流程定义id
        bpmnInfo.setActProcessKey(processDefinition.getKey());  // 流程定义key
        bpmnInfoMapper.insert(bpmnInfo);
    }

    // 删除
    @Override
    public void deleteProcess(Long id) {
        // 获取部署信息 ( 流程定义id )
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        // 删除部署信息表的数据
        bpmnInfoMapper.deleteByPrimaryKey(id);
        // 获取流程定义信息(获取部署文件的路径)
        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                deploymentId(bpmnInfo.getDeploymentId()).
                singleResult();
        // 删除activiti的表数据
        repositoryService.deleteDeployment(bpmnInfo.getDeploymentId());
        // 删除部署时候的流程文件
        File file = new File(SystemConfig.getProfile(), processDefinition.getResourceName());
        if (file.exists()){      // 如果该文件存在
            file.delete();
        }
    }

    @Override
    @Transactional
    public InputStream readResource(String deploymentId, String type) {
        // 先获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                deploymentId(deploymentId).
                singleResult();

        InputStream inputStream = null;
        // 判断类型来选择响应对象
        if ("xml".equals(type)){   // 流程文件
            inputStream = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
        } else {    // xml文件
            DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();  // 标准流程图发生器
            inputStream = diagramGenerator.generateDiagram(repositoryService.getBpmnModel(processDefinition.getId()),// bpmnModel
                    Collections.emptyList(),// 高亮节点集合
                    Collections.emptyList(),// 高亮连线集合
                    "方正楷体", "方正楷体", "方正楷体"  // 字体
            );
        }
        return inputStream;
    }

    @Override
    public List<BpmnInfo> queryByType(String type) {
        return bpmnInfoMapper.queryByType(type);
    }

}
