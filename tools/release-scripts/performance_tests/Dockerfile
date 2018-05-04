FROM alpine:3.5

RUN apk add --update --no-cache bash openjdk8 maven nodejs && \
    adduser -s /bin/bash -D -h /home/sepior -u 2000 sepior sepior

ENV JAVA_HOME /usr
ENV HOME /home/sepior
ENV WORKDIR $HOME/java-crypto

RUN mkdir -p $WORKDIR/jars

COPY *.jar $WORKDIR/jars/
COPY run_performance_tests.sh $WORKDIR/

RUN chown -R sepior:sepior $HOME

WORKDIR $WORKDIR

USER sepior

CMD ./run_performance_tests.sh "$WORKDIR/jars"
