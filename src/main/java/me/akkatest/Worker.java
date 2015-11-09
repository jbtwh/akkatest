package me.akkatest;

import akka.actor.Props;
import akka.actor.UntypedActor;
import scala.collection.mutable.ArraySeq;

import java.util.HashMap;
import java.util.Map;

public class Worker extends UntypedActor {
    @Override
    public void onReceive(Object message) {
        if (message instanceof Main.StringsMsg) {
            Map<String, Integer> result = new HashMap<>();
            for (String s : ((Main.StringsMsg) message).strings) {
                String[] parts = s.split(";");
                String k = parts[0];
                Integer v = Integer.parseInt(parts[1]);
                if (result.get(k) == null) result.put(k, 0);
                result.put(k, result.get(k) + v);
            }
            getSender().tell(new Main.ResultMsg(result), getSelf());
        } else unhandled(message);
    }

    public static Props createWorker() {
        return Props.create(Worker.class, new ArraySeq<>(0));
    }
}
