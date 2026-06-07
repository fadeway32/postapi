package com.fadeway32.postadmin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fadeway32.postadmin.entity.ApiDefinition;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ApiDefinitionMapper extends BaseMapper<ApiDefinition> {
    @Select("SELECT d.* FROM pa_api_definition d "
            + "JOIN (SELECT api_code, MAX(CAST(SUBSTRING(version, 2) AS INT)) AS max_version "
            + "FROM pa_api_definition WHERE tenant_id = #{tenantId} GROUP BY api_code) latest "
            + "ON d.api_code = latest.api_code AND CAST(SUBSTRING(d.version, 2) AS INT) = latest.max_version "
            + "WHERE d.tenant_id = #{tenantId} ORDER BY d.updated_at DESC")
    List<ApiDefinition> selectLatestByTenant(@Param("tenantId") Long tenantId);

    @Update("UPDATE pa_api_definition SET call_count = call_count + 1, "
            + "success_count = success_count + #{successDelta}, "
            + "failure_count = failure_count + #{failureDelta}, "
            + "last_call_time = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP "
            + "WHERE id = #{id}")
    int increaseCallStats(@Param("id") Long id,
                          @Param("successDelta") int successDelta,
                          @Param("failureDelta") int failureDelta);
}
