import paxos.Acceptor;
import paxos.Proposer;
import paxos.utils.Networking;
import paxos.utils.Value;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Networking networking = Networking.getDefaultNetworking();

        int nProposers = 3;
        int nAcceptors = 7;

        try (ExecutorService proposerService = Executors.newFixedThreadPool(nProposers);
             ExecutorService acceptorService = Executors.newFixedThreadPool(nAcceptors);
             ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {

            for (int i = 1; i <= nProposers; i++) {
                networking.createRecvQueue(i);
                Proposer proposer = new Proposer(i, networking, new Value("Hello, world @ " + i));
                for (int j = 1001; j <= 1000 + nAcceptors; j++) {
                    proposer.addAcceptor(j);
                }
                proposerService.submit(proposer::run);
            }

            Set<Acceptor> acceptorSet = new HashSet<>();
            for (int i = 1001; i <= 1000 + nAcceptors; i++) {
                networking.createRecvQueue(i);
                Acceptor acceptor = new Acceptor(i, networking);
                acceptorSet.add(acceptor);
                acceptorService.submit(acceptor::run);
            }

            scheduledExecutorService.scheduleAtFixedRate(() -> {
                StringBuilder sb = new StringBuilder();
                sb.append("===========================================================================\n");
                sb.append(LocalTime.now()).append("\n\n");
                acceptorSet.forEach(acceptor -> sb.append(acceptor.status()));
                sb.append("===========================================================================\n");
                System.out.print(sb);
            }, 0, 1, TimeUnit.SECONDS);


            try {
                proposerService.shutdown();
                acceptorService.shutdown();

                if (!proposerService.awaitTermination(10, TimeUnit.SECONDS)) {
                    proposerService.shutdownNow();
                }

                if (!acceptorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    acceptorService.shutdownNow();
                }

                scheduledExecutorService.shutdown();
                scheduledExecutorService.shutdownNow();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                proposerService.shutdownNow();
                acceptorService.shutdownNow();
                scheduledExecutorService.shutdownNow();
            }
        }
    }
}
