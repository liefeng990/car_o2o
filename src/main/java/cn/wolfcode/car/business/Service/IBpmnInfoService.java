package cn.wolfcode.car.business.Service;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 岗位服务接口
 */
public interface IBpmnInfoService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    BpmnInfo get(Long id);


    /**
     * 保存
     * @param bpmnInfo
     */
    void save(BpmnInfo bpmnInfo);

  
    /**
     * 更新
     * @param bpmnInfo
     */
    void update(BpmnInfo bpmnInfo);

    /**
     *  批量删除
     * @param ids
     */
    void deleteBatch(String ids);

    /**
     * 查询全部
     * @return
     */
    List<BpmnInfo> list();

    /**
     * 部署流程的文件上传
     * @param file      上传的文件
     * @return          文件所在的路径
     */
    String uploadDeploymentFile(MultipartFile file);

    /**
     *  流程部署
     * @param path      部署的文件的地址
     * @param info      流程部署的备注信息
     * @param bpmnType  流程类型
     */
    void deploy(String path, String info, String bpmnType) throws IOException;

    /**
     * 删除流程
     * @param id
     */
    void deleteProcess(Long id);

    /**
     * 流程文件查看(xml和流程图)
     * @param deploymentId  流程定义id
     * @param type          文件扩展名
     */
    InputStream readResource(String deploymentId, String type);

    List<BpmnInfo> queryByType(String type);
}
