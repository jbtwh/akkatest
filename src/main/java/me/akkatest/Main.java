package me.akkatest;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Main {
    public static class StartMsg {
        public final File from;
        public final File to;

        public StartMsg(File from, File to) {
            this.from = from;
            this.to = to;
        }
    }

    public static class StringsMsg {
        public final List<String> strings;

        public StringsMsg(List<String> strings) {
            this.strings = strings;
        }
    }

    public static class ResultMsg {
        public final Map<String, Integer> result;

        public ResultMsg(Map<String, Integer> result) {
            this.result = result;
        }
    }

    public static void main(String... args) {
        ActorSystem system = ActorSystem.create("akkatest");
        ActorRef master = system.actorOf(Master.createMaster(), "master");
        master.tell(new StartMsg(new File(Main.class.getClassLoader().getResource("test.txt").getFile()), new File("C:\\akkatest\\test2.txt")), ActorRef.noSender());
    }
}
