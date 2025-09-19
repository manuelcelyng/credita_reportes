FROM gradle:jdk17-corretto AS TEMP_BUILD_IMAGE
ENV INSTALL_DIR=/opt/app
WORKDIR $INSTALL_DIR
COPY . $INSTALL_DIR
RUN gradle clean
RUN gradle bootJar

FROM eclipse-temurin:17-jre AS runtime

ENV INSTALL_DIR=/opt/app

ARG USERNAME=user
ARG USER_UID=999
ARG USER_GID=$USER_UID


WORKDIR $INSTALL_DIR

COPY --from=TEMP_BUILD_IMAGE $INSTALL_DIR/applications/app-service/build/libs/*.jar $INSTALL_DIR/app.jar

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && chown -R $USER_UID:$USER_GID $INSTALL_DIR

USER user


ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8082
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]