package org.instalk.cloud.instalkidentityservice.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkidentityservice.config.OssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
public class OssService {

    @Autowired
    private OssConfig ossConfig;

    public Result<AssumeRoleResponse.Credentials> getCredentials() {
        Long userId = ThreadLocalUtil.getId();
        try {
            String policy = StreamUtils.copyToString(
                    new ClassPathResource(ossConfig.getPolicyFile()).getInputStream(),
                    StandardCharsets.UTF_8
            );

            IClientProfile profile = DefaultProfile.getProfile(ossConfig.getRegion(), ossConfig.getAccessKeyId(), ossConfig.getAccessKeySecret());
            DefaultAcsClient client = new DefaultAcsClient(profile);

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(ossConfig.getRoleArn());
            request.setRoleSessionName("user-" + userId);
            request.setPolicy(policy);
            request.setDurationSeconds(ossConfig.getExpireTime());

            AssumeRoleResponse response = client.getAcsResponse(request);
            return Result.success(response.getCredentials());
        } catch (Exception e) {
            return Result.error("生成临时凭证失败：" + e.getMessage());
        }
    }


    public Result<String> getBucket() {
        return Result.success(ossConfig.getBucket());
    }


    public Result<String> getRegion() {
        return Result.success(ossConfig.getRegion());
    }


    public Result<String> getEndPoint() {
        return Result.success(ossConfig.getEndpoint());
    }

}
