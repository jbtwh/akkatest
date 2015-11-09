package me.akkatest;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.routing.RoundRobinPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import scala.collection.mutable.ArraySeq;
import scala.concurrent.Future;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static akka.pattern.Patterns.ask;

public class Master extends UntypedActor {
    private ActorRef workerRouter;

    public Master() {
        workerRouter = this.getContext().actorOf(Worker.createWorker().withRouter(new RoundRobinPool(8)), "workerRouter");
    }

    @Override
    public void onReceive(final Object message) {
        if (message instanceof Main.StartMsg) {
            LineIterator it;
            try {
                it = FileUtils.lineIterator(((Main.StartMsg) message).from, "UTF-8");
                List<String> lines = new ArrayList<>();
                final ArrayList<Future<Object>> futures = new ArrayList<>();
                while (it.hasNext()) {
                    String line = it.nextLine();
                    lines.add(line);
                    if (lines.size() == 100) {
                        futures.add(ask(workerRouter, new Main.StringsMsg(new ArrayList<>(lines)), 1000));
                        lines.clear();
                    }
                }
                if (lines.size() > 0) {
                    futures.add(ask(workerRouter, new Main.StringsMsg(new ArrayList<>(lines)), 1000));
                    lines.clear();
                }
                LineIterator.closeQuietly(it);
                final Future<Iterable<Object>> aggregate = Futures.sequence(futures, getContext().system().dispatcher());
                aggregate.onSuccess(new OnSuccess<Iterable<Object>>() {
                    @Override
                    public void onSuccess(Iterable<Object> objects) throws Throwable {
                        Map<String, Integer> result = new HashMap<>();
                        for (Object m : objects) {
                            for (Map.Entry<String, Integer> e : ((Main.ResultMsg) m).result.entrySet()) {
                                String k = e.getKey();
                                Integer v = e.getValue();
                                if (result.get(k) == null) result.put(k, 0);
                                result.put(k, result.get(k) + v);
                            }
                        }
                        end();
                        createFile(result, ((Main.StartMsg) message).to);
                    }
                }, getContext().system().dispatcher());

            } catch (IOException e) {
                e.printStackTrace();
                end();
            }

        } else unhandled(message);

    }

    private void end() {
        getContext().system().shutdown();
    }

    private void createFile(Map<String, Integer> m, File f) {
        try {
            FileUtils.writeStringToFile(f, m.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Props createMaster() {
        return Props.create(Master.class, new ArraySeq<>(0));
    }
}
