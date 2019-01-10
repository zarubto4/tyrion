FROM openjdk:8

COPY . .
RUN chmod -R +x ./sbt-dist
RUN chmod +x ./sbt
RUN ./sbt dist
WORKDIR ./target/universal
RUN unzip ./dist.zip
WORKDIR ./dist/bin
RUN chomd +x ./tyrion 
EXPOSE 80 443
CMD ["./tyrion"]
