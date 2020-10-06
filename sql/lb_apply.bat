set LB_HOME=c:\java\liquibase
call %LB_HOME%\liquibase.bat --driver=org.postgresql.Driver ^
--classpath=%LB_HOME%\lib ^
--changeLogFile=C:\JavaProjects\masterjava\sql\databaseChangeLog.sql ^
--url="jdbc:postgresql://localhost:5432/masterjava" ^
--username=user ^
--password=password ^
--logLevel=debug ^
migrate