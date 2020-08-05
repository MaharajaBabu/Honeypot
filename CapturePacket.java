/**
 * @Author: Maharaja Babu
 **/

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.sun.jna.Platform;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapStat;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

public class CapturePacket {
    static int i= 1;

    // gets all network devices available to capture packet
    static PcapNetworkInterface getNetworkDevice() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }

    public static void main(String[] args) throws PcapNativeException, NotOpenException, InterruptedException, DockerException, DockerCertificateException {

        Honeypot honey = new Honeypot();

        // gets network devices
        PcapNetworkInterface device = getNetworkDevice();
        System.out.println("U SELECTED: " + device);

        if (device == null) {
            System.out.println("NO NETWORK SELECTED: ");
            System.exit(1);
        }

        int snapshotLength = 65536;
        int readTimeout = 50;
        final PcapHandle handle;
        handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);

        //dump packet
        PcapDumper dumper = handle.dumpOpen("out.pcap");

        // port filtering
        String filter = "tcp port 2222";
        handle.setFilter(filter, BpfCompileMode.OPTIMIZE);


        /**
         *	Listens for packet
         *  @throws NotOpenException
         */
        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet)
            {

                System.out.println("\nTIME STAMP: "+ i +"\n\n"+handle.getTimestamp());
                System.out.println("\nPACKET: "+ i +"\n\n"+packet);
                i++;
                try
                {
                    dumper.dump(packet, handle.getTimestamp());
                }
                catch (NotOpenException e)
                {
                    e.printStackTrace();
                }
            }

        };

        /**
         *	runs cowrie honeypot
         *  @throws InterruptedException
         */
        try
        {
            int maxPackets = 1;
            handle.loop(maxPackets, listener);
            honey.start_cowrie();

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

      /*  // gets packet stats
        PcapStat stats = handle.getStats();
        System.out.println("Packets received: " + stats.getNumPacketsReceived());
        System.out.println("Packets dropped: " + stats.getNumPacketsDropped());
        System.out.println("Packets dropped by interface: " + stats.getNumPacketsDroppedByIf());

        dumper.close();
        handle.close();
        */
    }
}