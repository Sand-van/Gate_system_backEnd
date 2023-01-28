package com.chao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chao.common.BaseContext;
import com.chao.common.CommonEnum;
import com.chao.common.ReturnMessage;
import com.chao.dto.AdminAuthorityDto;
import com.chao.entity.AdminAuthority;
import com.chao.entity.User;
import com.chao.service.AdminAuthorityService;
import com.chao.service.DeviceService;
import com.chao.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "管理员权限管理相关接口")
public class AdminAuthorityController
{
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AdminAuthorityService adminAuthorityService;

    @PostMapping("/add")
    @ApiOperation("添加管理员权限")
    @ApiImplicitParam(name = "adminAuthorityToAdd", value = "要添加的管理员权限信息", required = true)
    public ReturnMessage<String> addAdminAuthority(@RequestBody AdminAuthority adminAuthorityToAdd)
    {
        User nowLoginUser = userService.getById(BaseContext.getCurrentID());
        User adminToAdd = userService.getById(adminAuthorityToAdd.getUserId());

        if (Objects.equals(nowLoginUser.getType(), CommonEnum.USER_TYPE_SUPER_ADMIN) && Objects.equals(adminToAdd.getType(), CommonEnum.USER_TYPE_ADMIN))
        {
            //查找是否有重复信息
            if (adminAuthorityService.getAuthorityIdByAdminIdAndDeviceId(adminAuthorityToAdd.getUserId(), adminAuthorityToAdd.getDeviceId()) != null)
                return ReturnMessage.commonError("重复的权限信息");

            adminAuthorityToAdd.setId(null);

            adminAuthorityService.save(adminAuthorityToAdd);
            return ReturnMessage.success("添加成功");
        }

        return ReturnMessage.commonError("没有权限");
    }

    @DeleteMapping("/deleteById")
    @ApiOperation("通过id来删除管理员权限")
    @ApiImplicitParam(name = "adminAuthorityId", value = "要删除的管理员权限id", required = true)
    public ReturnMessage<String> deleteAdminAuthority(Long adminAuthorityId)
    {
        User nowLoginUser = userService.getById(BaseContext.getCurrentID());
        if (Objects.equals(nowLoginUser.getType(), CommonEnum.USER_TYPE_SUPER_ADMIN))
        {
            adminAuthorityService.removeById(adminAuthorityId);
            return ReturnMessage.success("删除成功");
        }
        return ReturnMessage.commonError("没有权限");
    }

    @GetMapping("/page/adminId")
    @ApiOperation("通过管理员id请求管理员权限信息分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "要显示第几页", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "一页显示几条信息", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "adminId", value = "管理员id", required = true),
            @ApiImplicitParam(name = "deviceName", value = "要搜索的设备名字", dataTypeClass = String.class),
    })
    public ReturnMessage<Page<AdminAuthorityDto>> pageByAdminId(int page, int pageSize, Long adminId, String deviceName)
    {
        Page<AdminAuthority> adminAuthorityPageInfo = new Page<>(page, pageSize);
        Page<AdminAuthorityDto> adminAuthorityDtoPageInfo = new Page<>();

        User nowLoginUser = userService.getById(BaseContext.getCurrentID());
        //普通用户没有权限查看分页
        if (Objects.equals(nowLoginUser.getType(), CommonEnum.USER_TYPE_USER))
            return ReturnMessage.forbiddenError("没有权限");

        LambdaQueryWrapper<AdminAuthority> queryWrapper = new LambdaQueryWrapper<>();
        if (deviceName != null)
        {
            List<Long> likeNameDeviceIdList = deviceService.getIdByLikeName(deviceName);
            if (likeNameDeviceIdList.size() == 0)
                return ReturnMessage.success(adminAuthorityDtoPageInfo);
            queryWrapper.in(AdminAuthority::getDeviceId, likeNameDeviceIdList);
        }

        queryWrapper.eq(AdminAuthority::getUserId, adminId);

        adminAuthorityService.page(adminAuthorityPageInfo, queryWrapper);

        BeanUtils.copyProperties(adminAuthorityPageInfo, adminAuthorityDtoPageInfo, "records");
        //流处理，将adminAuthority转化为adminAuthorityDto
        List<AdminAuthority> records = adminAuthorityPageInfo.getRecords();
        List<AdminAuthorityDto> dtoRecords = records.stream().map((item) -> {
            AdminAuthorityDto adminAuthorityDto = new AdminAuthorityDto();
            BeanUtils.copyProperties(item, adminAuthorityDto);

            Long id = item.getDeviceId();
            adminAuthorityDto.setDeviceName(deviceService.getById(id).getName());
            adminAuthorityDto.setAdminName(userService.getById(adminId).getName());

            return adminAuthorityDto;
        }).collect(Collectors.toList());

        adminAuthorityDtoPageInfo.setRecords(dtoRecords);

        return ReturnMessage.success(adminAuthorityDtoPageInfo);
    }


    @GetMapping("/page/deviceId")
    @ApiOperation("通过设备id请求管理员权限信息分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "要显示第几页", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "一页显示几条信息", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "deviceId", value = "设备id", required = true),
            @ApiImplicitParam(name = "userName", value = "要搜索的管理员名字", dataTypeClass = String.class),
            @ApiImplicitParam(name = "userAccount", value = "要搜索的管理员账号", dataTypeClass = String.class),
    })
    public ReturnMessage<Page<AdminAuthorityDto>> pageByDeviceId(int page, int pageSize, Long deviceId, String userName, String userAccount)
    {
        Page<AdminAuthority> adminAuthorityPageInfo = new Page<>(page, pageSize);
        Page<AdminAuthorityDto> adminAuthorityDtoPageInfo = new Page<>();

        User nowLoginUser = userService.getById(BaseContext.getCurrentID());
        //普通用户没有权限查看分页
        if (Objects.equals(nowLoginUser.getType(), CommonEnum.USER_TYPE_USER))
            return ReturnMessage.forbiddenError("没有权限");

        LambdaQueryWrapper<AdminAuthority> queryWrapper = new LambdaQueryWrapper<>();
        if ((userName != null) || (userAccount != null))
        {
            List<Long> likeNameDeviceIdList = userService.getIdByLikeNameAndAccount(userName, userAccount);
            if (likeNameDeviceIdList.size() == 0)
                return ReturnMessage.success(adminAuthorityDtoPageInfo);
            queryWrapper.in(AdminAuthority::getUserId, likeNameDeviceIdList);
        }

        queryWrapper.eq(AdminAuthority::getDeviceId, deviceId);

        adminAuthorityService.page(adminAuthorityPageInfo, queryWrapper);

        BeanUtils.copyProperties(adminAuthorityPageInfo, adminAuthorityDtoPageInfo, "records");
        //流处理，将adminAuthority转化为adminAuthorityDto
        List<AdminAuthority> records = adminAuthorityPageInfo.getRecords();
        List<AdminAuthorityDto> dtoRecords = records.stream().map((item) -> {
            AdminAuthorityDto adminAuthorityDto = new AdminAuthorityDto();
            BeanUtils.copyProperties(item, adminAuthorityDto);

            Long id = item.getUserId();
            adminAuthorityDto.setAdminName(userService.getById(id).getName());
            adminAuthorityDto.setDeviceName(deviceService.getById(deviceId).getName());

            return adminAuthorityDto;
        }).collect(Collectors.toList());

        adminAuthorityDtoPageInfo.setRecords(dtoRecords);

        return ReturnMessage.success(adminAuthorityDtoPageInfo);
    }
}
