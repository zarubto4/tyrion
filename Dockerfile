FROM openjdk:8

COPY . .
RUN chmod -R +x ./sbt-dist
RUN chmod +x ./sbt
RUN ./sbt dist
RUN cd ./target/universal
RUN unzip dist.zip
RUN cd ./dist/bin
RUN chomd +x ./tyrion 
EXPOSE 80 443
CMD ["./tyrion"]
