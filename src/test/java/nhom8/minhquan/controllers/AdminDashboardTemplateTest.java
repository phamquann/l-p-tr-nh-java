package nhom8.minhquan.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.List;

class AdminDashboardTemplateTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dashboardTemplateParses() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new SpringSecurityDialect());

        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        IWebExchange exchange = JakartaServletWebApplication.buildApplication(servletContext).buildExchange(request, response);
        WebContext ctx = new WebContext(exchange);

        ctx.setVariable("pageTitle", "Dashboard - Tổng quan");
        ctx.setVariable("totalBooks", 0);
        ctx.setVariable("totalCategories", 0);
        ctx.setVariable("totalUsers", 0);
        ctx.setVariable("totalVouchers", 0);
        ctx.setVariable("validVouchers", 0);

        ctx.setVariable("totalOrders", 0);
        ctx.setVariable("pendingOrders", 0);
        ctx.setVariable("confirmedOrders", 0);
        ctx.setVariable("shippingOrders", 0);
        ctx.setVariable("completedOrders", 0);
        ctx.setVariable("cancelledOrders", 0);

        ctx.setVariable("recentBooks", List.of());
        ctx.setVariable("totalUnreadCount", 0);

        String html = templateEngine.process("admin/dashboard", ctx);
        if (html == null || html.isBlank()) {
            throw new AssertionError("Rendered HTML was blank");
        }
    }
}
