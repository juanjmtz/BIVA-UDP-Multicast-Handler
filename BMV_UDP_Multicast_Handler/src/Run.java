
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmartinezs
 */
public class Run {
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                if(args.length!=9){
                    System.out.println("Arguments: Group Port ReceiverIP FolderName(EUATFeedA/EUATFeedB/PRODFeedA/PRODFeedB) UserRecovery PasswordRecovery IPRecovery PortRecovery GroupRecovery");
                    System.exit(0);
                }
                
                try {
                    String myargs[] = new String[10];
                    myargs[0] = args[0];
                    myargs[1] = args[1];
                    myargs[2] = "BMV Multicast";
                    myargs[3] = args[2];
                    myargs[4] = args[3];
                    myargs[5] = args[4];
                    myargs[6] = args[5];
                    myargs[7] = args[6];
                    myargs[8] = args[7];
                    myargs[9] = args[8];
                    UDP.StartHandling(myargs);
                } catch (IOException ex) {
                    Logger.getLogger(Run.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
}
