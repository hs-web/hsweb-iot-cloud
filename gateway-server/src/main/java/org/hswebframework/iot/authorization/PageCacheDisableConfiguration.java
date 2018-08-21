package org.hswebframework.iot.authorization;

import org.joda.time.DateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * dev,test,default环境中,禁用页面缓存
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
@Profile({"dev", "test", "default"})
@ConditionalOnProperty(prefix = "page.cache", name = "disable", havingValue = "true", matchIfMissing = true)
public class PageCacheDisableConfiguration {

    @Bean
    public OncePerRequestFilter disableCacheFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                response.setHeader(HttpHeaders.LAST_MODIFIED, new DateTime().plusSeconds(-1).toDate().toGMTString());
                filterChain.doFilter(request, response);
            }
        };
    }
}
