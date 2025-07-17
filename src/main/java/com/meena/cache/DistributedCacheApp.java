package com.meena.cache;

import akka.actor.AddressFromURIString;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

import akka.actor.typed.Behavior;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import com.meena.cache.CacheActor.Command;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DistributedCacheApp {


  public static void main(String[] args) {
    List<Integer> seedNodePorts = ConfigFactory.load().getStringList("akka.cluster.seed-nodes")
      .stream()
      .map(AddressFromURIString::parse)
      .map(addr -> (Integer) addr.port().get()) // Missing Java getter for port, fixed in Akka 2.6.2
      .toList();

    // Either use a single port provided by the user
    // Or start each listed seed nodes port plus one node on a random port in this single JVM if the user didn't provide args for the app
    // In a production application you wouldn't start multiple ActorSystem instances in the same JVM, here we do it to simplify running a sample cluster from a single main method.
    List<Integer> ports = Arrays.stream(args)
      .findFirst()
      .map(str -> Collections.singletonList(Integer.parseInt(str)))
      .orElseGet(() -> {
        List<Integer> portsAndZero = new ArrayList<>(seedNodePorts);
        portsAndZero.add(0);
        return portsAndZero;
      });

    startup(ports);
  }


  private static Config configWithPort(int port) {
    return ConfigFactory.parseMap(Collections.singletonMap("akka.remote.artery.canonical.port", Integer.toString(port)))
      .withFallback(ConfigFactory.load());
  }


  private static Behavior<Void> createRootBehavior(
    ActorRef<ShardingEnvelope<CacheActor.Command>> cacheShardRegion,
    int httpPort
  ) {
    return Behaviors.setup(context -> {
      ActorSystem<Void> distributedCacheClusterSystem = context.getSystem();
      CacheRoute cacheServerRoutes = new CacheRoute(distributedCacheClusterSystem, cacheShardRegion);

      Http http = Http.get(context.getSystem());
      final CompletionStage<ServerBinding> binding = http.newServerAt("localhost", httpPort)
        .bind(cacheServerRoutes.routes());

      context.getLog().info("Server online at http://localhost:{}/", httpPort);

      return Behaviors.empty();
    });
  }


  private static void startup(List<Integer> ports) {
    for (int port : ports) {
      // 0            : let OS decide
      // 10000 + port : offset from akka port
      final int httpPort = port > 0 ? 10000 + port : 0;

      Config config = configWithPort(port);
      //      Guardian.create(httpPort)

      ActorSystem<Void> distributedCacheClusterSystem =
        ActorSystem.create(Behaviors.empty(), "DistributedCacheCluster", config);
      ActorRef<ShardingEnvelope<Command>> cacheShardRegion = CacheActor.initSharding(distributedCacheClusterSystem);
      ActorRef<Void> cacheHttpServer = distributedCacheClusterSystem.systemActorOf(
        createRootBehavior(cacheShardRegion, httpPort),
        "CacheHttpServer",
        Props.empty()
      );
    }
  }

}
