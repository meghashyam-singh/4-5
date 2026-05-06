FROM mysql:8.0
COPY app-user.sql /docker-entrypoint.initdb.d/
COPY master-data.sql /docker-entrypoint.initdb.d/
ENV MYSQL_ROOT_PASSWORD="RoboShop@1"