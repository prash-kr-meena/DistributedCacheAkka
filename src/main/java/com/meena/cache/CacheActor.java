package com.meena.cache;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CacheActor extends AbstractBehavior<CacheActor.Command> {

  public static final EntityTypeKey<Command> ENTITY_KEY =
    EntityTypeKey.create(CacheActor.Command.class, "CacheActor");


  public static ActorRef<ShardingEnvelope<Command>> initSharding(ActorSystem<Void> distributedCacheClusterSystem) {
    ClusterSharding sharding = ClusterSharding.get(distributedCacheClusterSystem);
    ActorRef<ShardingEnvelope<Command>> shardRegion = sharding.init(
      Entity.of(ENTITY_KEY, entityContext -> CacheActor.create(entityContext.getEntityId()))
    );
    return shardRegion;
  }


  // --- Messages ---
  public interface Command extends Serializable { }

  public record Put(
    String key,
    Object value,
    ActorRef<CacheResponse> replyTo
  ) implements Command { }

  public record Get(
    String key,
    ActorRef<CacheResponse> replyTo
  ) implements Command { }

  public record Delete(
    String key,
    ActorRef<CacheResponse> replyTo
  ) implements Command { }


  // --- Responses ---
  public interface Response extends Serializable { }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CacheResponse(
    String status,
    Object value,
    String message
  ) implements Response {

  }

  // --- Actor state ---
  private final String entityId;

  private final Map<String, Object> cache = new HashMap<>();

  private static final String SUCCESS = "success";
  private static final String FAILURE = "failure";


  private CacheActor(ActorContext<Command> context, String entityId) {
    super(context);
    this.entityId = entityId;
  }


  public static Behavior<Command> create(String entityId) {
    return Behaviors.setup(context -> new CacheActor(context, entityId));
  }


  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
      .onMessage(Put.class, this::onPut)
      .onMessage(Get.class, this::onGet)
      .onMessage(Delete.class, this::onDelete)
      .build();
  }


  private Behavior<Command> onPut(Put put) {
    getContext().getLog().info("Putting value for key: {}", put.key);
    if (cache.containsKey(put.key)) {
      put.replyTo.tell(new CacheResponse(SUCCESS, put.value, "Value Already Exists!"));
      return Behaviors.same();
    }
    cache.put(put.key, put.value);
    put.replyTo.tell(new CacheResponse(SUCCESS, put.value, "Value stored successfully"));
    return Behaviors.same();
  }


  private Behavior<Command> onGet(Get get) {
    getContext().getLog().info("Getting value for key: {}", get.key);
    if (!cache.containsKey(get.key)) {
      get.replyTo.tell(new CacheResponse(FAILURE, null, "Key not found"));
      return Behaviors.same();
    }
    get.replyTo.tell(new CacheResponse(SUCCESS, cache.get(get.key), null));
    return Behaviors.same();
  }


  private Behavior<Command> onDelete(Delete delete) {
    getContext().getLog().info("Deleting value for key: {}", delete.key);
    if (!cache.containsKey(delete.key)) {
      delete.replyTo.tell(new CacheResponse(FAILURE, null, "Key not found"));
      return Behaviors.same();
    }
    cache.remove(delete.key);
    delete.replyTo.tell(new CacheResponse(SUCCESS, null, "Key deleted successfully"));
    return Behaviors.same();
  }

}