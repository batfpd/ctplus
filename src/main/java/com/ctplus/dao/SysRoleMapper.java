package com.ctplus.dao;

import com.ctplus.entity.SysRole;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> getRolesByUserId(@Param("userId") String userId);

    List<String> getRoleIdsByUserId(@Param("userId") String userId);

    Boolean checkRidsContainRval(@Param("rids")List<String> rids,@Param("rval")String rval);

    Boolean checkUidContainRval(@Param("uid")String uid,@Param("rval")String rval);

}
