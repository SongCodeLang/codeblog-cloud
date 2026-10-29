package site.codeblog.system.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import site.codeblog.system.entity.RolePO;
import site.codeblog.system.vo.RoleCreateReqVO;
import site.codeblog.system.vo.RoleRespVO;
import site.codeblog.system.vo.RoleUpdateReqVO;

/**
 * 角色对象转换
 *
 * @author codeblog
 */
@Mapper(componentModel = "spring")
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);

    /**
     * PO → RespVO
     */
    RoleRespVO convert(RolePO po);

    /**
     * 创建请求 → PO（删除标记、审计字段由服务层填充）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    RolePO convert(RoleCreateReqVO reqVO);

    /**
     * 更新请求 → PO
     */
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    RolePO convert(RoleUpdateReqVO reqVO);

}
