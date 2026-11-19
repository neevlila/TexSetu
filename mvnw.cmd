@echo off
set MVN_EXEC="C:\Users\hp\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd"
if exist %MVN_EXEC% (
    %MVN_EXEC% %*
) else (
    mvn %*
)
