package org.instalk.cloud.instalkauthservice.service;

import org.instalk.cloud.common.model.dto.LoginDTO;
import org.instalk.cloud.common.model.vo.LoginVO;
import org.instalk.cloud.common.model.vo.Result;

public interface FrontService {
    Result<LoginVO> login(LoginDTO loginDTO);
}
