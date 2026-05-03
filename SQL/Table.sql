-- 创建数据库
create database IF NOT EXISTS ins_talk
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

use ins_talk;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    signature VARCHAR(255) DEFAULT '这家伙很懒，什么都留下...',
    avatar VARCHAR(255) DEFAULT 'https://luf-23.oss-cn-wuhan-lr.aliyuncs.com/ins_talk/defaultUserAvatar.png',
    role ENUM('USER', 'ADMIN','ROBOT') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- 好友关系表
CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id1 BIGINT NOT NULL, -- 小id
    user_id2 BIGINT NOT NULL, -- 大id
    requester_id BIGINT, -- 发起请求的用户id
    status ENUM('PENDING', 'ACCEPTED', 'BLOCKED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id1) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (user_id1, user_id2)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- 群组表
CREATE TABLE IF NOT EXISTS chat_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    avatar VARCHAR(255) DEFAULT 'https://luf-23.oss-cn-wuhan-lr.aliyuncs.com/ins_talk/defaultGroupAvatar.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES user(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- 群组成员表
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'ADMIN', 'MEMBER') DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES chat_group(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_member (group_id, user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- 聊天消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT,
    group_id BIGINT,
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'FILE') DEFAULT 'TEXT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES chat_group(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT   =100001;

-- 消息状态表(已读/未读)
CREATE TABLE IF NOT EXISTS message_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES message(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_message_status (message_id, user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- 用户AI助手配置表
CREATE TABLE IF NOT EXISTS user_ai_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '真实用户ID',
    robot_id BIGINT NOT NULL COMMENT '专属机器人用户ID',

    system_prompt TEXT COMMENT '系统提示词',
    model ENUM('deepseek-v3','deepseek-r1','qwq-plus','qwen-max-2025-01-25') DEFAULT 'deepseek-v3' COMMENT 'AI模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数(0-2)，越高越随机',
    max_tokens INT DEFAULT 2000 COMMENT '单次回复最大token数',
    top_p DECIMAL(3,2) DEFAULT 1.00 COMMENT 'Top P采样参数',
    presence_penalty DECIMAL(3,2) DEFAULT 0.00 COMMENT '存在惩罚(-2到2)',
    seed INT DEFAULT 1234 COMMENT '随机数种子',

    daily_message_limit INT DEFAULT 100 COMMENT '每日消息限制',
    daily_message_count INT DEFAULT 0 COMMENT '今日已使用消息数',
    last_reset_date DATE COMMENT '最后重置日期',

    total_messages INT DEFAULT 0 COMMENT '总消息数',
    total_tokens_used BIGINT DEFAULT 0 COMMENT '总token使用量',
    last_used_at TIMESTAMP NULL DEFAULT NULL COMMENT '最后使用时间',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (robot_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_robot_id (robot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- AI会话摘要表
CREATE TABLE IF NOT EXISTS ai_chat_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,
    summary TEXT NOT NULL,
    last_message_id BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_robot (user_id, robot_id),
    INDEX idx_updated_at (updated_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (robot_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;

-- AI记忆表(用于RAG检索)
CREATE TABLE IF NOT EXISTS ai_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    robot_id BIGINT NOT NULL,
    type ENUM('FACT','PREF','TASK','DECISION') DEFAULT 'FACT',
    content TEXT NOT NULL,
    embedding_json JSON NULL,
    embedding VECTOR(1536) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_robot (user_id, robot_id),
    VECTOR INDEX idx_embedding (embedding),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (robot_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=100001;