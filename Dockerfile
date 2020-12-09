FROM adoptopenjdk/openjdk11:alpine-jre

VOLUME /tmp

EXPOSE 8080

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENV jasypt.encryptor.password password
ENV JAVA_OPTS="-Djasypt.encryptor.password=password -Xms1024m -Xmx1024m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -cp app:app/lib/* com.lendico.api.PlanGeneratorApplication" ]