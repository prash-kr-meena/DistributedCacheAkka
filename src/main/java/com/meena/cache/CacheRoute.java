package com.meena.cache;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.meena.cache.CacheActor.CacheResponse;
import com.meena.cache.CacheActor.Command;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class CacheRoute extends AllDirectives {

  private final ActorRef<ShardingEnvelope<Command>> shardingRegion;
  private final Duration timeout;
  private final Scheduler scheduler;
  private final ActorSystem<?> system;


  public CacheRoute(ActorSystem<?> system, ActorRef<ShardingEnvelope<CacheActor.Command>> shardingRegion) {
    this.system = system;
    this.shardingRegion = shardingRegion;
    this.scheduler = system.scheduler();
    this.timeout = system.settings().config().getDuration("akka.http.server.request-timeout");
  }


  public record Value(Object value) {

    @JsonCreator
    public Value(
      @JsonProperty("value") Object value
    ) {
      this.value = value;
    }

  }


  public Route routes() {
    return pathPrefix("cache", () -> path(key -> concat(
          get(() -> {
            system.log().info("Received GET request for key: {}", key);
            CompletionStage<CacheResponse> futureResult = AskPattern.ask(
              shardingRegion, replyTo -> new ShardingEnvelope<>(key, new CacheActor.Get(key, replyTo)), timeout, scheduler
            );

            return onSuccess(futureResult, result -> {
              if (result.value() != null) {
                return complete(StatusCodes.OK, result, Jackson.marshaller());
              } else {
                return complete(StatusCodes.NOT_FOUND, result, Jackson.marshaller());
              }
            });
          }),
          put(() -> {
            system.log().info("Received PUT request for key: {}", key);
            return entity(Jackson.unmarshaller(Value.class), value -> {
              CompletionStage<CacheResponse> futureResult = AskPattern.ask(
                shardingRegion,
                replyTo -> new ShardingEnvelope<>(key, new CacheActor.Put(key, value.value, replyTo)),
                timeout, scheduler
              );
              return onSuccess(futureResult, result -> complete(StatusCodes.OK, result, Jackson.marshaller()));
            });
          }),
          delete(() -> {
            system.log().info("Received DELETE request for key: {}", key);
            CompletionStage<CacheResponse> futureResult = AskPattern.ask(
              shardingRegion,
              replyTo -> new ShardingEnvelope<>(key, new CacheActor.Delete(key, replyTo)),
              timeout, scheduler
            );
            return onSuccess(futureResult, result -> complete(StatusCodes.OK, result, Jackson.marshaller()));
          })
        )
      )
    );
  }

}



