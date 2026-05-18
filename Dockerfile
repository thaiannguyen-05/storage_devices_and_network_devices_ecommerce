# Sử dụng bản Payara Server (bản phân phối tối ưu, bảo mật của GlassFish) chạy trên JDK 17
FROM payara/server-full:6.2023.1-jdk17

# Đổi tên thành ROOT.war để tự động triển khai tại context-root "/"
COPY dist/WebApplication3.war /opt/payara/deployments/ROOT.war

# Mở cổng 8080 cho Web và 4848 cho Admin Console
EXPOSE 8080
EXPOSE 4848
