package org.instalk.cloud.gateway.filter;

import org.instalk.cloud.common.util.JwtUtil;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.common.util.TokenUtil;
import org.instalk.cloud.gateway.config.AuthWhiteListProperties;
import org.instalk.cloud.infrastructure.redis.RedisUtilImpl;
import org.instalk.cloud.infrastructure.redis.TokenUtilImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;


@Component
//@Order(-1)
@ComponentScan(basePackageClasses = {TokenUtilImpl.class, RedisUtilImpl.class})
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private AuthWhiteListProperties authWhiteListProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("AuthorizeFilter");
        
        // 获取请求路径
        String path = exchange.getRequest().getURI().getPath();
        
        // 检查是否在白名单中
        if (authWhiteListProperties.getWhiteList() != null && 
            authWhiteListProperties.getWhiteList().stream().anyMatch(path::startsWith)) {
            System.out.println("白名单路径，直接放行: " + path);
            return chain.filter(exchange);
        }
        
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        try {
            Map<String,Object> claims = JwtUtil.parseToken(token);
            String jti = (String) claims.get("jti");
            // 查询redis 黑名单是否存在此jti
            if (tokenUtil.exist(jti)){
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            ThreadLocalUtil.set(claims);
            // 放行
            return chain.filter(exchange);
        }catch(Exception e){
            //System.out.println("token error");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
