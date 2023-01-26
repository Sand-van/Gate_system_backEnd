package com.chao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@ApiModel(value = "用户", description = "用户实体类")
@TableName("user")
@Data
public class User implements Serializable
{
    /**
     * 主键
     */
    @ApiModelProperty(name = "主键")
    @TableId
    private Long id;
    /**
     * 用户姓名
     */
    @ApiModelProperty(name = "用户姓名")
    private String name;
    /**
     * 用户学号/工号
     */
    @ApiModelProperty(name = "用户学号/工号")
    private String number;
    /**
     * 密码
     */
    @ApiModelProperty(name = "密码")
    private String password;
    /**
     * 手机号
     */
    @ApiModelProperty(name = "手机号")
    private String phoneNumber;
    /**
     * 用户类型
     */
    @ApiModelProperty(name = "用户类型")
    private Integer type;
    /**
     * 创建人id
     */
    @ApiModelProperty(name = "创建人id")
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    /**
     * 创建时间
     */
    @ApiModelProperty(name = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新人id
     */
    @ApiModelProperty(name = "更新人id")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(name = "更新时间")
    private Date updateTime;
}