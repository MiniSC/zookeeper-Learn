import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用LeaderLatch进行leader选举等操作
 * @author dongjj
 * 2018.1.10
 */
public class LeaderGet {
    private static  String ZK_ADDRESS = "192.168.108.65:218";
    private static final int CLIENT_QTY=10;
    private static final String PATH="/examples";

    public static void main(String[] args) throws Exception{
        List<CuratorFramework> clients = Lists.newArrayList();
        List<LeaderLatch> examples = Lists.newArrayList();
        TestingServer server = new TestingServer();

        try{
            for (int i = 0; i < CLIENT_QTY; ++i) {
                System.out.println(server.getConnectString());
                CuratorFramework client = createClientExample.createSimple(server.getConnectString());
                clients.add(client);
                LeaderLatch example = new LeaderLatch(client, PATH, "Client #" + i);
                examples.add(example);
                client.start();
                example.start();
            }
            Thread.sleep(20000);
            LeaderLatch currentLeader =null;
            for (int j = 0; j < 10; j++) {
                for (int i = 0; i <CLIENT_QTY ; i++) {
                    System.out.println(examples.get(i));
                    LeaderLatch example = examples.get(i);
                    System.out.println(example.hasLeadership());
                    if (example.hasLeadership()){
                        currentLeader=example;
                    }
                }
                System.out.println("current leader is " + currentLeader.getId());
                System.out.println("release the leader " + currentLeader.getId());
                currentLeader.close();
                System.out.println(examples.get(0).getParticipants());
                System.out.println("Client #0 maybe is elected as the leader or not although it want to be");
                System.out.println("the new leader is " + examples.get(0).getLeader().getId());

            }


            System.out.println("Press enter/return to quit\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            for (LeaderLatch exampleClient : examples) {
                CloseableUtils.closeQuietly(exampleClient);
            }
            for (CuratorFramework client : clients) {
                CloseableUtils.closeQuietly(client);
            }
           // CloseableUtils.closeQuietly(ZK_ADDRESS);
        }
    }
}
