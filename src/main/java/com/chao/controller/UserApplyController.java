package com.chao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chao.common.BaseContext;
import com.chao.common.CommonEnum;
import com.chao.common.ReturnMessage;
import com.chao.dto.UserApplyDto;
import com.chao.entity.User;
import com.chao.entity.UserApply;
import com.chao.service.DeviceService;
import com.chao.service.UserApplyService;
import com.chao.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/apply")
@Api(tags = "用户申请操作相关")
public class UserApplyController
{
    @Autowired
    private UserService userService;

    @Autowired
    private UserApplyService userApplyService;

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/add")
    @ApiOperation("添加用户申请")
    @ApiImplicitParam(name = "userApplyToAdd", value = "要添加的用户申请", required = true)
    public ReturnMessage<String> addUserApply(@RequestBody UserApply userApplyToAdd)
    {
        userApplyToAdd.setId(null);
        userApplyToAdd.setApplyTime(LocalDateTime.now());

        userApplyService.save(userApplyToAdd);
        return ReturnMessage.success("保存成功");
    }

    @DeleteMapping("/deleteById")
    @ApiOperation("通过id来删除用户申请")
    @ApiImplicitParam(name = "userApplyId", value = "要删除的用户申请id", required = true)
    public ReturnMessage<String> deleteUserApplyById(Long userApplyId)
    {
        userApplyService.removeById(userApplyId);
        return ReturnMessage.success("删除成功");
    }

    @GetMapping("/page")
    @ApiOperation("请求用户申请分页信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "要显示第几页", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "pageSize", value = "一页显示几条信息", dataTypeClass = int.class, required = true),
            @ApiImplicitParam(name = "queryName", value = "要搜索的人名", dataTypeClass = String.class),
            @ApiImplicitParam(name = "queryNumber", value = "要搜索的学号", dataTypeClass = String.class),
            @ApiImplicitParam(name = "queryDevice", value = "要搜索的设备名", dataTypeClass = String.class)
    })
    public ReturnMessage<Page<UserApplyDto>> page(int page, int pageSize, String queryName, String queryNumber, String queryDevice)
    {
        Page<UserApply> userPageInfo = new Page<>(page, pageSize);
        User nowLoginUser = userService.getById(BaseContext.getCurrentID());

        Page<UserApplyDto> userApplyDtoPageInfo = new Page<>();

        //普通用户没有权限查看用户分页
        if (Objects.equals(nowLoginUser.getType(), CommonEnum.USER_TYPE_USER))
            return ReturnMessage.forbiddenError("没有权限");

        LambdaQueryWrapper<UserApply> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(queryName) || StringUtils.isNotEmpty(queryNumber))
        {
            List<Long> userIds = userService.getIdByLikeNameAndNumber(queryName, queryNumber);
            if (userIds.size() == 0)    //防止出现 select * from xxx where(user_id in [null])错误
                return ReturnMessage.success(userApplyDtoPageInfo);
            queryWrapper.in(UserApply::getUserId, userIds);
        }

        if (StringUtils.isNotEmpty(queryDevice))
        {
            List<Long> deviceIds = deviceService.getIdByLikeName(queryDevice);
            if (deviceIds.size() == 0)  //防止出现 in []错误
                return ReturnMessage.success(userApplyDtoPageInfo);
            queryWrapper.in(UserApply::getDeviceId, deviceIds);
        }

        queryWrapper.orderByDesc(UserApply::getApplyTime);
        userApplyService.page(userPageInfo, queryWrapper);
        BeanUtils.copyProperties(userPageInfo, userApplyDtoPageInfo, "records");
        //流处理，将UserApply转化为UserApplyDto
        List<UserApply> records = userPageInfo.getRecords();
        List<UserApplyDto> dtoRecords = records.stream().map((item) ->
        {
            UserApplyDto userApplyDto = new UserApplyDto();
            BeanUtils.copyProperties(item, userApplyDto);

            Long id = item.getUserId();
            userApplyDto.setUserName(userService.getById(id).getName());

            id = item.getDeviceId();
            userApplyDto.setDeviceName(deviceService.getById(id).getName());

            return userApplyDto;
        }).collect(Collectors.toList());

        userApplyDtoPageInfo.setRecords(dtoRecords);
        return ReturnMessage.success(userApplyDtoPageInfo);
    }
}