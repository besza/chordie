FROM registry.access.redhat.com/ubi8/openjdk-17:1.11 AS builder
COPY . /home/jboss/chordie-app
USER root
RUN chown -R jboss /home/jboss/chordie-app
USER jboss
WORKDIR /home/jboss/chordie-app/
RUN mvn package

FROM registry.access.redhat.com/ubi8/openjdk-17:1.11 AS runtime
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'
EXPOSE 8080 5005
USER root
RUN microdnf -y --nodocs install \
    ghostscript \
    gpg \
    gzip \
    nano \
    perl \
    wget \
    which

USER jboss
RUN wget -qO- "https://yihui.org/tinytex/install-bin-unix.sh" | sh
ENV PATH="${PATH}:${HOME}/bin"
RUN tlmgr install \
        cnltx \
        etoolbox \
        guitarchordschemes \
        pgf \
        pgfopts \
        tikz-cd \
        trimspaces \
        dvisvgm \
        && tlmgr path add

COPY --from=builder --chown=185 /home/jboss/chordie-app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=185 /home/jboss/chordie-app/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=185 /home/jboss/chordie-app/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=185 /home/jboss/chordie-app/target/quarkus-app/quarkus/ /deployments/quarkus/

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

