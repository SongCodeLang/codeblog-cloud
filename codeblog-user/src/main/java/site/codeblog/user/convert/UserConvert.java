package site.codeblog.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import site.codeblog.user.entity.UserPO;
import site.codeblog.user.vo.UserCreateReqVO;
import site.codeblog.user.vo.UserRespVO;
import site.codeblog.user.vo.UserUpdateReqVO;

/**
 * 用户对象转换
 *
 * @author codeblog
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * PO → RespVO
     */
    UserRespVO convert(UserPO po);

    /**
     * 创建请求 → PO（密码、删除标记、时间由服务层填充）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UserPO convert(UserCreateReqVO reqVO);

    /**
     * 更新请求 → PO
     */
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UserPO convert(UserUpdateReqVO reqVO);

}
