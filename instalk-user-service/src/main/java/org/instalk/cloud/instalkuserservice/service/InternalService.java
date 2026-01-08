package org.instalk.cloud.instalkuserservice.service;

import org.instalk.cloud.common.model.po.User;

public interface InternalService {
    User getUserByEmail(String email);

    User getUserByUsername(String username);
}
