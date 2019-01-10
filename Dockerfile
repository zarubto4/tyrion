FROM openjdk:8

COPY . .
RUN chmod -R +x ./sbt-dist
RUN chmod +x ./sbt
RUN ./sbt compile
EXPOSE 80 443
CMD ["./sbt", "run"]
